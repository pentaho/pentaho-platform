USE hibernate;
-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE MODIFY CRONSTRING VARCHAR(256) NULL;
ALTER TABLE DATASOURCE MODIFY USERNAME VARCHAR(50) NULL;
ALTER TABLE DATASOURCE MODIFY PASSWORD VARCHAR(150) NULL;
ALTER TABLE DATASOURCE MODIFY QUERY VARCHAR(100) NULL;
ALTER TABLE DATASOURCE MODIFY URL VARCHAR(512) NOT NULL;
UPDATE DATASOURCE SET URL='jdbc:hsqldb:hsql://localhost/SampleData' WHERE NAME='SampleData' AND DRIVERCLASS='org.hsqldb.jdbcDriver' AND URL LIKE 'jdbc:hsqldb:file:%';
commit;