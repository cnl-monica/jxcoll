# JXColl
--------

Program JXColl (Java XML Collector) slúži na zachytávanie a spracovávanie informácii
o tokoch v sieťach získané exportérom. Tvorí zhromažďovací proces meracej architektúry nástroja SLAmeter, ktorý
na základe nastavených parametrov konfiguračného súboru vie dáta získané z aktuálnej sieťovej prevádzky
ukladať do databázy alebo ich sprístupniť pomocou vlastného protokolu pre priame spracovanie (protokol ACP) používateľovi. 
Údaje uložené v databáze (MongoDB) sú určené pre neskoršie vyhodnotenie prídavnými modulmi spomínanej 
meracej architektúry a sú v súlade s požiadavkami protokolu IPFIX. JXColl tiež generuje účtovacie záznamy, 
ktoré slúžia na analýzu sieťovej hierarchie konkrétnym používateľom z hľadiska protokolov, 
portov, IP adries a časových charakteristík. 

Podľa IPFIX špecifikácie tiež nazývaný Collector. Zhromažďovač je hostiteľom jedného alebo viacerých zhromažďovacích procesov. Zhromažďovací proces prijíma záznamy o IP tokoch z jedného alebo viacerých exportovacích procesov. Zhromažďovací proces môže vykonať ľubovoľné spracovanie záznamov o IP tokoch a taktiež ukladá spracované alebo nespracované záznamy o IP tokoch do zvoleného úložiska. 

Jediný vyvíjaný druh skupinou MONICA -> **[JXColl](jxcoll)*

## Architektúra JXColl
----------------
Je znázornená na tomto [obrázku](https://git.cnl.sk/matus.husovsky/doc/raw/master/architektura_mongo.pdf). 
Jednotlivé zobrazené komponenty, resp. triedy sú opísané v [systémovej príručke](https://git.cnl.sk/monica/slameter_collector/wikis/JXCollSystemovaPrirucka401). 


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
 *   [Documentation for older versions of the tool are located here (available only in Slovak language)](https://github.com/cnl-monica/jxcoll/tree/master/doc/)

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
      * Exporter: [MyBeem](https://github.com/cnl-monica/mybeem) - JXColl depends on MyBeem, however, it should also be able to process IPFIX messages from other flow exporters (both hardware and software implementations).

## Installation on Ubuntu 14.04.2 LTS distribution
---------------------------

### 1. Install MongoDB 
First, it is necessary to import the public GPG key
```bash
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
```
Then, we add the repository
```bash
echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
```
Subsequently we update the databaze of packages
```bash
sudo apt-get update
```
Then, we install MongoDB
```bash
sudo apt-get install -y mongodb-org
```

### 2. Installation of other dependencies 
The installation of Java JRE 7 and lksctp-tools can be performed executing:
```bash
sudo apt-get install openjdk-7-jre-headless lksctp-tools
```

### 3. Installation of JXColl

#### I. Download the DEB package
```bash
sudo wget https://git.cnl.sk/monica/slameter_collector/raw/master/deb/jxcoll_4.0.1_i386.deb --no-check-certificate 
```

#### II. Run the DEB package using the following command: 
```bash
sudo dpkg -i jxcoll_4.0.1_i386.deb 
```

#### III. Set the configuration file `/etc/jxcoll/jxcoll_config.xml`. Make sure you configure the protocol for incoming messages (Netflow/IPFIX) and the database to be used.

The description of the configuration file parameters are provided in the [User Documentation](JXCOLL_USER_DOC.md).

## Runing the program
The program can be run using the following command:
```bash
sudo /etc/init.d/jxcolld start
```
or using the command:
```bash 
jxcoll 
```
More information on the options to run the program inculding the parameters is provided in the [User Documentation](JXCOLL_USER_DOC.md).

## Compilation of the source code
Instruction on how to compile the source code are provided [here](JXCOLL_COMPILE.md)
