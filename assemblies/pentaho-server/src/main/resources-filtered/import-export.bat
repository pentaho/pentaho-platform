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
call "%~dp0set-pentaho-env.bat"

SET DI_HOME="%~dp0pentaho-solutions\system\kettle"
set JAVA_ADD_OPENS=
pushd "%_PENTAHO_JAVA_HOME%"
if exist java.exe goto USEJAVAFROMPENTAHOJAVAHOME
cd bin
if exist java.exe goto USEJAVAFROMPENTAHOJAVAHOME
popd
pushd "%_PENTAHO_JAVA_HOME%\jre\bin"
if exist java.exe goto USEJAVAFROMPATH
GOTO USEJAVAFROMPATH

:USEJAVAFROMPENTAHOJAVAHOME
FOR /F %%a IN ('.\java.exe -version 2^>^&1^|%windir%\system32\find /C "version ""1.8."') DO (SET /a ISJAVA8=%%a)
GOTO CONTINUE
:USEJAVAFROMPATH
FOR /F %%a IN ('java -version 2^>^&1^|%windir%\system32\find /C "version ""1.8."') DO (SET /a ISJAVA8=%%a)
GOTO CONTINUE

:CONTINUE
popd
IF %ISJAVA8%==1 GOTO SKIPADDOPENSASSIGNMENT

REM Used to allow reflective access for Java 11
set JAVA_ADD_OPENS=--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED

:SKIPADDOPENSASSIGNMENT
"%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% -Xmx2048m -Dfile.encoding=utf8 -DDI_HOME="%DI_HOME%" -Dpentaho.disable.karaf=true -classpath "%~dp0tomcat\webapps\pentaho\WEB-INF\classes;%~dp0tomcat\webapps\pentaho\WEB-INF\lib\*" org.pentaho.platform.plugin.services.importexport.CommandLineProcessor %*

