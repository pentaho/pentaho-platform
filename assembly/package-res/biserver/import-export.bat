@echo off
setlocal
cd /D %~dp0
call "%~dp0set-pentaho-env.bat"

"%_PENTAHO_JAVA%" -classpath "%~dp0tomcat\webapps\pentaho\WEB-INF\lib\*" org.pentaho.platform.repository2.unified.importexport.Main %*
