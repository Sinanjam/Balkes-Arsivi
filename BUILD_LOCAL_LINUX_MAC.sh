#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
chmod +x ./gradlew
./gradlew assembleDebug
printf '\nAPK hazır: app/build/outputs/apk/debug/app-debug.apk\n'
