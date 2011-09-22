For mysql5, oracle10g, and postgresql Installs, run the sql create scripts in
the following order:

create_repository_<db>.sql
create_quartz_<db>.sql

If you would like to create the sample data source connection, run the following script after running the create script:

create_sample_datasource_<db>.sql

If you want to use an existing 1.7.0 Pentaho rdbms repository database, run the following script:

migration.sql