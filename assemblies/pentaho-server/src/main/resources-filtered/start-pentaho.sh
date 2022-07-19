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
# Copyright 2011 - ${copyright.year} Hitachi Vantara. All rights reserved.
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
  CATALINA_OPTS="-Xms2048m -Xmx6144m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Dfile.encoding=utf8 -Djava.locale.providers=COMPAT,SPI -DDI_HOME=\"$DI_HOME\""
  export CATALINA_OPTS

  #Sets options that only get read by Java 11 to remove illegal reflective access warnings
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens=java.base/java.lang=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens=java.base/java.net=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens=java.base/java.security=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens java.base/sun.net.www.protocol.file=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
  JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED"  
  export JDK_JAVA_OPTIONS

  JAVA_HOME=$_PENTAHO_JAVA_HOME
  sh startup.sh
fi
