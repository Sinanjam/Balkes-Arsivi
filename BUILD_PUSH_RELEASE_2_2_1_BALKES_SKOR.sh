#!/usr/bin/env bash
set -euo pipefail

VERSION="2.2.1"
TAG="v${VERSION}"
APK_NAME="BalkesArsivi-v${VERSION}-release.apk"
RELEASE_NOTES="RELEASE_NOTES_2_2_1.md"
COMMIT_MSG="Balkes Skor butonu ekle v${VERSION}"
if ! command -v git >/dev/null 2>&1; then
  echo "HATA: git bulunamadı." >&2
  exit 1
fi
if ! command -v gh >/dev/null 2>&1; then
  echo "HATA: GitHub CLI (gh) bulunamadı. NixOS'ta nix-shell'a github-cli ekle ya da geçici olarak: nix shell nixpkgs#gh" >&2
  exit 1
fi
if ! gh auth status >/dev/null 2>&1; then
  echo "HATA: gh giriş yapılmamış. Önce: gh auth login" >&2
  exit 1
fi

BRANCH="${BRANCH:-$(git branch --show-current 2>/dev/null || true)}"
if [ -z "$BRANCH" ]; then BRANCH="main"; fi

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

chmod +x ./gradlew
./gradlew --stop || true
./gradlew clean assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
  echo "HATA: $APK_PATH bulunamadı." >&2
  exit 1
fi

echo "APK dosyası doğrulanıyor..."
if command -v zip >/dev/null 2>&1; then
  zip -T "$APK_PATH"
fi
unzip -l "$APK_PATH" AndroidManifest.xml >/dev/null
unzip -l "$APK_PATH" assets/archive/archive_items.json >/dev/null
unzip -p "$APK_PATH" assets/archive/archive_items.json | head -c 64 | grep -q "items"
unzip -l "$APK_PATH" assets/archive/archive_items_min.json >/dev/null

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

git add app/build.gradle app/src/main/java/com/sinanjam/arsiv/MainActivity.java balkes-android-2-0.nix "$RELEASE_NOTES" BUILD_PUSH_RELEASE_2_2_1_BALKES_SKOR.sh BUILD_PUSH_RELEASE_2_2_1_BALKES_SKOR.fish
if ! git diff --cached --quiet; then
  git commit -m "$COMMIT_MSG"
else
  echo "Commitlenecek yeni değişiklik yok."
fi

git pull --rebase origin "$BRANCH"
git push origin "$BRANCH"

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Yerel tag zaten var: $TAG"
else
  git tag "$TAG"
fi
if git ls-remote --exit-code --tags origin "$TAG" >/dev/null 2>&1; then
  echo "Uzak tag zaten var: $TAG"
else
  git push origin "$TAG"
fi

if gh release view "$TAG" >/dev/null 2>&1; then
  echo "Release zaten var, APK güncelleniyor: $TAG"
  gh release upload "$TAG" "$HOME/$APK_NAME" --clobber
else
  gh release create "$TAG" "$HOME/$APK_NAME" \
    --title "Balkes Arşivi ${VERSION}" \
    --notes-file "$RELEASE_NOTES" \
    --latest
fi

echo "Tamam: kod pushlandı, APK üretildi ve GitHub release hazırlandı."
