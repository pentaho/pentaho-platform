#!/bin/sh

# *******************************************************************************************
# This program is free software; you can redistribute it and/or modify it under the
# terms of the GNU General Public License, version 2 as published by the Free Software
# Foundation.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
# or from the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
#
#
# Copyright 2011 - 2018 Hitachi Vantara.  All rights reserved.
# *******************************************************************************************

### ====================================================================== ###
##                                                                          ##
##  Hitachi Vantara Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
#cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/jre"

### =========================================================== ###
## Set a variable for DI_HOME (to be used as a system property)  ##
## The plugin loading system for kettle needs this set to know   ##
## where to load the plugins from                                ##
### =========================================================== ###
DI_HOME="$DIR"/pentaho-solutions/system/kettle

errCode=0
if [ -f "$DIR/promptuser.sh" ]; then
  sh "$DIR/promptuser.sh"
  errCode="$?"
  rm "$DIR/promptuser.sh"
fi
if [ "$errCode" = 0 ]; then
  cd "$DIR/tomcat/bin"
  ## Temporary credentials, replace with correct values
  CREDENTIALS=-Drepos.url=http://localhost:8080/pentaho -Drepos.user=admin -Drepos.password=password -Dcda.hostname=localhost -Dcda.port=8080
  CATALINA_OPTS="-Xms32m -Xmx128m -Dfile.encoding=utf8 -DDI_HOME=\"$DI_HOME\" $CREDENTIALS"
  export CATALINA_OPTS
  JAVA_HOME=$_PENTAHO_JAVA_HOME
  sh startup.sh
fi
