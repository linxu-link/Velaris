#!/usr/bin/env python3

"""Downscale PNG files next to this script to half of their original size.

The script intentionally uses only Python's standard library so it can run in a
fresh checkout without ImageMagick or Pillow. It supports the common PNG formats
used by Android assets: 8-bit, non-interlaced grayscale, RGB, palette,
grayscale-alpha, and RGBA images. Output files are written as 8-bit RGBA PNGs.
"""

from __future__ import annotations

import argparse
import binascii
import os
import struct
import sys
import tempfile
import zlib
from pathlib import Path


PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"


class PngError(RuntimeError):
    pass


def read_chunks(data: bytes) -> list[tuple[bytes, bytes]]:
    if not data.startswith(PNG_SIGNATURE):
        raise PngError("not a PNG file")

    chunks: list[tuple[bytes, bytes]] = []
    offset = len(PNG_SIGNATURE)
    while offset < len(data):
        if offset + 8 > len(data):
            raise PngError("truncated chunk header")
        length = struct.unpack(">I", data[offset : offset + 4])[0]
        chunk_type = data[offset + 4 : offset + 8]
        chunk_start = offset + 8
        chunk_end = chunk_start + length
        crc_end = chunk_end + 4
        if crc_end > len(data):
            raise PngError("truncated chunk data")

        expected_crc = struct.unpack(">I", data[chunk_end:crc_end])[0]
        actual_crc = binascii.crc32(chunk_type + data[chunk_start:chunk_end]) & 0xFFFFFFFF
        if expected_crc != actual_crc:
            raise PngError("CRC check failed")

        chunk_data = data[chunk_start:chunk_end]
        chunks.append((chunk_type, chunk_data))
        offset = crc_end
        if chunk_type == b"IEND":
            break

    return chunks


def parse_png(path: Path) -> tuple[int, int, list[tuple[int, int, int, int]]]:
    chunks = read_chunks(path.read_bytes())

    width = height = bit_depth = color_type = interlace = None
    palette: list[tuple[int, int, int]] = []
    palette_alpha: list[int] = []
    idat_parts: list[bytes] = []

    for chunk_type, chunk_data in chunks:
        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(
                ">IIBBBBB",
                chunk_data,
            )
        elif chunk_type == b"PLTE":
            if len(chunk_data) % 3 != 0:
                raise PngError("invalid palette")
            palette = [
                tuple(chunk_data[index : index + 3])  # type: ignore[arg-type]
                for index in range(0, len(chunk_data), 3)
            ]
        elif chunk_type == b"tRNS":
            palette_alpha = list(chunk_data)
        elif chunk_type == b"IDAT":
            idat_parts.append(chunk_data)

    if width is None or height is None or bit_depth is None or color_type is None:
        raise PngError("missing IHDR")
    if bit_depth != 8:
        raise PngError(f"unsupported bit depth: {bit_depth}")
    if interlace != 0:
        raise PngError("interlaced PNG is not supported")

    channels_by_type = {
        0: 1,
        2: 3,
        3: 1,
        4: 2,
        6: 4,
    }
    channels = channels_by_type.get(color_type)
    if channels is None:
        raise PngError(f"unsupported color type: {color_type}")

    raw = zlib.decompress(b"".join(idat_parts))
    stride = width * channels
    rows = unfilter_rows(raw, width, height, channels)

    pixels: list[tuple[int, int, int, int]] = []
    for row in rows:
        if color_type == 0:
            pixels.extend((value, value, value, 255) for value in row)
        elif color_type == 2:
            pixels.extend(
                (row[index], row[index + 1], row[index + 2], 255)
                for index in range(0, stride, 3)
            )
        elif color_type == 3:
            for value in row:
                if value >= len(palette):
                    raise PngError("palette index out of range")
                red, green, blue = palette[value]
                alpha = palette_alpha[value] if value < len(palette_alpha) else 255
                pixels.append((red, green, blue, alpha))
        elif color_type == 4:
            pixels.extend(
                (row[index], row[index], row[index], row[index + 1])
                for index in range(0, stride, 2)
            )
        elif color_type == 6:
            pixels.extend(
                (row[index], row[index + 1], row[index + 2], row[index + 3])
                for index in range(0, stride, 4)
            )

    return width, height, pixels


