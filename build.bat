
@echo off
title ESRC compiler script - by Effyiex

echo.
echo [ESRC-Build]: Compiling Language-Core...
echo.
py source/esrc/compiler/JavaCompiler.py

echo.
echo [ESRC-Build]: Injecting Bytecode into Wrapper...
echo.
py source/esrc/wrapper/Windows/Injection.py

echo.
echo [ESRC-Build]: Compiling Windows-Wrapper...
echo.
csc /out:"compiled/ESRCCore.exe" /win32icon:"assets/Icon.ico" compiled/ESRCWrapper.cs

echo.
echo [ESRC-Build]: Launching Debug-Session...
echo.
"compiled/ESRCCore" "%*"
