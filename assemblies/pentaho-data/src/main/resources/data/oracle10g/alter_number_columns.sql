--NOTE: the script fixes ESR-5241 issue.
--NOTE: run create_quartz_ora.sql before running this script, which
--      creates appropriate quartz tables in pentaho_tablespace

CONN quartz/password;

ALTER TABLE QRTZ5_TRIGGERS
  MODIFY (
          NEXT_FIRE_TIME NUMBER(38),
          PREV_FIRE_TIME NUMBER(38),
          PRIORITY NUMBER(38),
          START_TIME NUMBER(38),
		  END_TIME NUMBER(38),
		  MISFIRE_INSTR NUMBER(38)
         );

ALTER TABLE QRTZ5_SIMPLE_TRIGGERS
  MODIFY(
         REPEAT_COUNT NUMBER(38),
         REPEAT_INTERVAL NUMBER(38),
         TIMES_TRIGGERED NUMBER(38)
		);

ALTER TABLE QRTZ5_FIRED_TRIGGERS
  MODIFY(
         FIRED_TIME NUMBER(38),
         PRIORITY NUMBER(38)
		);

ALTER TABLE QRTZ5_SCHEDULER_STATE
  MODIFY(
         LAST_CHECKIN_TIME NUMBER(38),
         CHECKIN_INTERVAL NUMBER(38)
		);

commit;