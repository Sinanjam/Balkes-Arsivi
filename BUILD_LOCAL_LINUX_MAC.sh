#!/usr/bin/env bash
set -euo pipefail
./gradlew --no-daemon assembleRelease
printf '\nAPK: app/build/outputs/apk/release/app-release.apk\n'
