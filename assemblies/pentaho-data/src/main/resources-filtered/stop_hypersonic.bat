@Echo Off

REM *******************************************************************************************
REM This program is free software; you can redistribute it and/or modify it under the
REM terms of the GNU General Public License, version 2 as published by the Free Software
REM Foundation.
REM
REM You should have received a copy of the GNU General Public License along with this
REM program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
REM or from the Free Software Foundation, Inc.,
REM 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
REM
REM This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
REM without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
REM See the GNU General Public License for more details.
REM
REM
REM Copyright 2011 - ${copyright.year} Hitachi Vantara. All rights reserved.
REM *******************************************************************************************

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
