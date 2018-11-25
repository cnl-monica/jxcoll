--execute these commands from bash as postgres user:
--export PGPASSWORD="bm"
--psql < bmdb_install.sql 

--
--The entries shown by \dp are interpreted thus:
--
--rolename=xxxx -- privileges granted to a role
--        =xxxx -- privileges granted to PUBLIC
--
--            r -- SELECT ("read")
--            w -- UPDATE ("write")
--            a -- INSERT ("append")
--            d -- DELETE
--            D -- TRUNCATE
--            x -- REFERENCES
--            t -- TRIGGER
--            X -- EXECUTE
--            U -- USAGE
--            C -- CREATE
--            c -- CONNECT
--            T -- TEMPORARY
--      arwdDxt -- ALL PRIVILEGES (for tables, varies for other objects)
--            * -- grant option for preceding privilege
--
--        /yyyy -- role that granted this privilege

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

-- connect to a database (1st argument) as user (2nd argument)
-- the password is passed by the command: export PGPASSWORD="bm"
--
\connect bmdb bm;

-- new, NON-public schema for bm database
-- it is a good practice to set as username, because default search_path is $user,public (if set different name, the tables were created under public schema???!!!)
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

-- revoke all privileges on bmdb and bm schema from public and bmro user
--
REVOKE ALL ON DATABASE bmdb FROM public; -- nobody from public can connect to bmdb
REVOKE ALL ON SCHEMA bm FROM public;
REVOKE ALL ON DATABASE bmdb FROM bmro;
REVOKE ALL ON SCHEMA bm FROM bmro;
--REVOKE ALL ON SCHEMA public FROM PUBLIC; -- only working with superuser privileges

-- grant all privileges on bmdb and schema bm to bm user
-- grant connect and usage privileges to bmro 
--
GRANT ALL ON SCHEMA bm TO bm;
GRANT CONNECT ON DATABASE bmdb TO bmro;
GRANT USAGE ON SCHEMA bm TO bmro;

-- set the privileges that will be applied to objects created in the future
--
ALTER DEFAULT PRIVILEGES IN SCHEMA bm GRANT SELECT ON TABLES TO bmro;

-- create tables, indexes and sequences for bm database
--
CREATE SEQUENCE acc_seq START 1;

CREATE TABLE acc_user (
  id            int4                    not null default nextval('acc_seq'),
  name          character varying(32)   not null,
  organization  character varying(64)   not null,
  address       character varying(128)  not null,
  phone         character varying(16),
  mobile        character varying(16),
  email         character varying(64),
  ipaddresses   character varying(256)  not null,
  ico           character varying(8),
  dic           character varying(8),
  accountno     character varying(16),
  active        boolean,
  PRIMARY KEY(id)
);

CREATE SEQUENCE criteria_seq START 1;

CREATE TABLE acc_criteria (
 id                      int4                  default nextval('criteria_seq'),
 user_id                 int4                  REFERENCES acc_user ON DELETE CASCADE NOT NULL,
 sourceipaddresses       character varying(1024),
 destinationipaddresses  character varying(1024),
 protocol                character varying(3)  CONSTRAINT chck_protocol CHECK (protocol = 'tcp' OR protocol = 'udp' OR protocol = 'any'),
 sourceports             character varying(1024),
 destinationports        character varying(1024),
 dscp                    character varying(1024),
 multicast               boolean,
 rate                    real                  not null,
 priority                int4                  not null,
 PRIMARY KEY(id)
);

CREATE TABLE acc_record (
 user_id                 int4                      REFERENCES acc_user ON DELETE CASCADE NOT NULL,
 sourceipv4address       inet                      not null,
 destinationipv4address  inet                      not null,
 protocolidentifier      int2,
 sourceport              int4,
 destinationport         int4,
 ipdiffservcodepoint     int2,
 datetime                timestamp without time zone  not null,
 ismulticast             boolean,
 octettotalcount         int8                       not null,
 packettotalcount        int8                       not null,
 flowcount               int4
);

CREATE INDEX ix_datetime ON acc_record (datetime);

insert into ACC_USER values (1,'defaultUser','CNL','Letna 9','23321','0092', 'adrian.pekar@gmail.com', '192.168.1.1','234234','2342342','1/1234','true');

create table mp_desc ( descr char(255), IP_MP inet not null);

