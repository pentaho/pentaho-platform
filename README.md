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
* Java JDK 11
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

This is a maven project, and to build it use the following command

```
mvn clean install
```

Optionally you can specify -Dmaven.test.skip=true to skip the tests (even though
you shouldn't as you know)

The build result will be a Pentaho package located in *assemblies/pentaho-server/*. Then, this package can be dropped inside your system folder.

#### Running the tests

__Unit tests__

This will run all unit tests in the project (and sub-modules). To run integration tests as well, see Integration Tests below.

```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):

```
$ cd core
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```

__Integration tests__

In addition to the unit tests, there are integration tests that test cross-module operation. This will run the integration tests.

```
$ mvn verify -DrunITs
```

To run a single integration test:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>>
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>> -Dmaven.failsafe.debug
```

To skip test

```
$ mvn clean install -DskipTests
```

To get log as text file

```
$ mvn clean install test >log.txt
```


__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory

````
For issue tracking and bug report please use
[Jira](http://jira.pentaho.com/browse/biserver). Its master branch is built upon commit
merges in [Jenkins Continuous Integration](http://ci.pentaho.com/job/BISERVER-CE/)
