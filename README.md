pentaho-platform
================

Overview
--------

This set of modules make up Pentaho's core platform and business analytics server.

## api
This module contains common interfaces used by the platform.  APIs for the plugin system, repository, security, and many others exist here.

## core
This module contains core implementations of platform sub-systems and utility classes.

## repository
This module contains the JCR Jackrabbit repository implementation of the pentaho platform

## scheduler
This module contains the Quartz based scheduler implementation of the pentaho platform

## extensions
This module contains a variety of capabilities used for various purposes within the platform

## user-console
This module is a GWT front end for the pentaho platform, allowing users to navigate the repository, execute and schedule content, as well as administer the platform

## assemblies
This module creates the Pentaho Server archive and contains the samples and other content needed for the Pentaho Server.


How to build
--------------

Pentaho platform uses the maven framework. 


#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

This is a maven project, and to build it use the following command

```
mvn clean install
```

Optionally you can specify -Dmaven.test.skip=true to skip the tests (even though
you shouldn't as you know)

The build result will be a Pentaho package located in *assemblies/pentaho-server/*. Then, this package can be dropped inside your system folder.


For issue tracking and bug report please use
[Jira](http://jira.pentaho.com/browse/biserver). Its master branch is built upon commit
merges in [Jenkins Continuous Integration](http://ci.pentaho.com/job/BISERVER-CE/)
