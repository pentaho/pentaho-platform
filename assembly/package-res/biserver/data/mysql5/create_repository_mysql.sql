DROP DATABASE IF EXISTS `hibernate`;
DROP USER 'hibuser'@'localhost';

CREATE DATABASE IF NOT EXISTS `hibernate` DEFAULT CHARACTER SET latin1;

GRANT ALL ON hibernate.* TO 'hibuser'@'localhost' identified by 'password'; 

commit;
