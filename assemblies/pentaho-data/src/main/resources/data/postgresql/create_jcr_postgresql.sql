--
-- note: this script assumes pg_hba.conf is configured correctly
--

-- \connect postgres postgres

drop database if exists jackrabbit;
drop user if exists jcr_user;

CREATE USER jcr_user PASSWORD 'password';

CREATE DATABASE jackrabbit WITH OWNER = jcr_user ENCODING = 'UTF8' TABLESPACE = pg_default;

GRANT ALL PRIVILEGES ON DATABASE jackrabbit to jcr_user;
