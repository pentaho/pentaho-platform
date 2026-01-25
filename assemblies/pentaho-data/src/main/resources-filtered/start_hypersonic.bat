@Echo Off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2029-07-20
REM ******************************************************************************
setlocal
cd /D "%~dp0"
REM ---------------------------------------------
REM - Create the classpath for this application -
REM ---------------------------------------------
SET "tempclasspath="
SET "libdir=.\lib"

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

REM Start HSQLDB server in background
start "HSQLDB Server" "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.Server -database.0 mem:sampledata -dbname.0 sampledata -database.1 mem:hibernate -dbname.1 hibernate -database.2 mem:quartz -dbname.2 quartz -port 9001

REM Wait for server to start
echo Waiting for server to start...
timeout /t 5 /nobreak

REM Load data in background
start "Load HSQLDB Data" cmd /c "%~dp0load-hypersonic-data.bat"

echo Server started. Data loading in background.
exit /b 0

:end
