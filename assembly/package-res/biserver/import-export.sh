#!/bin/sh
DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"
setPentahoEnv

# uses Java 6 classpath wildcards
# quotes required around classpath to prevent shell expansion
"$_PENTAHO_JAVA" -classpath "$DIR/resource/config:$DIR/jdbc/*:$DIR/lib/*" org.pentaho.platform.repository2.unified.importexport.Main $@
