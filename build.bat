
:: Remove any " > NUL" at the end of a line, that u need for debug-purposes

@echo off
title ESRC-Language-Compiler, by Effyiex

timeout /t 1 > NUL

echo.
echo [ESRC-Build]: Compiling Language-Core...
echo.
py source/esrc/CoreCompiler.py

timeout /t 1 > NUL

echo.
echo [ESRC-Build]: Moving OS-Launchers...
echo.
xcopy /Y "source\esrc\launchers\esrc.bat" "compiled\esrc.bat" > NUL
xcopy /Y "source\esrc\launchers\esrcw.bat" "compiled\esrcw.bat" > NUL

timeout /t 1 > NUL

if "%1" == "" goto :eof

echo.
echo [ESRC-Build]: Launching Debug-Session...
echo.
"compiled/esrc" "%*"
