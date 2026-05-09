#!/usr/bin/env bash
set -euo pipefail
GRADLE_VERSION="8.10.2"
DIST_DIR="${HOME}/.gradle/standalone-dists"
GRADLE_HOME="${DIST_DIR}/gradle-${GRADLE_VERSION}"

if [ ! -x "${GRADLE_HOME}/bin/gradle" ]; then
  mkdir -p "${DIST_DIR}"
  ZIP_FILE="${DIST_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
  if [ ! -f "${ZIP_FILE}" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${ZIP_FILE}"
  fi
  rm -rf "${GRADLE_HOME}"
  unzip -q "${ZIP_FILE}" -d "${DIST_DIR}"
fi

if [ -n "${ANDROID_HOME:-}" ] && command -v sdkmanager >/dev/null 2>&1; then
  yes | sdkmanager --licenses >/dev/null 2>&1 || true
  sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools" >/dev/null 2>&1 || true
fi

exec "${GRADLE_HOME}/bin/gradle" "$@"
