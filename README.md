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

##### 2. Run the DEB package using the following command: 
```bash
sudo dpkg -i jxcoll_4.0.1_i386.deb 
```
### Installation by compiling the source code

##### 1. Download the source code:
```bash
wget https://github.com/cnl-monica/mybeem/archive/master.zip --no-check-certificate
```
##### 2. Unzip the code:
```bash
unzip master.zip
```
#### 3. 
cd mybeem-master/src/mybeem

###################################

#### Vlastný preklad v iných IDE
---------

Preklad programu spočíva v nakopírovaní zdrojových súborov a spustení kompilátora jazyka Java s potrebnými parametrami a parametrom classpath nastaveným na prídavné knižnice. Odporúča sa použiť váš obľúbený java IDE, kde stačí jednoducho nastaviť verziu JDK na 7.0 alebo vyššie a do cesty classpath pridať cesty ku všetkým potrebným knižniciam.


## Preklad zdrojových kódov v prostredí NetBeans
--------

   1. Používateľ si nainštaluje vývojárské prostredie NetBeans v8.0.2 a vyššie
   1. Otvorí stiahnutý repozitár ako projekt (návod vyššie) alebo pridá v NetBeans-e repozitár pre aktuálnu verziu JXColl https://git.cnl.sk/monica/slameter_collector.git 
   1. Pomocou tlačítka (Clean and Build) si preloží zdrojové súbory, výsledok (spustiteľný binárný súbor NetBeans uloží v priečinku dist pracovného priečinka programu)



## Spustenie vývojovej verzie JXColl z príkaz. riadku
Program spúšťame z priečinka `dist/` príkazom:
```bash
java -jar jxcoll.jar
```

###########################


### Runing the program

##### First, set the configuration file `/etc/jxcoll/jxcoll_config.xml`. Make sure you configure the protocol for incoming messages (Netflow/IPFIX) and the database to be used.

The description of the configuration file parameters are provided in the [User Documentation](JXCOLL_USER_DOC.md).

The program can be run using the following command:
```bash
sudo /etc/init.d/jxcolld start
```
or using the command:
```bash 
jxcoll 
```
More information on the options to run the program inculding the parameters is provided in the [User Documentation](JXCOLL_USER_DOC.md).

