call %~dp0delete-service.bat
set SERVICE_INSTALLER=%CATALINA_HOME%\bin\service.bat

call net stop %SERVICE_NAME% /Y
call sc delete %SERVICE_NAME%
call %SERVICE_INSTALLER% install %SERVICE_NAME%
call %CATALINA_HOME%\bin\tomcat6.exe //US//%SERVICE_NAME% ++JvmOptions %CATALINA_OPTS: =;%
call net start %SERVICE_NAME%
pause