CREATE TABLE netflow (
    "IPV4_SRC_ADDR" inet NOT NULL,
    "IPV4_DST_ADDR" inet NOT NULL,
    "IPV4_NEXT_HOP" inet,
    "IN_PKTS" integer,
    "IN_BYTES" integer,
    "FIRST_SWITCHED" timestamp without time zone NOT NULL,
    "LAST_SWITCHED" timestamp without time zone NOT NULL,
    "L4_SRC_PORT" integer,
    "L4_DST_PORT" integer,
    "TCP_FLAGS" integer,
    "PROTOCOL" integer,
    "SRC_TOS" integer,
    "INPUT_SNMP" integer,
    "OUTPUT_SNMP" integer,
    ts bigint,
    pkt_id double precision,
    "SAMPLING_INTERVAL" integer,
    "SAMPLING_ALGORITHM" integer,
    "IP_MP" inet NOT NULL,
    ts_mp timestamp without time zone NOT NULL,
    nf_ver smallint NOT NULL
); 

CREATE TABLE records_main (
   RID                            bigserial NOT NULL,
   octetDeltaCount                numeric(22),
   packetDeltaCount               numeric(22),
   protocolIdentifier             int2,
   sourceTransportPort            int4,
   sourceIPv4Address              inet,
   destinationTransportPort       int4,
   destinationIPv4Address         inet,
   observationPointId             int8,
   octetTotalCount                numeric(22),
   packetTotalCount               numeric(22),
   flowStartSeconds               numeric(22),
   flowEndSeconds                 numeric(22),
   PRIMARY KEY(RID)
);

CREATE TABLE records_cnlinformationelements (
    ID                             bigserial NOT NULL,
    RID                            bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE CASCADE NOT NULL,
    roundTripTimeNanoseconds	   numeric(22),
    packetPairsTotalCount	   numeric(22),
    hostnameOrIP		   text,
    ipCount			   numeric(22),
    objectsSize			   numeric(22),
    userBrowser			   text,
    operationSystem		   text,
    debug			   numeric(22),
    PRIMARY KEY(ID)
);

CREATE TABLE records_config (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   exporterIPv4Address                      inet,
   exporterIPv6Address                      inet,
   exporterTransportPort                    int4,
   collectorIPv4Address                     inet,
   collectorIPv6Address                     inet,
   collectorInterface                       int8,
   collectorProtocolVersion                 int2,
   collectorTransportProtocol               int2,
   collectorTransportPort                   int4,
   flowKeyIndicator                         numeric(22),
   PRIMARY KEY(ID)
);

CREATE TABLE records_derived (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   ipPayloadLength                          int8,
   ipNextHopIPv4Address                     inet,
   ipNextHopIPv6Address                     inet,
   bgpSourceAsNumber                        int8,
   bgpDestinationAsNumber                   int8,
   bgpNextAdjacentAsNumber                  int8,
   bgpPrevAdjacentAsNumber                  int8,
   bgpNextHopIPv4Address                    inet,
   bgpNextHopIPv6Address                    inet,
   mplsTopLabelType                         int2,
   mplsTopLabelIPv4Address                  inet,
   mplsTopLabelIPv6Address                  inet,
   mplsVpnRouteDistinguisher                bit varying,
   PRIMARY KEY(ID)
);

CREATE TABLE records_flowCounter  (
   ID                             bigserial NOT NULL,
   RID                            bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   postOctetDeltaCount            numeric(22),
   octetDeltaSumOfSquares         numeric(22),
   postOctetTotalCount            numeric(22),
   octetTotalSumOfSquares         numeric(22),
   postPacketDeltaCount           numeric(22),
   postPacketTotalCount           numeric(22),
   droppedOctetDeltaCount         numeric(22),
   droppedPacketDeltaCount        numeric(22),
   droppedOctetTotalCount         numeric(22),
   droppedPacketTotalCount        numeric(22),
   postMCastPacketDeltaCount      numeric(22),
   postMCastOctetDeltaCount       numeric(22),
   postMCastPacketTotalCount      numeric(22),
   postMCastOctetTotalCount       numeric(22),
   tcpSynTotalCount               numeric(22),
   tcpFinTotalCount               numeric(22),
   tcpRstTotalCount               numeric(22),
   tcpPshTotalCount               numeric(22),
   tcpAckTotalCount               numeric(22),
   tcpUrgTotalCount               numeric(22),
   PRIMARY KEY(ID)
);

CREATE TABLE records_ipHeader (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   ipVersion                                int2,
   sourceIPv6Address                        inet,
   sourceIPv4PrefixLength                   int2,
   sourceIPv6PrefixLength                   int2,
   sourceIPv4Prefix                         inet,
   sourceIPv6Prefix                         inet,
   destinationIPv6Address                   inet,
   destinationIPv4PrefixLength              int2,
   destinationIPv6PrefixLength              int2,
   destinationIPv4Prefix                    inet,
   destinationIPv6Prefix                    inet,
   ipTTL                                    int2,
   nextHeaderIPv6                           int2,
   ipDiffServCodePoint                      int2,
   ipPrecedence                             int2,
   ipClassOfService                         int2,
   postIpClassOfService                     int2,
   flowLabelIPv6                            int8,
   isMulticast                              int2,
   fragmentIdentification                   int8,
   fragmentOffset                           int4,
   fragmentFlags                            int2,
   ipHeaderLength                           int2,
   ipv4IHL                                  int2,
   totalLengthIPv4                          int4,
   ipTotalLength                            numeric(22),
   payloadLengthIPv6                        int4,
   PRIMARY KEY(ID)
);

