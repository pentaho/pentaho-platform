#!/bin/bash

cd repository
ant clean-all resolve dist publish-local
cp dist/pentaho-platform-repository-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib
