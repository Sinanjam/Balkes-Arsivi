#!/usr/bin/env bash
set -euo pipefail

VERSION="2.1-install-fix"
APK_NAME="BalkesArsivi-v${VERSION}-release.apk"

cat > local.properties <<LOCAL
sdk.dir=${ANDROID_HOME:-}
LOCAL

AAPT2_PATH="${AAPT2_PATH:-}"
if [ -z "$AAPT2_PATH" ] && [ -n "${ANDROID_HOME:-}" ] && [ -f "$ANDROID_HOME/build-tools/35.0.0/aapt2" ]; then
  AAPT2_PATH="$ANDROID_HOME/build-tools/35.0.0/aapt2"
fi
if [ -z "$AAPT2_PATH" ] && [ -n "${ANDROID_HOME:-}" ] && [ -f "$ANDROID_HOME/build-tools/34.0.0/aapt2" ]; then
  AAPT2_PATH="$ANDROID_HOME/build-tools/34.0.0/aapt2"
fi
if [ -z "$AAPT2_PATH" ]; then
  AAPT2_PATH=$(find /nix/store -type f -path '*/libexec/android-sdk/build-tools/35.0.0/aapt2' 2>/dev/null | sort -V | tail -n 1 || true)
fi
if [ -z "$AAPT2_PATH" ]; then
  AAPT2_PATH=$(find /nix/store -type f -path '*/libexec/android-sdk/build-tools/*/aapt2' 2>/dev/null | sort -V | tail -n 1 || true)
fi
if [ -n "$AAPT2_PATH" ]; then
  grep -v 'android.aapt2FromMavenOverride=' gradle.properties > /tmp/balkes_gradle.properties || true
  mv /tmp/balkes_gradle.properties gradle.properties
  echo "android.aapt2FromMavenOverride=$AAPT2_PATH" >> gradle.properties
fi

echo "ANDROID_HOME=${ANDROID_HOME:-}"
echo "AAPT2_PATH=${AAPT2_PATH:-}"
[ -n "${ANDROID_HOME:-}" ] && ls -la "$ANDROID_HOME/build-tools" || true

chmod +x ./gradlew
./gradlew --stop || true
./gradlew clean assembleRelease
APK_PATH="app/build/outputs/apk/release/app-release.apk"
[ -f "$APK_PATH" ] || { echo "HATA: $APK_PATH bulunamadı" >&2; exit 1; }


echo "APK dosyası doğrulanıyor..."
command -v zip >/dev/null 2>&1 && zip -T "$APK_PATH"
unzip -l "$APK_PATH" AndroidManifest.xml >/dev/null
if command -v apksigner >/dev/null 2>&1; then
  apksigner verify --verbose "$APK_PATH" >/dev/null
elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/build-tools/35.0.0/apksigner" ]; then
  "$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose "$APK_PATH" >/dev/null
elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/build-tools/34.0.0/apksigner" ]; then
  "$ANDROID_HOME/build-tools/34.0.0/apksigner" verify --verbose "$APK_PATH" >/dev/null
else
  echo "apksigner bulunamadı, imza doğrulama atlandı."
fi

cp -f "$APK_PATH" "$HOME/$APK_NAME"
echo "APK hazır: $HOME/$APK_NAME"
