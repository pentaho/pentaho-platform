--THIS USER IS SPECIFIC TO THE DATABASE WHERE THIS SCRIPT IS TO BE RUN AND
--IT SHOULD BE A USER WITH DBA PRIVS.
--AND ALSO @pentaho should be replaced with the correct instance name
--
--NOTE: run create_repository_ora.sql before running this script, which
--      creates the pentaho_tablespace

-- conn admin/password@pentaho

alter session set "_ORACLE_SCRIPT"=true;

declare tablespaceexists integer;
begin
select count(*) into tablespaceexists from dba_tablespaces where tablespace_name='PENTAHO_TABLESPACE';
  if (tablespaceexists = 0) then
    execute immediate 'create tablespace pentaho_tablespace logging datafile ''ptho_ts.dbf'' size 32m autoextend on next 32m maxsize 2048m extent management local';
  end if;
end;
/

declare userexist integer;
begin
  select count(*) into userexist from dba_users where username='QUARTZ';
  if (userexist = 0) then
    execute immediate 'create user quartz identified by "password" default tablespace pentaho_tablespace quota unlimited on pentaho_tablespace temporary tablespace temp quota 5M on system';
    execute immediate 'grant create session, create procedure, create table to quartz';
  end if;
end;
/

--CREATE QUARTZ TABLES

CONN quartz/password

DELETE FROM QRTZ6_FIRED_TRIGGERS;
DELETE FROM QRTZ6_SIMPLE_TRIGGERS;
DELETE FROM QRTZ6_SIMPROP_TRIGGERS;
DELETE FROM QRTZ6_CRON_TRIGGERS;
DELETE FROM QRTZ6_BLOB_TRIGGERS;
DELETE FROM QRTZ6_TRIGGERS;
DELETE FROM QRTZ6_JOB_DETAILS;
DELETE FROM QRTZ6_CALENDARS;
DELETE FROM QRTZ6_PAUSED_TRIGGER_GRPS;
DELETE FROM QRTZ6_LOCKS;
DELETE FROM QRTZ6_SCHEDULER_STATE;

DROP TABLE QRTZ6_CALENDARS;
DROP TABLE QRTZ6_FIRED_TRIGGERS;
DROP TABLE QRTZ6_BLOB_TRIGGERS;
DROP TABLE QRTZ6_CRON_TRIGGERS;
DROP TABLE QRTZ6_SIMPLE_TRIGGERS;
DROP TABLE QRTZ6_SIMPROP_TRIGGERS;
DROP TABLE QRTZ6_TRIGGERS;
DROP TABLE QRTZ6_JOB_DETAILS;
DROP TABLE QRTZ6_PAUSED_TRIGGER_GRPS;
DROP TABLE QRTZ6_LOCKS;
DROP TABLE QRTZ6_SCHEDULER_STATE;

CREATE TABLE QRTZ6_JOB_DETAILS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    JOB_NAME  VARCHAR2(200) NOT NULL,
    JOB_GROUP VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(250) NULL,
    JOB_CLASS_NAME   VARCHAR2(250) NOT NULL, 
    IS_DURABLE VARCHAR2(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR2(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR2(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR2(1) NOT NULL,
    JOB_DATA BLOB NULL,
    CONSTRAINT QRTZ6_JOB_DETAILS_PK PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);
CREATE TABLE QRTZ6_TRIGGERS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    JOB_NAME  VARCHAR2(200) NOT NULL, 
    JOB_GROUP VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(250) NULL,
    NEXT_FIRE_TIME NUMBER(13) NULL,
    PREV_FIRE_TIME NUMBER(13) NULL,
    PRIORITY NUMBER(13) NULL,
    TRIGGER_STATE VARCHAR2(16) NOT NULL,
    TRIGGER_TYPE VARCHAR2(8) NOT NULL,
    START_TIME NUMBER(13) NOT NULL,
    END_TIME NUMBER(13) NULL,
    CALENDAR_NAME VARCHAR2(200) NULL,
    MISFIRE_INSTR NUMBER(2) NULL,
    JOB_DATA BLOB NULL,
    CONSTRAINT QRTZ6_TRIGGERS_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ6_TRIGGER_TO_JOBS_FK FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) 
      REFERENCES QRTZ6_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP) 
);
CREATE TABLE QRTZ6_SIMPLE_TRIGGERS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    REPEAT_COUNT NUMBER(7) NOT NULL,
    REPEAT_INTERVAL NUMBER(12) NOT NULL,
    TIMES_TRIGGERED NUMBER(10) NOT NULL,
    CONSTRAINT QRTZ6_SIMPLE_TRIG_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ6_SIMPLE_TRIG_TO_TRIG_FK FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE QRTZ6_CRON_TRIGGERS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    CRON_EXPRESSION VARCHAR2(120) NOT NULL,
    TIME_ZONE_ID VARCHAR2(80),
    CONSTRAINT QRTZ6_CRON_TRIG_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ6_CRON_TRIG_TO_TRIG_FK FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
      REFERENCES QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE QRTZ6_SIMPROP_TRIGGERS
  (          
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    STR_PROP_1 VARCHAR2(512) NULL,
    STR_PROP_2 VARCHAR2(512) NULL,
    STR_PROP_3 VARCHAR2(512) NULL,
    INT_PROP_1 NUMBER(10) NULL,
    INT_PROP_2 NUMBER(10) NULL,
    LONG_PROP_1 NUMBER(13) NULL,
    LONG_PROP_2 NUMBER(13) NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR2(1) NULL,
    BOOL_PROP_2 VARCHAR2(1) NULL,
    CONSTRAINT QRTZ6_SIMPROP_TRIG_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ6_SIMPROP_TRIG_TO_TRIG_FK FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
      REFERENCES QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE QRTZ6_BLOB_TRIGGERS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    CONSTRAINT QRTZ6_BLOB_TRIG_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ6_BLOB_TRIG_TO_TRIG_FK FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
CREATE TABLE QRTZ6_CALENDARS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    CALENDAR_NAME  VARCHAR2(200) NOT NULL, 
    CALENDAR BLOB NOT NULL,
    CONSTRAINT QRTZ6_CALENDARS_PK PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);
