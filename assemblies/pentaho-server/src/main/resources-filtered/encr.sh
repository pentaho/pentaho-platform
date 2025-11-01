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


BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null
java -cp "$DIR"/tomcat/webapps/pentaho/WEB-INF/lib/pentaho-encryption-support-${encryption-support.version}.jar:"$DIR"/tomcat/webapps/pentaho/WEB-INF/lib/jetty-util-${jetty.version}.jar:"$DIR"/tomcat/webapps/pentaho/WEB-INF/classes org.pentaho.support.encryption.Encr "$@"

