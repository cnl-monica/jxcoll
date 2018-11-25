-- create main role with login privilege, privileges to create databases, roles and with a password in encrypted format
-- is good practice to create a role that has the CREATEDB and CREATEROLE privileges, but is not a superuser, and then use this role for all routine management of databases and roles. This approach avoids the dangers of operating as a superuser for tasks that do not really require it.
--
CREATE ROLE bm LOGIN CREATEDB CREATEROLE ENCRYPTED PASSWORD 'bm';

-- create role for read only user; with encrypted password
--
CREATE ROLE bmro LOGIN ENCRYPTED PASSWORD 'bmro';

-- create a database owned by a user
--
CREATE DATABASE bmdb WITH OWNER bm;

-- create a database owned by a user
--
CREATE DATABASE bmdwh WITH OWNER bm;

