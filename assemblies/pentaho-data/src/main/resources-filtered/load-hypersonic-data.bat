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

REM Load sampledata script
echo Loading sampledata...
"%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/sampledata,user=SA,password= "%~dp0..\data\hsqldb\sampledata.script"

if %ERRORLEVEL% equ 0 (
  echo Sampledata loaded successfully
) else (
  echo Error loading sampledata
)

REM Load hibernate script
echo Loading hibernate...
"%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/hibernate,user=SA,password= "%~dp0..\data\hsqldb\hibernate.script"

if %ERRORLEVEL% equ 0 (
  echo Hibernate loaded successfully
) else (
  echo Warning: Error loading hibernate (optional)
)

REM Load quartz script
echo Loading quartz...
"%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/quartz,user=SA,password= "%~dp0..\data\hsqldb\quartz.script"

if %ERRORLEVEL% equ 0 (
  echo Quartz loaded successfully
) else (
  echo Warning: Error loading quartz (optional)
)

:end
