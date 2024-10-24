For new installation of Pentaho with postgresql, mysql, oracle, and sqlserver installs, run the sql create scripts in
the following order:

1) create_jcr_<db>.sql
2) create_quartz_<db>.sql
3) create_repository_<db>.sql



If you are upgrading from a version 10.2 or earlier to 10.2.0.1 or later, use the following
scripts to create QRTZ6_ table and to transfer your old QRTZ5 tables to QRTZ6

1) create_quartz_<db>.sql
2) migrate_old_quartz_data_<db>.sql
