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
##  HSQLDB Load Data Script                                                ##
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

# Check if lib directory exists
if [ ! -d "$DIR_REL/lib" ]; then
  echo "Error: lib directory not found at $DIR_REL/lib"
  echo "This script should be run from the assembled package"
  exit 1
fi

for i in `ls "$DIR_REL"/lib/hsqldb*.jar 2>/dev/null`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
for i in `ls "$DIR_REL"/lib/sqltool*.jar 2>/dev/null`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

if [ -z "$THE_CLASSPATH" ]; then
  echo "Error: No HSQLDB or SqlTool JAR files found in $DIR_REL/lib"
  exit 1
fi

echo "classpath is $THE_CLASSPATH"

# Load sampledata script
echo "Loading sampledata..."
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/sampledata,user=SA,password= "$DIR_REL"/../data/hsqldb/sampledata.script

if [ $? -eq 0 ]; then
  echo "Sampledata loaded successfully"
else
  echo "Error loading sampledata"
fi

# Load hibernate script
echo "Loading hibernate..."
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/hibernate,user=SA,password= "$DIR_REL"/../data/hsqldb/hibernate.script

if [ $? -eq 0 ]; then
  echo "Hibernate loaded successfully"
else
  echo "Warning: Error loading hibernate (optional)"
fi

# Load quartz script
echo "Loading quartz..."
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/quartz,user=SA,password= "$DIR_REL"/../data/hsqldb/quartz.script

if [ $? -eq 0 ]; then
  echo "Quartz loaded successfully"
else
  echo "Warning: Error loading quartz (optional)"
fi

echo "Data loading complete"
