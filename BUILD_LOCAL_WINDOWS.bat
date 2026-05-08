@echo off
cd /d %~dp0
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
echo.
echo APK hazir: app\build\outputs\apk\debug\app-debug.apk
