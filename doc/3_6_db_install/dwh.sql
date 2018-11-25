-- create a schema for user bm; the schema will also be named bm
--
CREATE SCHEMA AUTHORIZATION bm;

-- set the search_path permanently from the default schema ($user,public) to the new schema (bm)
-- without this the correct create command would look like this: create table bm.records_main....
-- or the select command: select * from bm_records_main...
-- or pqsl command: \dt bm.records_main...
-- if we miss the "bm." part, the new table would be created in public schema!
--
ALTER USER bm SET search_path TO bm;
ALTER USER bmro SET search_path TO bm;

-- revoke all privileges on bmdwh and bm schema from public and bmro user
--
REVOKE ALL ON DATABASE bmdwh FROM public; -- nobody from public can connect to bmdb
REVOKE ALL ON SCHEMA bm FROM public;
REVOKE ALL ON DATABASE bmdwh FROM bmro;
REVOKE ALL ON SCHEMA bm FROM bmro;

-- grant all privileges on bmdwh and schema bm to bm user
-- grant connect and usage privileges to bmro 
--
GRANT ALL ON SCHEMA bm TO bm;
GRANT CONNECT ON DATABASE bmdwh TO bmro;
GRANT USAGE ON SCHEMA bm TO bmro;

-- set the privileges that will be applied to objects created in the future
--
ALTER DEFAULT PRIVILEGES IN SCHEMA bm GRANT SELECT ON TABLES TO bmro;

create table observation_point (
observation_point        INT8                null,
constraint observation_point_PK primary key (observation_point));

create table time(
id_time     BIGSERIAL      not null,
minute      INT2        null,
hour        INT2        null,
day         INT2        null,
month       INT2        null,
year        INT4        null,
constraint time_PK primary key (id_time)
);

create table ip(
ip     inet      not null,
constraint ip_PK primary key (ip)
);

create table flow(
id_flow     BIGSERIAL      not null,
packetTotalCount      NUMERIC(22)        null,
octetTotalCount       NUMERIC(22)        null,
id_ip_source_FK         inet        null,
id_ip_destination_FK         inet        null,
id_time_start_FK       BIGINT        null,
id_time_end_FK          BIGINT    null,
id_observation_point_FK          BIGINT    null,
constraint flow_PK primary key (id_flow),
constraint id_ip_source_FK foreign key (id_ip_source_FK) references ip (ip) on delete restrict on update restrict,
constraint id_ip_destination_FK foreign key (id_ip_destination_FK) references ip (ip) on delete restrict on update restrict,
constraint id_time_start_FK foreign key (id_time_start_FK) references time (id_time) on delete restrict on update restrict,
constraint id_time_end_FK foreign key (id_time_end_FK) references time (id_time) on delete restrict on update restrict,
constraint id_observation_point_FK foreign key (id_observation_point_FK) references observation_point (observation_point) on delete restrict on update restrict
);
