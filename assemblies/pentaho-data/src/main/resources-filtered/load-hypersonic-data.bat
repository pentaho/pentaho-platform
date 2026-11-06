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
SET "HIBERNATE_NEEDS_INIT=%~1"
SET "QUARTZ_NEEDS_INIT=%~2"
SET "JACKRABBIT_NEEDS_INIT=%~3"

if "%HIBERNATE_NEEDS_INIT%"=="" SET "HIBERNATE_NEEDS_INIT=true"
if "%QUARTZ_NEEDS_INIT%"=="" SET "QUARTZ_NEEDS_INIT=true"
if "%JACKRABBIT_NEEDS_INIT%"=="" SET "JACKRABBIT_NEEDS_INIT=true"

if not exist "%libdir%" (
  echo Error: lib directory not found at %libdir%
  echo This script should be run from the assembled package
  exit /b 1
)

FOR /f "delims=" %%a IN ('dir "%libdir%\hsqldb*.jar" /b /a-d') DO call :addToClasspath %%a
FOR /f "delims=" %%a IN ('dir "%libdir%\sqltool*.jar" /b /a-d') DO call :addToClasspath %%a
GOTO :loadData

:addToClasspath
IF "%tempclasspath%"=="" SET "tempclasspath=%libdir%\%1"& GOTO :end
SET "tempclasspath=%tempclasspath%;%libdir%\%1"
GOTO :end

REM -----------------------
REM - Load data into databases -
REM -----------------------
:loadData

call set-pentaho-env.bat "%~dp0..\jre"
if "%tempclasspath%"=="" (
  echo Error: No HSQLDB or SqlTool JAR files found in %libdir%
  exit /b 1
)

echo classpath is %tempclasspath%

REM Load sampledata script
echo Loading sampledata...
"%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/sampledata,user=SA,password= "%~dp0..\data\hsqldb\sampledata.script"

if %ERRORLEVEL% equ 0 (
  echo Sampledata loaded successfully
) else (
  echo Error loading sampledata
)

if /I "%HIBERNATE_NEEDS_INIT%"=="true" (
  echo Initializing hibernate database ^(first run^)...
  "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/hibernate,user=SA,password= "%~dp0..\data\hsqldb\hibernate.script"
  if %ERRORLEVEL% equ 0 (
    echo Hibernate initialized successfully
  ) else (
    echo Warning: Error initializing hibernate ^(optional^)
  )
) else (
  echo Hibernate database already initialized; preserving existing data.
)

if /I "%QUARTZ_NEEDS_INIT%"=="true" (
  echo Initializing quartz database ^(first run^)...
  "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/quartz,user=SA,password= "%~dp0..\data\hsqldb\quartz.script"
  if %ERRORLEVEL% equ 0 (
    echo Quartz initialized successfully
  ) else (
    echo Warning: Error initializing quartz ^(optional^)
  )
) else (
  echo Quartz database already initialized; preserving existing data.
)

if /I "%JACKRABBIT_NEEDS_INIT%"=="true" (
  echo Initializing jackrabbit database ^(first run^)...
  "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/jackrabbit,user=SA,password= "%~dp0..\data\hsqldb\jackrabbit.script"
  if %ERRORLEVEL% equ 0 (
    echo Jackrabbit initialized successfully
  ) else (
    echo Warning: Error initializing jackrabbit ^(optional^)
  )
) else (
  echo Jackrabbit database already initialized; preserving existing data.
)

echo Data loading complete

:end
