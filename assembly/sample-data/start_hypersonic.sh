#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  HSQLDB Start Script                                                     ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/../jre"

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls $DIR_REL/lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

"$_PENTAHO_JAVA" -cp $THE_CLASSPATH org.hsqldb.Server -database.0 $DIR_REL/hsqldb/sampledata -dbname.0 sampledata -database.1 $DIR_REL/hsqldb/hibernate -dbname.1 hibernate -database.2 $DIR_REL/hsqldb/quartz -dbname.2 quartz
