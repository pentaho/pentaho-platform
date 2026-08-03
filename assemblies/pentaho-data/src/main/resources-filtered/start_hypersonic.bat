@Echo Off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2030-06-15
REM ******************************************************************************
setlocal
cd /D "%~dp0"
REM ---------------------------------------------
REM - Create the classpath for this application -
REM ---------------------------------------------
SET "tempclasspath="
SET "libdir=.\lib"
SET "DATA_DIR=%~dp0..\data\hsqldb"
SET "HIBERNATE_NEEDS_INIT=true"
SET "QUARTZ_NEEDS_INIT=true"
SET "JACKRABBIT_NEEDS_INIT=true"

if not exist "%libdir%" (
  echo Warning: lib directory not found at %libdir%
  echo This script should be run from the assembled package
  echo Please build the project with: mvn clean install
  exit /b 1
)

FOR /f "delims=" %%a IN ('dir "%libdir%\hsqldb*.jar" /b /a-d') DO call :addToClasspath %%a
FOR /f "delims=" %%a IN ('dir "%libdir%\sqltool*.jar" /b /a-d') DO call :addToClasspath %%a
GOTO :startApp

:addToClasspath
IF "%tempclasspath%"=="" SET "tempclasspath=%libdir%\%1"& GOTO :end
SET "tempclasspath=%tempclasspath%;%libdir%\%1"
GOTO :end

REM -----------------------
REM - Run the application -
REM -----------------------
:startApp

call set-pentaho-env.bat "%~dp0..\jre"
if "%tempclasspath%"=="" (
  echo Error: No HSQLDB or SqlTool JAR files found in %libdir%
  exit /b 1
)

echo classpath is %tempclasspath%
echo Starting HSQLDB Server...

if exist "%DATA_DIR%\hibernatedb.properties" SET "HIBERNATE_NEEDS_INIT=false"
if exist "%DATA_DIR%\quartzdb.properties" SET "QUARTZ_NEEDS_INIT=false"
if exist "%DATA_DIR%\jackrabbitdb.properties" SET "JACKRABBIT_NEEDS_INIT=false"

REM Start HSQLDB server in background (sampledata is in-memory; others are persistent)
start "HSQLDB Server" "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.Server -database.0 mem:sampledata -dbname.0 sampledata -database.1 file:"%DATA_DIR%\hibernatedb" -dbname.1 hibernate -database.2 file:"%DATA_DIR%\quartzdb" -dbname.2 quartz -database.3 file:"%DATA_DIR%\jackrabbitdb" -dbname.3 jackrabbit -port 9001

REM Wait for server to start
echo Waiting for server to start...
timeout /t 5 /nobreak

REM Load data in background
start "Load HSQLDB Data" cmd /c ""%~dp0load-hypersonic-data.bat" %HIBERNATE_NEEDS_INIT% %QUARTZ_NEEDS_INIT% %JACKRABBIT_NEEDS_INIT%"

echo Server started. Data loading in background.
exit /b 0

:end
