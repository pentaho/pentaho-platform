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

SET BITS=64
SET "DI_HOME=%~dp0pentaho-solutions\system\kettle"

SET "CATALINA_OPTS=-Xms2048m -Xmx6144m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Dfile.encoding=utf8 -Djava.locale.providers=COMPAT,SPI -DDI_HOME=%DI_HOME%"
REM Add this property to change the equivalent value of "SaveOnlyUsedConnectionsToXML" property on the server. Please see JIRA PDI-20078 for more information
REM set CATALINA_OPTS=%CATALINA_OPTS% -DSTRING_ONLY_USED_DB_TO_XML=N

REM Sets options that only get read by Java 11 to remove illegal reflective access warnings
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.lang=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.io=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.net=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.security=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.util=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.reflect.misc=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.management/javax.management=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.management/javax.management.openmbean=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.math=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.base/java.nio=ALL-UNNAMED"
SET "JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"


REM Add this option to allow orc's compatibility with protobuf-java 3.25.6 libraries
SET "CATALINA_OPTS=%CATALINA_OPTS% -Dcom.google.protobuf.use_unsafe_pre22_gencode=true"


rem Make sure we set the appropriate variable so Tomcat can start (e.g. JAVA_HOME iff. _PENTAHO_JAVA_HOME points to a JDK)
if not exist "%_PENTAHO_JAVA_HOME%\bin\jdb.exe" goto noJdk
if not exist "%_PENTAHO_JAVA_HOME%\bin\javac.exe" goto noJdk
SET "JAVA_HOME=%_PENTAHO_JAVA_HOME%"
SET "JRE_HOME="
goto start

:noJdk
rem If no JDK found at %_PENTAHO_JAVA_HOME% unset JAVA_HOME and set JRE_HOME so Tomcat doesn't misinterpret JAVA_HOME == JDK_HOME
SET "JAVA_HOME="
SET "JRE_HOME=%_PENTAHO_JAVA_HOME%"

:start
call startup
:quit
endlocal
