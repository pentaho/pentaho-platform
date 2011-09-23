--
-- note: this script assumes pg_hba.conf is configured correctly
--

-- \connect postgres postgres

drop database if exists hibernate;
drop user if exists hibuser;

CREATE USER hibuser PASSWORD 'password';

CREATE DATABASE hibernate WITH OWNER = hibuser ENCODING = 'UTF8' TABLESPACE = pg_default;

GRANT ALL PRIVILEGES ON DATABASE hibernate to hibuser;

\connect hibernate hibuser

begin;

DROP TABLE IF EXISTS DATASOURCE;

CREATE TABLE DATASOURCE(NAME VARCHAR(50) NOT NULL PRIMARY KEY,MAXACTCONN INTEGER NOT NULL,DRIVERCLASS VARCHAR(50) NOT NULL,IDLECONN INTEGER NOT NULL,USERNAME VARCHAR(50) NULL,PASSWORD VARCHAR(150) NULL,URL VARCHAR(512) NOT NULL,QUERY VARCHAR(100) NULL,WAIT INTEGER NOT NULL);

commit;