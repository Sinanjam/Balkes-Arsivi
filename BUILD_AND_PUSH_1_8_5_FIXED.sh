#!/usr/bin/env bash
set -euo pipefail

REPO_URL="https://github.com/Sinanjam/Balkes-Arsivi.git"
REPO_API="https://api.github.com/repos/Sinanjam/Balkes-Arsivi"
VERSION="1.8.5"
TAG="v${VERSION}"
APK_NAME="BalkesArsivi-v${VERSION}-release.apk"
TITLE="Balkes Arşivi 1.8.5 - Parlak Rötüş"
BODY=$'Fix sürümü:\n\n- Metin bozulmaları temizlendi\n- Tablolar düzenlendi\n- Fotoğraf ekranı sadeleştirildi\n- Splash ekranı temizlendi\n- Paylaşım akışı iyileştirildi'

if [ ! -d .git ]; then
  git init
  git branch -M main
fi

git remote remove origin >/dev/null 2>&1 || true
git remote add origin "$REPO_URL"

read -rp "GitHub kullanıcı adın [Sinanjam]: " GUSER
GUSER=${GUSER:-Sinanjam}
read -rsp "GitHub token: " GTOKEN
echo

cat > local.properties <<EOF
sdk.dir=${ANDROID_HOME}
EOF

AAPT2_PATH="${AAPT2_PATH:-}"
if [ -z "$AAPT2_PATH" ]; then
  AAPT2_PATH=$(find "$ANDROID_HOME/build-tools" -type f -name aapt2 | sort | tail -n 1 || true)
fi
if [ -z "$AAPT2_PATH" ]; then
  echo "HATA: aapt2 bulunamadı" >&2
  exit 1
fi

grep -q 'android.aapt2FromMavenOverride=' gradle.properties || echo "android.aapt2FromMavenOverride=$AAPT2_PATH" >> gradle.properties

chmod +x gradlew || true
./gradlew --stop || true
./gradlew clean assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
[ -f "$APK_PATH" ] || { echo "HATA: $APK_PATH bulunamadı" >&2; exit 1; }
cp -f "$APK_PATH" "$HOME/$APK_NAME"
echo "APK hazır: $HOME/$APK_NAME"

cat > .gitignore <<'EOF'
.gradle/
build/
app/build/
local.properties
*.apk
*.aab
*.zip
*.tar
*.tar.gz
*.tar.zst
.DS_Store
EOF

git add .
git commit -m "1.8.5 fix sürümü" || true
git push --force "https://${GUSER}:${GTOKEN}@github.com/Sinanjam/Balkes-Arsivi.git" main

git tag -f "$TAG"
git push -f "https://${GUSER}:${GTOKEN}@github.com/Sinanjam/Balkes-Arsivi.git" "$TAG"

OLD_REL=$(curl -sS -H "Authorization: token $GTOKEN" "$REPO_API/releases/tags/$TAG" || true)
REL_ID=$(printf '%s' "$OLD_REL" | sed -n 's/.*"id": \([0-9][0-9]*\).*/\1/p' | head -n 1)
if [ -n "$REL_ID" ]; then
  curl -sS -X DELETE -H "Authorization: token $GTOKEN" "$REPO_API/releases/$REL_ID" >/dev/null || true
fi

BODY_JSON=$(python3 -c 'import json,sys; print(json.dumps(sys.argv[1]))' "$BODY")
RELEASE_JSON=$(mktemp)
printf '{"tag_name":"%s","name":"%s","body":%s,"draft":false,"prerelease":false}' "$TAG" "$TITLE" "$BODY_JSON" \
  | curl -sS -X POST -H "Authorization: token $GTOKEN" -H "Content-Type: application/json" "$REPO_API/releases" -d @- > "$RELEASE_JSON"
UPLOAD_URL=$(sed -n 's/.*"upload_url": "\([^"]*\){?name,label}?".*/\1/p' "$RELEASE_JSON" | head -n 1)
if [ -n "$UPLOAD_URL" ]; then
  curl -sS -X POST \
    -H "Authorization: token $GTOKEN" \
    -H "Content-Type: application/vnd.android.package-archive" \
    --data-binary @"$HOME/$APK_NAME" \
    "${UPLOAD_URL}?name=${APK_NAME}" >/dev/null
fi

echo "Tamamlandı."
echo "APK: $HOME/$APK_NAME"
