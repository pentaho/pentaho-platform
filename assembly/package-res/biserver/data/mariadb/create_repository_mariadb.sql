CREATE DATABASE IF NOT EXISTS `hibernate` DEFAULT CHARACTER SET latin1;

USE hibernate;

GRANT ALL ON hibernate.* TO 'hibuser'@'localhost' identified by 'password'; 

commit;