CREATE TABLE records_minMax (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   minimumIpTotalLength                     numeric(22),
   maximumIpTotalLength                     numeric(22),
   minimumTTL                               int2,
   maximumTTL                               int2,
   ipv4Options                              int8,
   ipv6ExtensionHeaders                     int8,
   tcpControlBits                           int2,
   tcpOptions                     	    numeric(22),
   PRIMARY KEY(ID)
);

CREATE TABLE records_misc (
   ID                             bigserial NOT NULL,
   RID                            bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   flowActiveTimeout              int4,
   flowIdleTimeout                int4,
   flowEndReason                  int2,
   flowDurationMilliseconds       int8,
   flowDurationMicroseconds       int8,
   flowDirection                  int2,
   PRIMARY KEY(ID)
);

CREATE TABLE records_padding (
   ID                             bigserial NOT NULL,
   RID                            bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   paddingOctets            bit varying,
   PRIMARY KEY(ID)
);

CREATE TABLE records_processCounter (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   exportedMessageTotalCount                numeric(22),
   exportedOctetTotalCount                  numeric(22),
   exportedFlowRecordTotalCount 	    numeric(22),
   observedFlowTotalCount                   numeric(22),
   ignoredPacketTotalCount                  numeric(22),
   ignoredOctetTotalCount                   numeric(22),
   notSentFlowTotalCount                    numeric(22),
   notSentPacketTotalCount                  numeric(22),
   notSentOctetTotalCount                   numeric(22),
   PRIMARY KEY(ID)
);

CREATE TABLE records_scope (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   lineCardId                               int8,
   portId                                   int8,
   ingressInterface                         int8,
   egressInterface                          int8,
   meteringProcessId                        int8,
   exportingProcessId                       int8,
   flowId                                   numeric(22),
   templateId                               int4,
   observationDomainId                      int8,
   commonPropertiesId                       numeric(22),
   PRIMARY KEY(ID)
);

CREATE TABLE records_subIpHeader            (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   sourceMacAddress                         macaddr,
   postSourceMacAddress                     macaddr,
   vlanId                                   int4,
   postVlanId                               int4,
   destinationMacAddress                    macaddr,
   postDestinationMacAddress                macaddr,
   wlanChannelId                            int2,
   wlanSSID                                 text,
   mplsTopLabelTTL                          int2,
   mplsTopLabelExp                          int2,
   postMplsTopLabelExp                      int2,
   mplsLabelStackDepth                      int8,
   mplsLabelStackLength                     int8,
   mplsPayloadLength                        int8,
   mplsTopLabelStackSection                 bit varying,
   mplsLabelStackSection2                   bit varying,
   mplsLabelStackSection3                   bit varying,
   mplsLabelStackSection4                   bit varying,
   mplsLabelStackSection5                   bit varying,
   mplsLabelStackSection6                   bit varying,
   mplsLabelStackSection7                   bit varying,
   mplsLabelStackSection8                   bit varying,
   mplsLabelStackSection9                   bit varying,
   mplsLabelStackSection10                  bit varying,
   PRIMARY KEY(ID)
);

CREATE TABLE records_timestamp (
   ID                             bigserial NOT NULL,
   RID                            bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   flowStartMilliseconds          numeric(22),
   flowEndMilliseconds            numeric(22),
   flowStartMicroseconds          numeric(22),
   flowEndMicroseconds            numeric(22),
   flowStartNanoseconds           numeric(22),
   flowEndNanoseconds             numeric(22),
   flowStartDeltaMicroseconds     int8,
   flowEndDeltaMicroseconds       int8,
   systemInitTimeMilliseconds     numeric(22),
   flowStartSysUpTime             int8,
   flowEndSysUpTime               int8,
   PRIMARY KEY(ID)
);

CREATE TABLE records_transportHeader (
   ID                                       bigserial NOT NULL,
   RID                                      bigint REFERENCES records_main ON UPDATE CASCADE ON DELETE
CASCADE NOT NULL,
   udpSourcePort                            int4,
   udpDestinationPort                       int4,
   udpMessageLength                         int4,
   tcpSourcePort                            int4,
   tcpDestinationPort                       int4,
   tcpSequenceNumber                        int8,
   tcpAcknowledgementNumber                 int8,
   tcpWindowSize                            int4,
   tcpWindowScale                           int4,
   tcpUrgentPointer                         int4,
   tcpHeaderLength                          int2,
   icmpTypeCodeIPv4                         int4,
   icmpTypeIPv4                             int2,
   icmpCodeIPv4                             int2,
   icmpTypeCodeIPv6                         int4,
   icmpTypeIPv6                             int2,
   icmpCodeIPv6                             int2,
   igmpType                                 int2,
   PRIMARY KEY(ID)
);

