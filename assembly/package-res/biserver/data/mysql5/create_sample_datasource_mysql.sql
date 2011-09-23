-- This script populates the repository with sample data related 
-- information including users and data sources

USE hibernate;

--  Create HSQLDB Sample Data Source

INSERT INTO DATASOURCE VALUES('SampleData',20,'org.hsqldb.jdbcDriver',5,'pentaho_user','cGFzc3dvcmQ=','jdbc:hsqldb:hsql://localhost:9001/sampledata','select count(*) from INFORMATION_SCHEMA.SYSTEM_SEQUENCES',1000);

commit;