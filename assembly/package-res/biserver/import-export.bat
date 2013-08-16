@echo off
setlocal
cd /D %~dp0
call "%~dp0set-pentaho-env.bat"

"%_PENTAHO_JAVA%" -Xmx2048m -XX:MaxPermSize=256m -classpath "%~dp0tomcat\webapps\pentaho\WEB-INF\lib\*" org.pentaho.platform.plugin.services.importexport.CommandLineProcessor %*
