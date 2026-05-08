@echo off
gradlew.bat --no-daemon assembleRelease
echo APK: app\build\outputs\apk\release\app-release.apk
