
CREATE DATABASE IF NOT EXISTS `jackrabbit` DEFAULT CHARACTER SET latin1;

CREATE USER 'jcr_user'@'localhost' identified by 'password';
GRANT ALL PRIVILEGES ON jackrabbit.* TO 'jcr_user'@'localhost' WITH GRANT OPTION;

commit;