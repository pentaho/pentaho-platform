#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
#cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/jre"

if [ -f "$DIR/promptuser.sh" ]; then
  sh "$DIR/promptuser.sh"
  rm "$DIR/promptuser.sh"
fi
if [ "$?" = 0 ]; then
  cd "$DIR/tomcat/bin"
  CATALINA_OPTS="-Xms1024m -Xmx2048m -XX:MaxPermSize=256m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8044 -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  export CATALINA_OPTS
  JAVA_HOME=$_PENTAHO_JAVA_HOME
  sh startup.sh
fi
