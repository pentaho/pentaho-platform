For postgresql, mysql, oracle, and sqlserver installs, run the sql create scripts in
the following order:

create_jcr_<db>.sql
create_quartz_<db>.sql
create_repository_<db>.sql

If you are upgrading from a version 10.2 or earlier to 10.2.0.1 or later, use the following
script to transfer your old QRTZ5 tables to QRTZ6

migrate_old_quartz_data_<db>.sql
