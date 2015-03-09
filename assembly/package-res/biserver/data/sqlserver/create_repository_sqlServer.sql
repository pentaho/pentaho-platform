--Begin--
USE master
IF EXISTS(select * from sys.databases where name = N'hibernate')
DROP DATABASE hibernate
GO
CREATE DATABASE hibernate
GO
IF NOT EXISTS 
    (SELECT name  
     FROM master.sys.server_principals
     WHERE name = N'hibuser')
CREATE LOGIN hibuser WITH PASSWORD = N'password', CHECK_POLICY = OFF
GO

USE hibernate;
CREATE USER hibuser FOR LOGIN hibuser
EXEC sp_addrolemember N'db_owner', N'hibuser'
GO

--End--
