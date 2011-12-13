#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Stop Script                                                     ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/jre"

cd "$DIR/tomcat/bin"
env JAVA_HOME="$_PENTAHO_JAVA_HOME" CATALINA_PID="/var/run/pentaho/[[[linuxPackage.name]]].pid" ./shutdown.sh