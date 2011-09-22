set escape on;

conn hibuser/password;

-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE MODIFY CRONSTRING VARCHAR2(256) NULL;
ALTER TABLE DATASOURCE MODIFY USERNAME VARCHAR2(50) NULL;
ALTER TABLE DATASOURCE MODIFY PASSWORD VARCHAR2(150) NULL;
ALTER TABLE DATASOURCE MODIFY QUERY VARCHAR2(100) NULL;
ALTER TABLE DATASOURCE MODIFY URL VARCHAR2(512) NOT NULL;
UPDATE DATASOURCE SET URL='jdbc:hsqldb:hsql://localhost/SampleData' WHERE NAME='SampleData' AND DRIVERCLASS='org.hsqldb.jdbcDriver' AND URL LIKE 'jdbc:hsqldb:file:%';
commit;