def unfilter_rows(raw: bytes, width: int, height: int, channels: int) -> list[bytearray]:
    row_length = width * channels
    expected = height * (row_length + 1)
    if len(raw) != expected:
        raise PngError("unexpected decompressed image size")

    rows: list[bytearray] = []
    offset = 0
    previous = bytearray(row_length)
    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        current = bytearray(raw[offset : offset + row_length])
        offset += row_length

        for index in range(row_length):
            left = current[index - channels] if index >= channels else 0
            up = previous[index]
            upper_left = previous[index - channels] if index >= channels else 0

            if filter_type == 0:
                predictor = 0
            elif filter_type == 1:
                predictor = left
            elif filter_type == 2:
                predictor = up
            elif filter_type == 3:
                predictor = (left + up) // 2
            elif filter_type == 4:
                predictor = paeth(left, up, upper_left)
            else:
                raise PngError(f"unknown filter type: {filter_type}")

            current[index] = (current[index] + predictor) & 0xFF

        rows.append(current)
        previous = current

    return rows


def paeth(left: int, up: int, upper_left: int) -> int:
    estimate = left + up - upper_left
    left_distance = abs(estimate - left)
    up_distance = abs(estimate - up)
    upper_left_distance = abs(estimate - upper_left)
    if left_distance <= up_distance and left_distance <= upper_left_distance:
        return left
    if up_distance <= upper_left_distance:
        return up
    return upper_left


def downscale_half(
    width: int,
    height: int,
    pixels: list[tuple[int, int, int, int]],
) -> tuple[int, int, list[tuple[int, int, int, int]]]:
    new_width = max(1, width // 2)
    new_height = max(1, height // 2)
    new_pixels: list[tuple[int, int, int, int]] = []

    for y in range(new_height):
        for x in range(new_width):
            source_indexes = []
            for source_y in (y * 2, min(y * 2 + 1, height - 1)):
                for source_x in (x * 2, min(x * 2 + 1, width - 1)):
                    source_indexes.append(source_y * width + source_x)

            red = sum(pixels[index][0] for index in source_indexes) // 4
            green = sum(pixels[index][1] for index in source_indexes) // 4
            blue = sum(pixels[index][2] for index in source_indexes) // 4
            alpha = sum(pixels[index][3] for index in source_indexes) // 4
            new_pixels.append((red, green, blue, alpha))

    return new_width, new_height, new_pixels


def write_png(path: Path, width: int, height: int, pixels: list[tuple[int, int, int, int]]) -> None:
    rows = bytearray()
    for y in range(height):
        rows.append(0)
        for x in range(width):
            rows.extend(pixels[y * width + x])

    chunks = [
        make_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)),
        make_chunk(b"IDAT", zlib.compress(bytes(rows), level=9)),
        make_chunk(b"IEND", b""),
    ]
    payload = PNG_SIGNATURE + b"".join(chunks)

    fd, temp_name = tempfile.mkstemp(
        prefix=f".{path.name}.",
        suffix=".tmp",
        dir=str(path.parent),
    )
    try:
        with os.fdopen(fd, "wb") as temp_file:
            temp_file.write(payload)
        os.replace(temp_name, path)
    except Exception:
        try:
            os.unlink(temp_name)
        finally:
            raise


def make_chunk(chunk_type: bytes, chunk_data: bytes) -> bytes:
    crc = binascii.crc32(chunk_type + chunk_data) & 0xFFFFFFFF
    return (
        struct.pack(">I", len(chunk_data))
        + chunk_type
        + chunk_data
        + struct.pack(">I", crc)
    )


def process_file(path: Path, dry_run: bool) -> None:
    width, height, pixels = parse_png(path)
    new_width, new_height, new_pixels = downscale_half(width, height, pixels)

    if dry_run:
        print(f"{path.name}: {width}x{height} -> {new_width}x{new_height}")
        return

    write_png(path, new_width, new_height, new_pixels)
    print(f"{path.name}: {width}x{height} -> {new_width}x{new_height}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Downscale PNG files in this script's directory to half size.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the planned changes without writing files.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    script_dir = Path(__file__).resolve().parent
    png_paths = sorted(
        path
        for path in script_dir.iterdir()
        if path.is_file() and path.suffix.lower() == ".png"
    )

    if not png_paths:
        print(f"No PNG files found in {script_dir}")
        return 0

    failed = False
    for path in png_paths:
        try:
            process_file(path, args.dry_run)
        except Exception as error:
            failed = True
            print(f"{path.name}: skipped ({error})", file=sys.stderr)

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