-- create unique tables
--

create table uniqueSourceTransportPort (
   sourceTransportPort  INT4                 not null,
   duplicateCount INT8,
   constraint PK_PORTSOURCE primary key (sourceTransportPort)
);

create table uniqueSourceIPv4Address (
   sourceIPv4Address    INET                 not null,
   duplicateCount INT8,
   constraint PK_IPV4SOURCE primary key (sourceIPv4Address)
);

create table uniqueDestinationTransportPort (
   destinationTransportPort INT4                 not null,
   duplicateCount INT8,
   constraint PK_PORTDESTINATION primary key (destinationTransportPort)
);

CREATE TABLE uniqueDestinationIPv4Address (
   destinationIPv4Address INET                 not null,
   duplicateCount INT8,
   constraint PK_IPV4DESTINATION primary key (destinationIPv4Address)
);

create table uniqueObservationPointId (
   observationPointId   INT8                 not null,
   duplicateCount INT8,
   constraint PK_OBSERVATIONPOINTID primary key (observationPointId)
);

--CREATE LANGUAGE plpgsql;

-- create function and trigger for unique values filling the unique tables
--
CREATE OR REPLACE FUNCTION process_filter() RETURNS TRIGGER AS $filter$
    BEGIN
        --
        -- Create a rows in unique tables to reflect the operation performed on records_main,
        --
	-- if operation is insert        
	IF (TG_OP = 'INSERT') THEN
            	BEGIN
			----------------------------SourPort------------------------------------
			BEGIN
				-- try to insert value XY into unique_XY table
	    			INSERT INTO uniqueSourceTransportPort SELECT NEW.sourceTransportPort, 0;
				-- if the value XY is already existing in XY unique table
				EXCEPTION WHEN UNIQUE_VIOLATION THEN
				   	-- catch the exception and increment the XY value's counter
	   				UPDATE uniqueSourceTransportPort SET duplicateCount = duplicateCount+1 WHERE (SELECT sourceTransportPort = NEW.sourceTransportPort);
			END;
			------------------------------------------------------------------------
			----------------------------ipSour--------------------------------------		
			BEGIN	
	    			INSERT INTO uniqueSourceIPv4Address SELECT NEW.sourceIPv4Address, 0;	
				EXCEPTION WHEN UNIQUE_VIOLATION THEN
			   	UPDATE uniqueSourceIPv4Address SET duplicateCount = duplicateCount+1 WHERE (SELECT sourceIPv4Address = NEW.sourceIPv4Address);
			END;
			------------------------------------------------------------------------
			----------------------------DestPort------------------------------------	
			BEGIN		
	    			INSERT INTO uniqueDestinationTransportPort SELECT NEW.destinationTransportPort, 0;
				EXCEPTION WHEN UNIQUE_VIOLATION THEN
	   				UPDATE uniqueDestinationTransportPort SET duplicateCount = duplicateCount+1 WHERE (SELECT destinationTransportPort = NEW.destinationTransportPort);
			END;
			------------------------------------------------------------------------
			----------------------------ipDest--------------------------------------
			BEGIN
	    			INSERT INTO uniqueDestinationIPv4Address SELECT NEW.destinationIPv4Address, 0;
				EXCEPTION WHEN UNIQUE_VIOLATION THEN		
				   UPDATE uniqueDestinationIPv4Address SET duplicateCount = duplicateCount+1 WHERE (SELECT destinationIPv4Address = NEW.destinationIPv4Address);
			END;
			------------------------------------------------------------------------
			----------------------------ObsPoID-------------------------------------
			BEGIN	
	    			INSERT INTO uniqueObservationPointId SELECT NEW.observationPointId, 0;
				EXCEPTION WHEN UNIQUE_VIOLATION THEN
	   				UPDATE uniqueObservationPointId SET duplicateCount = duplicateCount+1 WHERE (SELECT observationPointId = NEW.observationPointId);
			END;
			------------------------------------------------------------------------
	    		END;
            	RETURN NEW;
        END IF;
        RETURN NULL; -- result is ignored since this is an AFTER trigger
    END;
$filter$ LANGUAGE plpgsql;

CREATE TRIGGER filter
AFTER INSERT ON records_main
    FOR EACH ROW EXECUTE PROCEDURE process_filter();
