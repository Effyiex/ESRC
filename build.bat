
:: Setup of script environment
@echo off
title ESRC compiler script - by Effyiex

echo Compiling Language-Core...
py source/esrc/compiler/JavaCompiler.py > NUL

echo Injecting Bytecode into Wrapper...
py source/esrc/wrapper/WindowsInjection.py > NUL

echo Compiling Windows-Wrapper...
csc /out:"compiled/ESRCCore.exe" /win32icon:"assets/Icon.ico" compiled/ESRCWrapper.cs > NUL

echo Launching Debug-Session...
cd compiled
ESRCCore "%*"
cd ..
