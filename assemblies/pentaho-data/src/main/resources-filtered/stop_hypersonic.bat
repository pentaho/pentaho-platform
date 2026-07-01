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
FOR %%b IN (sampledata,hibernate,quartz) DO (
  echo SHUTDOWN; | "%_PENTAHO_JAVA%" -cp "%tempclasspath%" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/%%b,user=SA,password= > nul 2>&1
)

echo HSQLDB server stopped successfully.

:end
