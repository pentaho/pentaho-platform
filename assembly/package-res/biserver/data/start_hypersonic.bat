@Echo Off

setlocal
cd /D %~dp0
REM ---------------------------------------------
REM - Create the classpath for this application -
REM ---------------------------------------------
SET tempclasspath=
SET libdir=.\lib

FOR /f "delims=" %%a IN ('dir %libdir%\hsqldb*.jar /b /a-d') DO call :addToClasspath %%a
GOTO :startApp

:addToClasspath
IF "%tempclasspath%"=="" SET tempclasspath=%libdir%\%1& GOTO :end
SET tempclasspath=%tempclasspath%;%libdir%\%1
GOTO :end

REM -----------------------
REM - Run the application -
REM -----------------------
:startApp

call set-pentaho-env.bat "%~dp0..\jre"

"%_PENTAHO_JAVA%" -cp %tempclasspath% org.hsqldb.Server -database.0 hsqldb\sampledata -dbname.0 sampledata -database.1 hsqldb\hibernate -dbname.1 hibernate -database.2 hsqldb\quartz -dbname.2 quartz
echo %command%
%command%
exit

:end
