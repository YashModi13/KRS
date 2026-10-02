@echo off
echo ========================================================
echo Starting KRS Construction Full-Stack Application...
echo ========================================================

FOR /F "tokens=2 delims=:" %%a IN ('ipconfig ^| findstr /c:"IPv4 Address"') DO (
    SET LOCAL_IP=%%a
    GOTO :found_ip
)
:found_ip
IF DEFINED LOCAL_IP (
    SET LOCAL_IP=%LOCAL_IP: =%
) ELSE (
    SET LOCAL_IP=localhost
)

echo Checking for any process on port 8081 and killing it...
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :8081') DO (
    IF NOT "%%T"=="0" (
        taskkill /PID %%T /F 2>nul
    )
)

echo Starting Backend (Spring Boot)...
cd /d "%~dp0Backend"
start cmd /k "mvn spring-boot:run"

echo Starting Frontend (Angular)...
cd /d "%~dp0Frontend"
start cmd /k "npm start"

echo.
echo ========================================================
echo   KRS Application is live and accessible externally!
echo   
echo   Access from THIS PC:
echo   http://localhost:4200
echo   
echo   Access from ANOTHER PC on Network:
echo   http://%LOCAL_IP%:4200
echo ========================================================
echo.
pause
