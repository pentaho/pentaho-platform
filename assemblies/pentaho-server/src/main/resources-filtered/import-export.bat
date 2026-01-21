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
call "%~dp0set-pentaho-env.bat"

SET "DI_HOME=%~dp0pentaho-solutions\system\kettle"
SET "JAVA_ADD_OPENS="
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

REM Used to allow reflective access for Java 11/17
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/java.io=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.net=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.security=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.util=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.net.www.protocol.file=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.reflect.misc=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.management/javax.management=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.management/javax.management.openmbean=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/java.math=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/java.lang.Object=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.nio.ch=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/java.nio=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED"

:SKIPADDOPENSASSIGNMENT
"%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% -Xmx2048m -Dfile.encoding=utf8 "-DDI_HOME=%DI_HOME%" -Dpentaho.disable.karaf=true -classpath "%~dp0tomcat\webapps\pentaho\WEB-INF\classes;%~dp0tomcat\webapps\pentaho\WEB-INF\lib\*" org.pentaho.platform.plugin.services.importexport.CommandLineProcessor %*

