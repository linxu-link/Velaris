#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAIN_ACTIVITY="com.wujia.velaris.MainActivity"

BUILD_VARIANT="prodDebug"
GRADLE_TASK=":app:assembleProdDebug"
APK_PATH="$ROOT_DIR/app/build/outputs/apk/prod/debug/app-prod-debug.apk"
APPLICATION_ID="com.wujia.velaris.debug"

get_device_serials() {
    adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'
}

prompt_build_variant() {
    local selected=0
    local key

    if [[ ! -t 0 || ! -t 1 ]]; then
        return 1
    fi

    printf "Select build variant. Use Up/Down, Enter to confirm.\n"

    while true; do
        if [[ ${selected} -eq 0 ]]; then
            printf "\r\033[K> Prod Debug  (default)\n\033[K  Prod Release"
        else
            printf "\r\033[K  Prod Debug  (default)\n\033[K> Prod Release"
        fi
        printf "\033[1A\r"

        IFS= read -rsn1 key || true
        case "${key}" in
            "")
                printf "\033[1B\r\033[K"
                if [[ ${selected} -eq 1 ]]; then
                    BUILD_VARIANT="prodRelease"
                    GRADLE_TASK=":app:assembleProdRelease"
                    APK_PATH="$ROOT_DIR/app/build/outputs/apk/prod/release/app-prod-release.apk"
                    APPLICATION_ID="com.wujia.velaris.release"
                fi
                return 0
                ;;
            $'\x1b')
                IFS= read -rsn2 -t 0.1 key || true
                case "${key}" in
                    "[A"|"[B")
                        selected=$((1 - selected))
                        ;;
                esac
                ;;
        esac
    done
}

prompt_uninstall() {
    local selected=0
    local key

    if [[ ! -t 0 || ! -t 1 ]]; then
        return 1
    fi

    printf "Need uninstall current app before install? Use Up/Down, Enter to confirm.\n"

    while true; do
        if [[ ${selected} -eq 0 ]]; then
            printf "\r\033[K> No  (default)\n\033[K  Yes"
        else
            printf "\r\033[K  No  (default)\n\033[K> Yes"
        fi
        printf "\033[1A\r"

        IFS= read -rsn1 key || true
        case "${key}" in
            "")
                printf "\033[1B\r\033[K"
                [[ ${selected} -eq 1 ]]
                return
                ;;
            $'\x1b')
                IFS= read -rsn2 -t 0.1 key || true
                case "${key}" in
                    "[A"|"[B")
                        selected=$((1 - selected))
                        ;;
                esac
                ;;
        esac
    done
}

prompt_build_variant || true

echo "Building ${BUILD_VARIANT} APK..."
cd "${ROOT_DIR}"
"${ROOT_DIR}/gradlew" "${GRADLE_TASK}"

mapfile -t DEVICE_SERIALS < <(get_device_serials)

if [[ ${#DEVICE_SERIALS[@]} -eq 0 ]]; then
    echo "No connected adb devices found." >&2
    exit 1
fi

if prompt_uninstall; then
    for serial in "${DEVICE_SERIALS[@]}"; do
        echo "Uninstalling ${APPLICATION_ID} on ${serial}..."
        adb -s "${serial}" uninstall "${APPLICATION_ID}" || true
    done
else
    echo "Skip uninstall."
fi

if [[ ! -f "${APK_PATH}" ]]; then
    echo "APK not found: ${APK_PATH}" >&2
    exit 1
fi

for serial in "${DEVICE_SERIALS[@]}"; do
    echo "Installing ${APK_PATH} on ${serial}..."
    adb -s "${serial}" install -r "${APK_PATH}"

    echo "Launching ${MAIN_ACTIVITY} on ${serial}..."
    adb -s "${serial}" shell am start -n "${APPLICATION_ID}/${MAIN_ACTIVITY}"
done
echo "Done."
