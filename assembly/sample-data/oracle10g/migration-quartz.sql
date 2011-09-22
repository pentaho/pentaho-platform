set escape on;

conn quartz/password;

ALTER TABLE qrtz_job_details
    MODIFY JOB_NAME       VARCHAR2(200) NOT NULL,
    MODIFY JOB_GROUP      VARCHAR2(200) NOT NULL,
    MODIFY DESCRIPTION    VARCHAR2(250) NULL,
    MODIFY JOB_CLASS_NAME VARCHAR2(250) NOT NULL;

ALTER TABLE qrtz_job_listeners
    MODIFY JOB_NAME     VARCHAR2(200) NOT NULL, 
    MODIFY JOB_GROUP    VARCHAR2(200) NOT NULL,
    MODIFY JOB_LISTENER VARCHAR2(200) NOT NULL;
    
ALTER TABLE qrtz_triggers
    MODIFY TRIGGER_NAME VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    MODIFY JOB_NAME  VARCHAR2(200) NOT NULL, 
    MODIFY JOB_GROUP VARCHAR2(200) NOT NULL,
    MODIFY DESCRIPTION VARCHAR2(250) NULL,
    MODIFY CALENDAR_NAME VARCHAR2(200) NULL,
    ADD    PRIORITY NUMBER(13) NULL;
    
ALTER TABLE qrtz_simple_triggers
    MODIFY TRIGGER_NAME VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    MODIFY TIMES_TRIGGERED NUMBER(10) NOT NULL;

ALTER TABLE qrtz_cron_triggers
    MODIFY TRIGGER_NAME VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    MODIFY CRON_EXPRESSION VARCHAR2(120) NOT NULL;
    
ALTER TABLE qrtz_blob_triggers
    MODIFY TRIGGER_NAME VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL;
    
ALTER TABLE qrtz_trigger_listeners
    MODIFY TRIGGER_NAME  VARCHAR2(200) NOT NULL, 
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_LISTENER VARCHAR2(200) NOT NULL;
    
ALTER TABLE qrtz_calendars
    MODIFY CALENDAR_NAME  VARCHAR2(200) NOT NULL;

ALTER TABLE qrtz_paused_trigger_grps
    MODIFY TRIGGER_GROUP  VARCHAR2(200) NOT NULL;

ALTER TABLE qrtz_fired_triggers 
    MODIFY TRIGGER_NAME VARCHAR2(200) NOT NULL,
    MODIFY TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    MODIFY INSTANCE_NAME VARCHAR2(200) NOT NULL,
    MODIFY JOB_NAME VARCHAR2(200) NULL,
    MODIFY JOB_GROUP VARCHAR2(200) NULL,
    ADD    PRIORITY NUMBER(13) NOT NULL;
    
ALTER TABLE qrtz_scheduler_state 
    MODIFY INSTANCE_NAME VARCHAR2(200) NOT NULL,
    DROP COLUMN RECOVERER;

create index idx_qrtz_j_req_recovery on qrtz_job_details(REQUESTS_RECOVERY);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(NEXT_FIRE_TIME);
create index idx_qrtz_t_state on qrtz_triggers(TRIGGER_STATE);
create index idx_qrtz_t_nft_st on qrtz_triggers(NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_volatile on qrtz_triggers(IS_VOLATILE);
create index idx_qrtz_ft_trig_name on qrtz_fired_triggers(TRIGGER_NAME);
create index idx_qrtz_ft_trig_group on qrtz_fired_triggers(TRIGGER_GROUP);
create index idx_qrtz_ft_trig_nm_gp on qrtz_fired_triggers(TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_trig_volatile on qrtz_fired_triggers(IS_VOLATILE);
create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(INSTANCE_NAME);
create index idx_qrtz_ft_job_name on qrtz_fired_triggers(JOB_NAME);
create index idx_qrtz_ft_job_group on qrtz_fired_triggers(JOB_GROUP);
create index idx_qrtz_ft_job_stateful on qrtz_fired_triggers(IS_STATEFUL);
create index idx_qrtz_ft_job_req_recovery on qrtz_fired_triggers(REQUESTS_RECOVERY);

commit;
