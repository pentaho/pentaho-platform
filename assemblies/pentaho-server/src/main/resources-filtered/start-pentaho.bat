@echo off

REM *******************************************************************************************
REM This program is free software; you can redistribute it and/or modify it under the
REM terms of the GNU General Public License, version 2 as published by the Free Software
REM Foundation.
REM
REM You should have received a copy of the GNU General Public License along with this
REM program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
REM or from the Free Software Foundation, Inc.,
REM 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
REM
REM This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
REM without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
REM See the GNU General Public License for more details.
REM
REM
REM Copyright 2011 - ${copyright.year} Hitachi Vantara. All rights reserved.
REM *******************************************************************************************

setlocal
cd /D %~dp0
cscript promptuser.js //nologo //e:jscript
rem errorlevel 0 means user chose "no"
if %errorlevel%==0 goto quit
echo WScript.Quit(1); > promptuser.js

call set-pentaho-env.bat "%~dp0jre"

cd tomcat\bin
set CATALINA_HOME=%~dp0tomcat

SET BITS=64
SET DI_HOME="%~dp0pentaho-solutions\system\kettle"

set CATALINA_OPTS=-Xms2048m -Xmx6144m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Dfile.encoding=utf8 -Djava.locale.providers=COMPAT,SPI -DDI_HOME=%DI_HOME%

rem Sets options that only get read by Java 11 to remove illegal reflective access warnings
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.lang=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.net=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.security=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens java.base/sun.net.www.protocol.file=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
set "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED"

rem Make sure we set the appropriate variable so Tomcat can start (e.g. JAVA_HOME iff. _PENTAHO_JAVA_HOME points to a JDK)
if not exist "%_PENTAHO_JAVA_HOME%\bin\jdb.exe" goto noJdk
if not exist "%_PENTAHO_JAVA_HOME%\bin\javac.exe" goto noJdk
set JAVA_HOME=%_PENTAHO_JAVA_HOME%
set JRE_HOME=
goto start

:noJdk
rem If no JDK found at %_PENTAHO_JAVA_HOME% unset JAVA_HOME and set JRE_HOME so Tomcat doesn't misinterpret JAVA_HOME == JDK_HOME
set JAVA_HOME=
set JRE_HOME=%_PENTAHO_JAVA_HOME%

:start
call startup
:quit
endlocal
