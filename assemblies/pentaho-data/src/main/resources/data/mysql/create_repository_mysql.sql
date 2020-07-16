CREATE DATABASE IF NOT EXISTS `hibernate` DEFAULT CHARACTER SET latin1;

USE hibernate;

CREATE USER 'hibuser'@'localhost' identified by 'password';
GRANT ALL PRIVILEGES ON hibernate.* TO 'hibuser'@'localhost' WITH GRANT OPTION;

commit;
