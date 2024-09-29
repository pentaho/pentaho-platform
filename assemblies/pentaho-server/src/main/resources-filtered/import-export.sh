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

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
#cd -

. "$DIR/set-pentaho-env.sh"
setPentahoEnv
JAVA_ADD_OPENS=""

if $($_PENTAHO_JAVA -version 2>&1 | grep "version \"1\.8\..*" > /dev/null )
then
JAVA_ADD_OPENS=""
else
# Used to allow reflective access for Java 11/17
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.lang=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.io=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.net=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.security=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.util=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.net.www.protocol.file=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.reflect.misc=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.management/javax.management=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.management/javax.management.openmbean=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/java.math=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/java.lang.Object=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.nio.ch=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/java.nio=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
fi

### =========================================================== ###
## Set a variable for DI_HOME (to be used as a system property)  ##
## The plugin loading system for kettle needs this set to know   ##
## where to load the plugins from                                ##
### =========================================================== ###
DI_HOME="$DIR"/pentaho-solutions/system/kettle

# uses Java 6 classpath wildcards
# quotes required around classpath to prevent shell expansion
"$_PENTAHO_JAVA" $JAVA_ADD_OPENS -Xmx2048m -Dfile.encoding=utf8 -DDI_HOME="$DI_HOME" -Dpentaho.disable.karaf=true -classpath "$DIR/tomcat/webapps/pentaho/WEB-INF/lib/*:$DIR/tomcat/webapps/pentaho/WEB-INF/classes" org.pentaho.platform.plugin.services.importexport.CommandLineProcessor ${1+"$@"}
