call %~dp0set-pentaho-env.bat "%~dp0jre"

call net stop %SERVICE_NAME% /Y
call sc delete %SERVICE_NAME%