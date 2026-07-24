#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2030-06-15
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

# Check if lib directory exists
if [ ! -d "$DIR_REL/lib" ]; then
  echo "Warning: lib directory not found at $DIR_REL/lib"
  echo "This script should be run from the assembled package"
  echo "Please build the project with: mvn clean install"
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

echo "Starting HSQLDB Server..."

# Persistent (file-based) data directory for HSQLDB databases.
# hibernate and quartz are file-backed so user changes survive server restarts.
# sampledata stays in-memory (read-only demo data, re-seeded on every start).
DATA_DIR="$DIR/../data/hsqldb"

# Detect first run for each persistent database, BEFORE the server creates the files.
# When the persisted database does not exist yet, its seed script is loaded once.
# On subsequent starts the seed is skipped so existing (user-updated) data is preserved.
HIBERNATE_NEEDS_INIT=false
QUARTZ_NEEDS_INIT=false
JACKRABBIT_NEEDS_INIT=false
if [ ! -f "$DATA_DIR/hibernatedb.properties" ]; then HIBERNATE_NEEDS_INIT=true; fi
if [ ! -f "$DATA_DIR/quartzdb.properties" ]; then QUARTZ_NEEDS_INIT=true; fi
if [ ! -f "$DATA_DIR/jackrabbitdb.properties" ]; then JACKRABBIT_NEEDS_INIT=true; fi

"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.Server \
  -database.0 mem:sampledata -dbname.0 sampledata \
  -database.1 file:"$DATA_DIR/hibernatedb" -dbname.1 hibernate \
  -database.2 file:"$DATA_DIR/quartzdb" -dbname.2 quartz \
  -database.3 file:"$DATA_DIR/jackrabbitdb" -dbname.3 jackrabbit \
  -port 9001 &

SERVER_PID=$!
echo "Server started with PID: $SERVER_PID"

# Wait for server to be ready
echo "Waiting for server to start..."
sleep 5

# Load data in background so it doesn't block the server
(
echo "Loading sampledata..."
"$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/sampledata,user=SA,password= "$DIR_REL"/../data/hsqldb/sampledata.script
if [ $? -eq 0 ]; then
  echo "Sampledata loaded successfully"
else
  echo "Error loading sampledata"
fi

if [ "$HIBERNATE_NEEDS_INIT" = "true" ]; then
  echo "Initializing hibernate database (first run)..."
  "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/hibernate,user=SA,password= "$DIR_REL"/../data/hsqldb/hibernate.script
  if [ $? -eq 0 ]; then
    echo "Hibernate initialized successfully"
  else
    echo "Warning: Error initializing hibernate (optional)"
  fi
else
  echo "Hibernate database already initialized; preserving existing data."
fi

if [ "$QUARTZ_NEEDS_INIT" = "true" ]; then
  echo "Initializing quartz database (first run)..."
  "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/quartz,user=SA,password= "$DIR_REL"/../data/hsqldb/quartz.script
  if [ $? -eq 0 ]; then
    echo "Quartz initialized successfully"
  else
    echo "Warning: Error initializing quartz (optional)"
  fi
else
  echo "Quartz database already initialized; preserving existing data."
fi

if [ "$JACKRABBIT_NEEDS_INIT" = "true" ]; then
  echo "Initializing jackrabbit database (first run)..."
  "$_PENTAHO_JAVA" -cp "$THE_CLASSPATH" org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/jackrabbit,user=SA,password= "$DIR_REL"/../data/hsqldb/jackrabbit.script
  if [ $? -eq 0 ]; then
    echo "Jackrabbit initialized successfully"
  else
    echo "Warning: Error initializing jackrabbit (optional)"
  fi
else
  echo "Jackrabbit database already initialized; preserving existing data."
fi
) &

echo "Server started. Data loading in background..."

# Keep the server running
wait $SERVER_PID
