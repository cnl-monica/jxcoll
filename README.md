# JXColl
--------

JXColl (Java XML Collector of IPFIX messages) represents the middle component of the SLAmeter network traffic measurement/monitoring tool. It represents the collector of an IPFIX-based network flow measurement platform. The collector serves one or more collecting processes. This process receives records about IP flows from one or more exporters. The architecture of JXColl is as follows:

<p align="center">
  <img src="/fig/jxcoll.png" width="410" title="Architecture of the collector">
</p>

JXColl, based on the configured mode, can either store the obtained IPFIX flow records in a database or can send it directly (using the ACP protocol) for direct processing and visualisation. The data stored in the database (MongoDB) is destined for analysis of historical data. This is performed in full conformity with the [IPFIX specification](https://tools.ietf.org/html/rfc7011).

JXColl is also capable of generating accounting-related information. These information can be used for billing based on the used protocol types, IP addresses and time-base characteristics.

*  **Version:** 4.0.1 
*  **Version state:** stable, **the development was concluded in 2015**
*   **Developers:**
      * Pavol Beňko
      * Matúš Husovský
      * Marek Marcin
      * Samuel Tremko
      * Adrián Pekár
      * Tomáš Vereščák
      * Tomáš Baksay
      * Jakub Vargosko
      * Michal Kaščák
      * Ľuboš Koščo
        
*   **License**: GNU GPLv3
*   **Implementation environment**: openjdk-7-jre-headless 

## Documentation

**The documentation is available only in Slovak language:**
 * [User Documentation PDF](https://github.com/cnl-monica/jxcoll/tree/master/doc/JXColl_v4.0.1_PP.pdf)
 * [Technical Documenation PDF](https://github.com/cnl-monica/jxcoll/tree/master/doc/JXColl_v4.0.1_SP.pdf)

## Other useful documents
------------------------------------------------
 *   [Tutorial on creating a DEB installation package for JXColl](DEB_TUTORIAL.md)
 *   [Documentation for older versions of the tool are located here](https://github.com/cnl-monica/jxcoll/tree/master/doc/) **(available only in Slovak language)**

## System Requirements
-----------------------
* **Operating System:** GNU/Linux *i386* or *amd64* architecture

*  **Hardware**:
      *   processor: 1GHz+ (depends on the traffic load to measure)
      *   memory: 512MB+ (depends on the configure cache size) - the memory requirements are based on the number of concurent monitorings through the ACP protocol (direct transfer of information to the analysing application). For standard run, JXColl requires at least 120 MB memory, however, with ACP and the modul for one-way delay measurement the recommended memory is at least 512 MB.
      *   size on disk: min 1 GB (depends on the volume of data to be stored in the DB as well the DB location) - The installed program on disk occupies approximately 2.3 MB. The JXColl daemon logs into /var/log/jxcoll/ and based on the configured level of logging to ALL or DEBUG these log messages can have a substantial size. After the size of the log message reaches 100 MB, the contain is archived and compressed. The last 10 rotations are archived (1 GB of log output).
      *   other: network interface card (NIC)

**Note that monitoring a large-scale network requires larger system requirements**


*  **Software**:
      *   MongoDB
      *   Java Runtime Environment (JRE) v1.7.03
      *   lksctp-tools
      
* **Dependencies within SLAmeter**
      *   **Exporter:** JXColl depends on [MyBeem](https://github.com/cnl-monica/mybeem), however, it should also be able to process IPFIX messages from other flow exporters (both hardware and software implementations).

## Installation on Ubuntu 14.04.2 LTS
---------------------------

### Install MongoDB 

##### 1. First, it is necessary to import the public GPG key
```bash
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
```
##### 2. Then, we add the repository
```bash
echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
```
##### 3. Subsequently we update the databaze of packages
```bash
sudo apt-get update
```
##### 4. Then, we install MongoDB
```bash
sudo apt-get install -y mongodb-org
```

### Installation of other dependencies 

The installation of Java JRE 7 and lksctp-tools can be performed executing:
```bash
sudo apt-get install openjdk-7-jre-headless lksctp-tools
```

### Installation of JXColl using the .deb installation package

##### 1. Download the DEB package
```bash
sudo wget https://github.com/cnl-monica/jxcoll/blob/master/deb/jxcoll_4.0.1_i386.deb --no-check-certificate 
```

##### 2. Install the DEB package using the following command: 
```bash
sudo dpkg -i jxcoll_4.0.1_i386.deb 
```

## Set the configuration file
---------------------------

Before running the program set the configuration file `/etc/jxcoll/jxcoll_config.xml`. Make sure you configure the protocol for incoming messages (Netflow/IPFIX) and the database to be used.

The description of the configuration file parameters are provided below.

## Runing the program using the script
---------------------------

The program can be run using the following command:
```bash
sudo /etc/init.d/jxcolld start
```
or using the command:
```bash 
jxcoll 
```

## Run the program using the .jar file without installation
---------------------------

The program can be also run without installation. The .jar file is located in the `/dist` folder including all the libraries necessary for running the program. The entire distribution of JXColl can be downloaded using:

```bash
wget https://github.com/cnl-monica/jxcoll/archive/master.zip --no-check-certificate
```

Unzip the package:

```bash
unzip master.zip
```

If you want to run the program using the .jar file, you need to know how to set the path to the libraries required by JXColl. The required libraries are provided in the `\dist` directory.

Tu run the program, change directory (cd) to the `\dist` folder and run the file using:

```bash
java -jar jxcoll.jar
```

Before running the program, it is necessary to copy `ipfixFields.xml` (the file specifying the information elements that are supported by JXColl) and `jxcoll_config.xml` (the configuration file) to the appropriate locations.

## How to use the program
--------------

JXColl is a console application. When installed using the DEB package, JXColl is run automatically during system boot as a daemon. The JXColl daemon (jxcolld) can be managed using the following command:

```bash
sudo /etc/init.d/jxcolld <command> 
```

where **command** can be one of the following :

   * **start** if not running yet, this starts the JXColl daemon,
   * **stop** exits the operation of the JXColl daemon,
   * **restart** stops and starts the JXColl daemon,
   * **status** shows the status (running/not running) of the JXColl daemon,
   * **usage / help** shows information about how to operate the JXColl daemon.

After install, the JXColl daemon is not running. It will be start automatically after system reboot. Alternatively, using the above provided command one can start the daemon. The program logs are saved in the following file:

```bash
/var/log/jxcoll/YYYYMMDD-HHmmss/jxcoll.log 
```

where **Y** - year, **M** - month, **D** - day, **H** - hour, **m** - minute, **s** - second of the started JXColl daemon.

When JXColl was installed using the DEB package, it can be also run using the following command:

```bash
jxcoll [--logtofile] 
```
`--logtofile` is an optional parameter. If it is provided, the outputs of the program will be redirected into the specified file. Note that if the user running this command is not root, `sudo` privilige will be required:  

```
sudo jxcoll [--logtofile] 
```

As most of the applications in the Linux operating system, JXColl also has a manual page (man) that can be shown using the following command:

```bash
man jxcoll 
```
a man page is also provided for the configuration file:

```bash
man jxcoll_config 
```
In the case of operating systems other than Ubuntu/Debian, or if manual start of the program is required, JXColl can be run using the Java interpreter with the optional parameter consisting of the path (relative or absolute) to the configuration file:

```bash
java -jar jxcoll.jar [/path/to/the/config/jxcoll_config.xml] [--logtofile] 
```
If the path to the configuration file is not provided, the program will automatically assume the use of the configuration file located at:

```bash
/etc/jxcoll/jxcoll_config.xml 
```
If the configuration file is not found, the application will exit with an error message.

A further requirement for running JXColl is the `ipfixFields.xml` file. The path to this file can be set in the configuration `jxcoll_config` file. When at program start the `ipfixFields.xml` file is not located at the location as defined in the configuration file, the program exits with an error message. When the path to this file is not set, JXColl automatically assumes that the file is located at `/etc/jxcoll/ipfixFields.xml`. If the file cannot be located here either, JXColl stops it operation. Withouth this JXColl cannot detect the data obtained from the IPFIX messages.

## Description of the dialog beteen the program and the user
-----------------

As JXColl is a console application, it does not provide any graphical user interface. The error messages are shown in the same console window as the program was run. Alternatively, the outputs are redirected into the log file if it was set so in the configuration file.

The program can be stopped using the `CTRL + C` keys or sending the SIGTERM or SIGINT signals to the PID of the program:

```bash
kill -SIGTERM pid_of_jxcoll 
```
When the program was installed on Ubuntu/Debian operating systems using the DEB package, JXColl can be stopped using the `init.d` script (see above). 

## Description of the configuration file
----------------------

The configuration file is passed to the program as a parameter. The individual configuration parameters are grouped according to the modules they affect. These parameter with their default and valid values as well as description are provided in the tables below. 

If any of the parameters are not configured (left blank), the parameter is automatically set to the default value. 
The parameter is given in the following format: 

```
< name_of_parameter > value < \name_of_parameter >
```

The configuration file can also contain comments:
```
< ! -- comment -- >
```
As the parameters are passed to the program as plain text, the administrator must make sure the passwords are secured.

### Global parameters

| **Parameter** | **Default value** | **Valid values** | **Description** |
|--------|--------|--------|--------|
| logLevel | ERROR| ALL, DEBUG, INFO, WARN, TRACE, ERROR, FATAL, OFF | level of logging |
| ipfixFiledsXML | /etc/jxcoll/ipfixFields.xml | valid path | path to the XML file containing the configuration |
| ipfixTemplateTimeout | 300 | integer larger than 0 | time after the expiration of which the IPFIX template is considered as invalid |
| listenPort | 9996 | integer in the range of <0-65535> (that is available) | port for listening for incoming IPFIX messages |
| receiveUDP | no | yes, no | receiving IPFIX messages using UDP |
| receiveTCP | no | yes, no | receiving IPFIX messages using TCP |
| receiveSCTP | no | yes, no | receiving IPFIX messages using SCTP |
| maxConnections | 10 | 1-20 | max. number of connections (only for TCP or SCTP) |
| *Moduel: Module for synchronising the exporters (sync)* ||||
| makeSync | no | yes, no | parameter defines whether the collector acts as a snychronisation server towards the exporters (required for the OWD measurement)  |
| listenSynchPort | 5544 | integer in the range of <0-65535>  (that is available) | port for listening for synchronisation messages |

### Module for measuring one-way delay (owd)

| **Parameter** | **Default value** | **Valid values** | **Description** |
| -------- |--------|--------|--------|
| measureOwd | no | yes , no | turn the measurement of OWD on/off |
| owdStart_ObservationPointTemplateID | 256 | identifier of the template that is in conformity with IPFIX | ID of the template that is used for the start of OWD measurement |
| owdStart_ObservationDomainID | 0 | identifier of the domain that is in conformity with IPFIX | ID of the domain where OWD measurement starts |
| owdStart_Host | 127.0.0.1 | FQDN or IP | FQDN or IP of the observation point |
| owdStart_ObservationPointID | 123 | identifier of the observation point that is in conformity with IPFIX | ID of the observation point that is used for the start of OWD measurement |
| owdEnd_ObservationPointTemplateID | 257 | identifier of the template that is in conformity with IPFIX | ID of the template that is used for the end of OWD measurement |
| owdEnd_ObservationDomainID | 0 | identifier of the domain that is in conformity with IPFIX | ID of the domain where OWD measurement ends |
| owdEnd_Host | 127.0.0.1 | FQDN or IP | FQDN or IP of the observation point |
| owdEnd_ObservationPointID | 321 | identifier of the observation point that is in conformity with IPFIX | ID of the observation point that is used for the end of OWD measurement |
| passiveTimeout | 5000 | integer larger than 0 | passiveTimeout, that is set on the exporters |
| activeTimeout | 10000 | integer larger than 0 | activeTimeout, that is set on the exporters |

### Module for sending flow data directly to the analysing application (acp)

| **Parameter** | **Default value** | **Valid values** | **Description** |
| -------- |--------|--------|--------|
| acpTransfer | no | yes, true, no, false | sets the direct transmission of flow data to the analysing application |
| acpPort | 2138 | integer in the range of <0-65535> (that is available) | port, that runs the service for ACP |
| acpLogin| bm | user name | user name for the connection |
| acpPassword | bm | password | password for the connection |

### Module for storing flow data in the database

| **Parameter** | **Default value** | **Valid values** | **Description** |
| -------- |--------|--------|--------|
| dbExport | yes | yes, true, no, false | Data export to DB is on/off |
| dbHost | localhost | FQDN or IP of the dabase | The address of the DB server |
| dbPort | 5432 | port on the DB listens for connections | port on which the DB server runs |
| dbName | bm | name of the database | name of the DB where the data is going to be stored |
| dbLogin | bm | user name | user name of the DB connection |
| dbPassword | bm | password | password for the DB connection |

### Module used for accounting

| **Parameter** | **Default value** | **Valid values** | **Description** |
| -------- |--------|--------|--------|
| accExport | no | yes, true, no, false | sending accounting related information to postgresql is on/off |
| AccRecordExportInterval | 60 | integer larger than 0 | time after the expiration of which the information are stored in the database |
| collectorID | 1 | integer larger than 0 | ID of the collector |

## Description of messages for system programmer
-----------------------

During program run various messages at various levels are shown on the output. These messages can vary from errors to information. 
The log subsystem of the program can be configured at verious levels. These levels and their description is provided in the table below. Each level contain levels at lower layers. For example, for ERROR level, messages of type FATAL will be also shown. For production, ERROR is the suggested level of logging.

| **Message Type** | **Description** |
| -------- | -------- |
| ALL | all types of messages are shown |
| DEBUG | shows complex messages in detail for troubleshooting and debugging |
| INFO | program informs about its operation |
| WARN | messages related to possible errors or wrong interpretation of input data |
| TRACE | messages related to the program state |
| ERROR | messages related to data |
| FATAL | messages that raise doe to the crash of the program and that require program restart |
| OFF | turns off all log messages |

## Error messages
----------------------

When running the program, the following error messages can raise.

--------------

#### **Error:**

```bash
[main] DEBUG sk.tuke.cnl.bm.JXColl.export.DBExport - Connecting to postgres@jdbc:postgresql://127.0.0.3:5432/bm...
```

```bash
[main] ERROR sk.tuke.cnl.bm.JXColl.export.DBExport - Connection refused. 
Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
```

```bash
[main] INFO sk.tuke.cnl.bm.JXColl.export.DBExport - Login failed. org.postgresql.util.PSQLException: Connection refused. 
Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections. SQL error
```

```bash
[main] INFO sk.tuke.cnl.bm.JXColl.export.DBExport - Login failed. org.postgresql.util.PSQLException: 
FATAL: password authentication failed for user ”postgres” SQL error
```

#### **Description and resolution:**

JXColl was unable to connect to the database.
Check the connection string (address, port, user name and password). Also check blocking at the level of firewall/ACL.

--------------

#### **Error:**

```bash
[main] INFO sk.tuke.cnl.bm.JXColl.Config - Loading config file: /zla/cesta/k/jxcoll.conf
[main] ERROR sk.tuke.cnl.bm.JXColl.Config - Could not load property file: /zla/cesta/k/jxcoll.conf !
```

#### **Description and resolution:**

The program cannot read the configuration file. Check whether the `/etc/jxcoll/jxcoll.conf` exists or the path to this file is correct.

--------------

#### **Error:**

```bash
[main] FATAL sk.tuke.cnl.bm.JXColl.IpfixElements - XML file ”/path/to/the/ipfixFields.xml” was not found!
[main] FATAL sk.tuke.cnl.bm.JXColl.JXColl - JXColl could not start because of an error while processing XML file!
```

#### **Description and resolution:**

The `ipfixFields.xml` file was not found. Check whether the path set in the configuration file is correct or in the default directory (`/etc/jxcoll/ipfixFields.xml`).

--------------

#### **Error:**

```bash
[ACP Thread 4] ERROR sk.tuke.cnl.bm.JXColl.export.ACPIPFIXWorker - IO EXCEPTION :null
[ACP Thread 4] DEBUG sk.tuke.cnl.bm.JXColl.export.ACPIPFIXWorker - Closing connection in try-catch
```

#### **Description and resolution:**

This messages is related to the ACP protocol. JXColl will automatically recover and await connection through ACP.

--------------

#### **Error:**

```bash
[[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.export.DBExport - Check if is DB connected failed: java.lang.NullPointerException
```

#### **Description and resolution:**

During data processing the was a connection interruption with the DB. Check network connectivity.

--------------

#### **Error:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Element with ID: 74 is not supported, skipped! Update XML file!
```

#### **Description and resolution:**

During data processing, an information element was found that is not supported. JXColl will skip processing this information element. If the procsseing of this information element should be supported by JXColl, check whether it is provided in the `ipfixFields.xml` file. If the element is not supported, it is necessary to implement it.

--------------

#### **Error:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - i.e. ’icmpTypeCodeIPv6’ (unsigned16) - received data has wrong datatype! (4 bytes)
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Skipping this element DB exportation!
```

#### **Description and resolution:**

During data processing the size of the information element is not corresponding with the size provided in the `ipfixFields.xml` file. JXColl will skip the processing of this element. The error is most probably in the exporter.

--------------

#### **Error:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - ”i.e. ’mplsLabelStackSection5’ - Cannot decode datatype: octetArray
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Skipping this element DB exportation!
```

#### **Description and resolution:**

During data processing, the data type of the information element cannot be decoded by JXColl. JXColl will skip processing this element. The implementation of decoding this data type must be performed in the JXColl.

--------------

## Error messages related to the Java Virtual Machine 
-----------------------------

The program is interpreted in the Java Virtual Machine (JVM). Errors, that can raise and are not catched correctly by the program are errors, that were not expected and are easily identified in logger style usually containing `Java Error` and `Java Exception` strings. Usually, part of the buffer is also shown on the output. In general, these are three lines in the hierarchy of the called method that failed. Such errors usually raise due to program function crash and JXColl must be restarted. This type of error can be fixed only in the source code and is considered as programming error.

