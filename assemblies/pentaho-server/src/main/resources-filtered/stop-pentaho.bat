@echo off
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
call set-pentaho-env.bat "%~dp0jre"

cd tomcat\bin
SET "CATALINA_HOME=%~dp0tomcat"

REM Make sure we set the appropriate variable so Tomcat can start (e.g. JAVA_HOME iff. _PENTAHO_JAVA_HOME points to a JDK)
if not exist "%_PENTAHO_JAVA_HOME%\bin\jdb.exe" goto noJdk
if not exist "%_PENTAHO_JAVA_HOME%\bin\javac.exe" goto noJdk
SET "JAVA_HOME=%_PENTAHO_JAVA_HOME%"
SET "JRE_HOME="
goto start

:noJdk
REM If no JDK found at %_PENTAHO_JAVA_HOME% unset JAVA_HOME and set JRE_HOME so Tomcat doesn't misinterpret JAVA_HOME == JDK_HOME
SET "JAVA_HOME="
SET "JRE_HOME=%_PENTAHO_JAVA_HOME%"

:start
shutdown.bat
endlocal
exit
