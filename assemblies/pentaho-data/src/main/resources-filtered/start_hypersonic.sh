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
