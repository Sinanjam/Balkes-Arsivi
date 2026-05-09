#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(pwd)"
REPO="Balkes-Arsivi"
TAG="v1.8.0"
TITLE="Balkes Arşivi 1.8 — Premium Deneyim"
APK_OUT="$HOME/BalkesArsivi-v1.8.0-release.apk"
WORK="$HOME/Downloads/Balkes-Arsivi-PUSH"
NIXF="$HOME/balkes-android-18-shell.nix"
RUNNER="/tmp/balkes_build_18_runner.sh"

if [ ! -f "$PROJECT_DIR/settings.gradle" ] || [ ! -d "$PROJECT_DIR/app" ]; then
  echo "HATA: Bu script'i 1.8 proje klasöründe çalıştırmalısın."
  echo "Örnek: cd ~/Downloads/BalkesArsivi_1_8_BUILD && bash BUILD_AND_PUSH_1_8_FIXED.sh"
  exit 1
fi

read -rp "GitHub kullanıcı adın [Sinanjam]: " GH_USER
GH_USER="${GH_USER:-Sinanjam}"
read -rsp "GitHub token: " GH_TOKEN
echo
if [ -z "$GH_TOKEN" ]; then
  echo "HATA: Token boş olamaz."
  exit 1
fi
export GH_USER GH_TOKEN PROJECT_DIR APK_OUT WORK REPO TAG TITLE

cat > "$NIXF" <<'NIX'
{ pkgs ? import <nixpkgs> {
    config = {
      allowUnfree = true;
      android_sdk.accept_license = true;
    };
  }
}:
let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    platformVersions = [ "35" "34" ];
    buildToolsVersions = [ "35.0.0" "34.0.0" ];
    includeNDK = false;
  };
in pkgs.mkShell {
  buildInputs = with pkgs; [
    androidComposition.androidsdk
    jdk17
    gradle_8
    git
    gh
    rsync
    unzip
    zip
    findutils
    coreutils
    gnused
    cacert
  ];
  shellHook = ''
    export ANDROID_HOME=${androidComposition.androidsdk}/libexec/android-sdk
    export ANDROID_SDK_ROOT=$ANDROID_HOME
  '';
}
NIX

cat > "$RUNNER" <<'RUN'
#!/usr/bin/env bash
set -euo pipefail
cd "$PROJECT_DIR"

: "${ANDROID_HOME:?ANDROID_HOME boş}"
echo "SDK=$ANDROID_HOME"

AAPT2_PATH="$(find "$ANDROID_HOME/build-tools" -type f -name aapt2 2>/dev/null | sort -V | tail -n 1 || true)"
if [ -z "$AAPT2_PATH" ]; then
  AAPT2_PATH="$(find /nix/store -type f -path '*/libexec/android-sdk/build-tools/*/aapt2' 2>/dev/null | sort -V | tail -n 1 || true)"
fi
if [ -z "$AAPT2_PATH" ]; then
  echo "HATA: aapt2 bulunamadı."
  exit 1
fi
echo "AAPT2=$AAPT2_PATH"

printf "sdk.dir=%s\n" "$ANDROID_HOME" > local.properties
touch gradle.properties
grep -v "android.aapt2FromMavenOverride" gradle.properties > /tmp/balkes_gradle.properties || true
mv /tmp/balkes_gradle.properties gradle.properties
echo "android.aapt2FromMavenOverride=$AAPT2_PATH" >> gradle.properties

