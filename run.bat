@echo off
setlocal EnableExtensions

cd /d "%~dp0"

call "%~dp0scripts\ta-recruitment.bat" build
if errorlevel 1 goto failed

call "%~dp0scripts\ta-recruitment.bat" foreground
if errorlevel 1 goto failed

exit /b 0

:failed
echo.
echo Run failed. Please check the error message above.
pause
exit /b 1
