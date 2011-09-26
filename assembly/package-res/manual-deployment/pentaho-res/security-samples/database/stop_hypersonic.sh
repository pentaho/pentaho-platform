#!/bin/sh
java -cp lib/hsqldb.jar org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost:9002/userdb" -user "sa" -password ""
