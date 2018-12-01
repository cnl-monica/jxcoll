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
*   [User Documentation MD](JXCOLL_USER_DOC.md)
*   [Technical Documentation MD](JXCOLL_SYSTEM_DOC.md)

**The PDF version of the documentation is available only in Slovak language:**
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
      *   memory: 512MB+ (depends on the configure cache size)
      *   size on disk: min 1 GB (depends on the volume of data to be stored in the DB as well the DB location)
      *   other: network interface card (NIC)

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

The description of the configuration file parameters are provided in the [User Documentation](JXCOLL_USER_DOC.md).

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

Change directory (cd) to the `\dist` folder and run the file using:

```bash
java -jar jxcoll.jar
```
