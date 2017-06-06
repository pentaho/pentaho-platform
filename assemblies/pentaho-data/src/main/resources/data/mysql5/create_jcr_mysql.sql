
CREATE DATABASE IF NOT EXISTS `jackrabbit` DEFAULT CHARACTER SET latin1;

grant all on jackrabbit.* to 'jcr_user'@'localhost' identified by 'password';

commit;