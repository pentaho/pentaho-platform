--THIS USER IS SPECIFIC TO THE DATABASE WHERE THIS SCRIPT IS TO BE RUN AND IT 
--SHOULD BE A USER WITH DBA PRIVS.
--AND ALSO @pentaho should be replaced with the correct instance name

--conn admin/password@pentaho

set escape on;

create tablespace pentaho_tablespace
  logging
  datafile 'ptho_ts.dbf' 
  size 32m 
  autoextend on 
  next 32m maxsize 2048m
  extent management local;

drop user hibuser cascade;
 
create user hibuser identified by "password" default tablespace pentaho_tablespace quota unlimited on pentaho_tablespace temporary tablespace temp quota 5M on system;

grant create session, create procedure, create table, create sequence to hibuser;

--CREATE ADDITIONAL REPOSITORY TABLES

conn hibuser/password;
CREATE TABLE DATASOURCE(NAME VARCHAR2(50) NOT NULL PRIMARY KEY,MAXACTCONN INTEGER NOT NULL,DRIVERCLASS VARCHAR2(50) NOT NULL,IDLECONN INTEGER NOT NULL,USERNAME VARCHAR2(50) NULL,PASSWORD VARCHAR2(150) NULL,URL VARCHAR2(512) NOT NULL,QUERY VARCHAR2(100) NULL,WAIT INTEGER NOT NULL);

commit;