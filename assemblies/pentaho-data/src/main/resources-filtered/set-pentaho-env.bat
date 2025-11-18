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

if not "%PENTAHO_JAVA%" == "" goto gotPentahoJava
SET "__LAUNCHER=java.exe"
goto checkPentahoJavaHome

:gotPentahoJava
SET "__LAUNCHER=%PENTAHO_JAVA%"
goto checkPentahoJavaHome

:checkPentahoJavaHome
if exist "%~1\bin\%__LAUNCHER%" goto gotValueFromCaller
if not "%PENTAHO_JAVA_HOME%" == "" goto gotPentahoJavaHome
if exist "%~dp0jre\bin\%__LAUNCHER%" goto gotJreCurrentFolder
if exist "%~dp0java\bin\%__LAUNCHER%" goto gotJavaCurrentFolder
if exist "%~dp0..\jre\bin\%__LAUNCHER%" goto gotJreOneFolderUp
if exist "%~dp0..\java\bin\%__LAUNCHER%" goto gotJavaOneFolderUp
if exist "%~dp0..\..\jre\bin\%__LAUNCHER%" goto gotJreTwoFolderUp
if exist "%~dp0..\..\java\bin\%__LAUNCHER%" goto gotJavaTwoFolderUp
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
goto gotPath

:gotPentahoJavaHome
echo DEBUG: Using PENTAHO_JAVA_HOME
SET "_PENTAHO_JAVA_HOME=%PENTAHO_JAVA_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreCurrentFolder
echo DEBUG: Found JRE at the current folder
SET "_PENTAHO_JAVA_HOME=%~dp0jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaCurrentFolder
echo DEBUG: Found JAVA at the current folder
SET "_PENTAHO_JAVA_HOME=%~dp0java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreOneFolderUp
echo DEBUG: Found JRE one folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaOneFolderUp
echo DEBUG: Found JAVA one folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreTwoFolderUp
echo DEBUG: Found JRE two folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\..\jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaTwoFolderUp
echo DEBUG: Found JAVA two folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\..\java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJdkHome
echo DEBUG: Using JAVA_HOME
SET "_PENTAHO_JAVA_HOME=%JAVA_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreHome
echo DEBUG: Using JRE_HOME
SET "_PENTAHO_JAVA_HOME=%JRE_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotValueFromCaller
echo DEBUG: Using value (%~1) from calling script
SET "_PENTAHO_JAVA_HOME=%~1"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotPath
echo WARNING: Using java from path
SET "_PENTAHO_JAVA_HOME="
SET "_PENTAHO_JAVA=%__LAUNCHER%"

goto end

:end

echo DEBUG: _PENTAHO_JAVA_HOME=%_PENTAHO_JAVA_HOME%
echo DEBUG: _PENTAHO_JAVA=%_PENTAHO_JAVA%
