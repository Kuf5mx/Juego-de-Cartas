@echo off
setlocal
cd /d "%~dp0"

echo Cerrando instancias previas de Tecmimon...
taskkill /F /IM Tecmimon.exe >nul 2>nul

echo Abriendo Tecmimon...
start "Tecmimon" "%~dp0dist\Tecmimon\Tecmimon.exe"

echo Si no aparece la ventana, revisa la barra de tareas o Alt+Tab.
endlocal
