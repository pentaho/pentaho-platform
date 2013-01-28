@echo off
setlocal
cd /D %~dp0
cscript promptuser.js //nologo //e:jscript
rem errorlevel 0 means user chose "no"
if %errorlevel%==0 goto quit
echo WScript.Quit(1); > promptuser.js

call set-pentaho-env.bat "%~dp0jre"

cd tomcat\bin
set CATALINA_HOME=%~dp0tomcat
set CATALINA_OPTS=-Xmx2048m -Xmx2048m -XX:MaxPermSize=256m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8044 -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000

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