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
for i in `ls "$DIR_REL"/lib/sqltool*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

echo "SHUTDOWN;" | "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/sampledata,user=SA,password= > /dev/null 2>&1

echo "SHUTDOWN;" | "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/hibernate,user=SA,password= > /dev/null 2>&1

echo "SHUTDOWN;" | "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/quartz,user=SA,password= > /dev/null 2>&1

echo "HSQLDB server stopped successfully." 
