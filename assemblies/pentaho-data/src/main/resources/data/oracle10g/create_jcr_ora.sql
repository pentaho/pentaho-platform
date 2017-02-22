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

drop user jcr_user cascade;

create user jcr_user identified by "password" default tablespace pentaho_tablespace quota unlimited on pentaho_tablespace temporary tablespace temp quota 5M on system;

grant create session, create procedure, create table, create trigger, create sequence to jcr_user;

--CREATE ADDITIONAL REPOSITORY TABLES

conn jcr_user/password;
commit;