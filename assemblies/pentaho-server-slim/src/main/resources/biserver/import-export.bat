@echo off

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
REM Copyright 2011 - 2018 Hitachi Vantara.  All rights reserved.
REM *******************************************************************************************

setlocal
cd /D %~dp0
call "%~dp0set-pentaho-env.bat"

"%_PENTAHO_JAVA%" -Xmx2048m -XX:MaxPermSize=256m -Dfile.encoding=utf8 -classpath "%~dp0tomcat\webapps\pentaho\WEB-INF\lib\*" org.pentaho.platform.plugin.services.importexport.CommandLineProcessor %*
