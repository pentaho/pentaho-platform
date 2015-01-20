USE master
IF EXISTS(select * from sys.databases where name = N'jackrabbit')
DROP DATABASE jackrabbit
GO
CREATE DATABASE jackrabbit
GO

IF NOT EXISTS 
    (SELECT name  
     FROM master.sys.server_principals
     WHERE name = N'jcr_user')
CREATE LOGIN [jcr_user] WITH PASSWORD = N'password', CHECK_POLICY = OFF
GO

USE jackrabbit;
CREATE USER jcr_user FOR LOGIN jcr_user
EXEC sp_addrolemember N'db_owner', N'jcr_user'
GO
