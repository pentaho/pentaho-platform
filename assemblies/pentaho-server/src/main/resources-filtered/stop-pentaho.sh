#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Pentaho, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2029-07-20
# ******************************************************************************


### ====================================================================== ###
##                                                                          ##
##  Pentaho Stop Script                                                     ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname "$0"`
cd "$DIR_REL"
DIR=`pwd`
#cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv "$DIR/jre"

cd "$DIR/tomcat/bin"
JAVA_HOME=$_PENTAHO_JAVA_HOME
sh shutdown.sh
