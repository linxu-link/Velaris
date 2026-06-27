#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_OUT="$ROOT_DIR/app/build/outputs"
DIST_DIR="${DIST_DIR:-$ROOT_DIR/tools/dist/release}"
VARIANT_NAME="${VARIANT_NAME:-prodRelease}"
VARIANT_DIR="${VARIANT_NAME%Release}/release"
VARIANT_CAPITALIZED="${VARIANT_NAME^}"

APK_SOURCE="$APP_OUT/apk/${VARIANT_DIR}/app-${VARIANT_DIR//\//-}.apk"
AAB_SOURCE="$APP_OUT/bundle/${VARIANT_NAME}/app-${VARIANT_DIR//\//-}.aab"
MAPPING_TXT_SOURCE="$APP_OUT/mapping/${VARIANT_NAME}/mapping.txt"
MAPPING_PRT_SOURCE="$APP_OUT/mapping/${VARIANT_NAME}/mapping.prt"

GRADLE_PARAMS=("--stacktrace")
if [[ $# -gt 0 ]]; then
    GRADLE_PARAMS+=("$@")
fi

copy_if_exists() {
    local source_file="$1"
    local target_file="$2"

    if [[ -f "$source_file" ]]; then
        cp "$source_file" "$target_file"
        echo "Copied: $target_file"
    else
        echo "Skip missing file: $source_file"
    fi
}

echo "ROOT_DIR=$ROOT_DIR"
echo "DIST_DIR=$DIST_DIR"
echo "VARIANT_NAME=$VARIANT_NAME"

mkdir -p "$DIST_DIR"

cd "$ROOT_DIR"

echo "Building ${VARIANT_NAME} APK and AAB..."
"$ROOT_DIR/gradlew" \
    :app:clean \
    ":app:assemble${VARIANT_CAPITALIZED}" \
    ":app:bundle${VARIANT_CAPITALIZED}" \
    "${GRADLE_PARAMS[@]}"
BUILD_RESULT=$?

if [[ ! -f "$APK_SOURCE" ]]; then
    echo "APK not found: $APK_SOURCE" >&2
    exit 1
fi

if [[ ! -f "$AAB_SOURCE" ]]; then
    echo "AAB not found: $AAB_SOURCE" >&2
    exit 1
fi

cp "$APK_SOURCE" "$DIST_DIR/app-${VARIANT_DIR//\//-}.apk"
cp "$AAB_SOURCE" "$DIST_DIR/app-${VARIANT_DIR//\//-}.aab"
copy_if_exists "$MAPPING_TXT_SOURCE" "$DIST_DIR/mapping.txt"
copy_if_exists "$MAPPING_PRT_SOURCE" "$DIST_DIR/mapping.prt"

echo "Release artifacts:"
echo "  APK: $DIST_DIR/app-${VARIANT_DIR//\//-}.apk"
echo "  AAB: $DIST_DIR/app-${VARIANT_DIR//\//-}.aab"
if [[ -f "$DIST_DIR/mapping.txt" ]]; then
    echo "  Mapping TXT: $DIST_DIR/mapping.txt"
fi
if [[ -f "$DIST_DIR/mapping.prt" ]]; then
    echo "  Mapping PRT: $DIST_DIR/mapping.prt"
fi

exit "$BUILD_RESULT"
