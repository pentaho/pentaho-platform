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

commit;