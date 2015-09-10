#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/jre"
### =========================================================== ###
## Set a variable for DI_HOME (to be used as a system property)  ##
## The plugin loading system for kettle needs this set to know   ##
## where to load the plugins from                                ##
### =========================================================== ###
DI_HOME=$DIR/pentaho-solutions/system/kettle

cd "$DIR/tomcat/bin"
CATALINA_OPTS="-Xms2048m -Xmx6144m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -DDI_HOME=$DI_HOME"
env CATALINA_OPTS="$CATALINA_OPTS" JAVA_HOME="$_PENTAHO_JAVA_HOME" CATALINA_PID="/var/run/pentaho/[[[linuxPackage.name]]].pid" ./startup.sh
