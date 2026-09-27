@echo off
echo Starting KRS Construction Full-Stack Application...

echo Checking for any process on port 8080 and killing it...
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :8080') DO (
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

echo Applications starting...
pause
