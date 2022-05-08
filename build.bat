
@echo off
title ESRC compiler script - by Effyiex

echo.
echo [ESRC-Build]: Compiling Language-Core...
echo.
py source/esrc/compiler/CoreCompiler.py

xcopy /Y "source\esrc\launchers\esrc.bat" "compiled\esrc.bat" > NUL

echo.
echo [ESRC-Build]: Launching Debug-Session...
echo.
"compiled/esrc" "%*"
