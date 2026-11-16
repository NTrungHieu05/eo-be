@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Dang chay kiem tra he thong. Cua so nay se dong khi xong, ket qua luu o demo.log
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { .\scripts\docker-demo.ps1 *>&1 | Tee-Object -FilePath demo.log }"
echo.
echo ===== XONG. Quay lai Claude va nhan "da chay xong" =====
pause
