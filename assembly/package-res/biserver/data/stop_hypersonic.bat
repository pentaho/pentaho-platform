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
FOR %%b IN (sampledata,hibernate,quartz) DO call :runCommand %%b 
GOTO end

:runCommand

call set-pentaho-env.bat "%~dp0..\jre"

"%_PENTAHO_JAVA%" -cp %tempclasspath% org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/%1" -user "SA" -password ""
echo %command%
%command%
GOTO :end

:end
