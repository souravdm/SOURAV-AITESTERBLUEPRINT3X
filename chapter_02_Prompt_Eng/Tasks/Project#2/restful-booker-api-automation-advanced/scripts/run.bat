@echo off
REM run.bat — Windows launcher for the Restful-Booker test suite.
REM Usage: scripts\run.bat [suite] [env] [threads]
REM   suite   : smoke | sanity | regression | e2e   (default: regression)
REM   env     : qa | stage | prod                    (default: qa)
REM   threads : integer                              (default: 4)

setlocal enabledelayedexpansion

set SUITE=%~1
set ENV=%~2
set THREADS=%~3

if "%SUITE%"=="" set SUITE=regression
if "%ENV%"=="" set ENV=qa
if "%THREADS%"=="" set THREADS=4

echo ========================================================
echo   Restful-Booker API Test Runner
echo   Suite   : %SUITE%
echo   Env     : %ENV%
echo   Threads : %THREADS%
echo ========================================================

cd /d "%~dp0.."

mvn clean test ^
    -Denv="%ENV%" ^
    -DsuiteXmlFile="testsuites/%SUITE%.xml" ^
    -Dparallel.thread.count="%THREADS%" ^
    --no-transfer-progress

echo.
echo Report: %CD%\target\extent-report\index.html
endlocal
