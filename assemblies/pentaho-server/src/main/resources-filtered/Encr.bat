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
pushd %~dp0
SET STARTTITLE="Encr"
SET SPOON_CONSOLE=1
set JAVA_TOOL_OPTIONS=
java -cp tomcat/webapps/pentaho/WEB-INF/classes;tomcat/webapps/pentaho/WEB-INF/lib/pentaho-encryption-support-${encryption-support.version}.jar;tomcat/webapps/pentaho/WEB-INF/lib/jetty-util-${jetty.version}.jar org.pentaho.support.encryption.Encr %*
popd