CREATE TABLE QRTZ6_PAUSED_TRIGGER_GRPS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR2(200) NOT NULL, 
    CONSTRAINT QRTZ6_PAUSED_TRIG_GRPS_PK PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);
CREATE TABLE QRTZ6_FIRED_TRIGGERS 
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    ENTRY_ID VARCHAR2(95) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    INSTANCE_NAME VARCHAR2(200) NOT NULL,
    FIRED_TIME NUMBER(13) NOT NULL,
    SCHED_TIME NUMBER(13) NOT NULL,
    PRIORITY NUMBER(13) NOT NULL,
    STATE VARCHAR2(16) NOT NULL,
    JOB_NAME VARCHAR2(200) NULL,
    JOB_GROUP VARCHAR2(200) NULL,
    IS_NONCONCURRENT VARCHAR2(1) NULL,
    REQUESTS_RECOVERY VARCHAR2(1) NULL,
    CONSTRAINT QRTZ6_FIRED_TRIGGER_PK PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);
CREATE TABLE QRTZ6_SCHEDULER_STATE 
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    INSTANCE_NAME VARCHAR2(200) NOT NULL,
    LAST_CHECKIN_TIME NUMBER(13) NOT NULL,
    CHECKIN_INTERVAL NUMBER(13) NOT NULL,
    CONSTRAINT QRTZ6_SCHEDULER_STATE_PK PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);
CREATE TABLE QRTZ6_LOCKS
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    LOCK_NAME  VARCHAR2(40) NOT NULL, 
    CONSTRAINT QRTZ6_LOCKS_PK PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

INSERT INTO QRTZ6_LOCKS values('PentahoQuartzScheduler','TRIGGER_ACCESS');
INSERT INTO QRTZ6_LOCKS values('PentahoQuartzScheduler','JOB_ACCESS');
INSERT INTO QRTZ6_LOCKS values('PentahoQuartzScheduler','CALENDAR_ACCESS');
INSERT INTO QRTZ6_LOCKS values('PentahoQuartzScheduler','STATE_ACCESS');
INSERT INTO QRTZ6_LOCKS values('PentahoQuartzScheduler','MISFIRE_ACCESS');

CREATE INDEX IDX_QRTZ6_J_REQ_RECOVERY ON QRTZ6_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ6_J_GRP ON QRTZ6_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX IDX_QRTZ6_T_J ON QRTZ6_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ6_T_JG ON QRTZ6_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ6_T_C ON QRTZ6_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ6_T_G ON QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ6_T_STATE ON QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ6_T_N_STATE ON QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ6_T_N_G_STATE ON QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ6_T_NEXT_FIRE_TIME ON QRTZ6_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ6_T_NFT_ST ON QRTZ6_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ6_T_NFT_MISFIRE ON QRTZ6_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ6_T_NFT_ST_MISFIRE ON QRTZ6_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ6_T_NFT_ST_MISFIRE_GRP ON QRTZ6_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX IDX_QRTZ6_FT_TRIG_INST_NAME ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ6_FT_INST_JOB_REQ_RCVRY ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ6_FT_J_G ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ6_FT_JG ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ6_FT_T_G ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ6_FT_TG ON QRTZ6_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

commit;