gradle --stop || true
rm -rf "$HOME"/.gradle/caches/8.14.4/transforms/*/transformed/aapt2-* 2>/dev/null || true
rm -rf "$HOME"/.gradle/caches/modules-2/files-2.1/com.android.tools.build/aapt2 2>/dev/null || true

echo "Release APK derleniyor..."
gradle --no-daemon assembleRelease
cp app/build/outputs/apk/release/app-release.apk "$APK_OUT"
echo "APK hazır: $APK_OUT"
ls -lh "$APK_OUT"

ASKPASS="$(mktemp)"
trap 'rm -f "$ASKPASS"' EXIT
cat > "$ASKPASS" <<ASK
#!/usr/bin/env bash
case "\$1" in
  *Username*) echo "$GH_USER" ;;
  *Password*) echo "$GH_TOKEN" ;;
esac
ASK
chmod +x "$ASKPASS"
export GIT_ASKPASS="$ASKPASS"
export GIT_TERMINAL_PROMPT=0
export GH_TOKEN="$GH_TOKEN"

rm -rf "$WORK"
echo "GitHub repo klonlanıyor..."
git clone "https://github.com/$GH_USER/$REPO.git" "$WORK"

echo "Kaynaklar temiz repo klasörüne aktarılıyor..."
rsync -a --delete \
  --exclude='.git/' \
  --exclude='.gradle/' \
  --exclude='build/' \
  --exclude='app/build/' \
  --exclude='local.properties' \
  --exclude='*.apk' \
  --exclude='*.aab' \
  --exclude='*.zip' \
  --exclude='*.tar' \
  --exclude='*.tar.gz' \
  --exclude='*.tar.zst' \
  "$PROJECT_DIR/" "$WORK/"

cd "$WORK"
rm -f local.properties
rm -rf .gradle build app/build
if [ -f gradle.properties ]; then
  sed -i '/android.aapt2FromMavenOverride/d;/sdk.dir=/d;/\/nix\/store/d;/\/home\//d' gradle.properties
fi
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

git config user.name "$GH_USER"
git config user.email "$GH_USER@users.noreply.github.com"
git add .
if git diff --cached --quiet; then
  echo "Commitlenecek değişiklik yok."
else
  git commit -m "Release 1.8 Premium Deneyim"
fi

echo "GitHub'a pushlanıyor..."
git push origin main

git tag -f "$TAG"
git push -f origin "$TAG"

cat > /tmp/balkes_18_release_notes.md <<'NOTES'
Balkes Arşivi 1.8 — Premium Deneyim

Bu sürümün amacı: premium deneyim ve görünüm.

Öne çıkanlar:
- Fotoğraf bilgi paneli eklendi.
- Fotoğraf işlem menüsü eklendi.
- Gelişmiş arama filtreleri eklendi.
- Arama sonucu sayısı görünür hale geldi.
- Tam ekran tablo görüntüleme eklendi.
- Tabloyu görsel olarak paylaşma eklendi.
- Okuma ilerleme çubuğu eklendi.
- Gereksiz uzun arşiv/duyuru yazıları temizlendi.
- Ana ekrandaki sürüm bilgisi kutucukların altına alındı.
- GitHub verisi bozulursa yerel arşive güvenli dönüş eklendi.
- Sürüm notları açılış penceresi eklendi.
- Kart, buton ve boşluk düzeni premium görünüme yaklaştırıldı.

Wayback kurtarma verileri bu sürüme eklenmedi; 1.9 sürümüne bırakıldı.
NOTES

ASSET_NAME="BalkesArsivi-v1.8.0-release.apk"
cp "$APK_OUT" "$ASSET_NAME"

gh release delete "$TAG" -y >/dev/null 2>/dev/null || true
gh release create "$TAG" "$ASSET_NAME" --title "$TITLE" --notes-file /tmp/balkes_18_release_notes.md

echo
 echo "Tamamlandı."
echo "APK: $APK_OUT"
echo "Repo: https://github.com/$GH_USER/$REPO"
echo "Release: https://github.com/$GH_USER/$REPO/releases/tag/$TAG"
RUN
chmod +x "$RUNNER"

echo "NixOS Android ortamında build + push + release başlıyor..."
env NIXPKGS_ALLOW_UNFREE=1 nix-shell "$NIXF" --run "bash $RUNNER"
