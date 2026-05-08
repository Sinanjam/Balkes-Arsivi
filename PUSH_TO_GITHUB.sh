#!/usr/bin/env bash
set -euo pipefail

REPO_URL="${1:-https://github.com/Sinanjam/Balkes-Arsivi.git}"
BRANCH="${BRANCH:-main}"
COMMIT_MSG="${COMMIT_MSG:-Balkes Arşivi final Android sürümü}"

cd "$(dirname "$0")"

echo "== Balkes Arşivi GitHub push =="
echo "Repo:   $REPO_URL"
echo "Branch: $BRANCH"
echo

git --version >/dev/null

if [ ! -d .git ]; then
  git init
fi

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REPO_URL"
else
  git remote add origin "$REPO_URL"
fi

git checkout -B "$BRANCH"

git add -A

if git diff --cached --quiet; then
  echo "Commitlenecek değişiklik yok. Yine de push deneniyor."
else
  git commit -m "$COMMIT_MSG"
fi

echo
echo "Şimdi GitHub kullanıcı adı ve parola/token sorabilir."
echo "Username: Sinanjam"
echo "Password: GitHub tokenını yapıştır. Token ekranda görünmez."
echo

if [ "${FORCE_PUSH:-0}" = "1" ]; then
  git push --force-with-lease -u origin "$BRANCH"
else
  git push -u origin "$BRANCH"
fi

echo
echo "Bitti. GitHub Actions sekmesinden APK buildini takip edebilirsin."
