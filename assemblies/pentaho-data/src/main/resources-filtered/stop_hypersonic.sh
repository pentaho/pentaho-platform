#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2029-07-20
# ******************************************************************************


### ====================================================================== ###
##                                                                          ##
##  HSQLDB Start Script                                                     ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname "$0"`
cd "$DIR_REL"
DIR=`pwd`
cd -
. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/../jre"

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls "$DIR_REL"/lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/sampledata" -user "SA" -password "" 
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/hibernate" -user "SA" -password ""
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/quartz" -user "sa" -password "" 
