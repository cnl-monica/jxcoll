# JXColl
--------

*  **Verzia**: 4.0.1 
*  **Stav verzie**: vyvíjaná
*   [Dokumentácia k vytváraniu deb balíkov pre kolektor](create_debian)
*   **Autori**:
      * Pavol Beňko
      * Matúš Husovský
      * Marek Marcin - bývalý riešiteľ
      * Samuel Tremko - bývalý riešiteľ
      * Adrián Pekár - bývalý riešiteľ
      * Tomáš Vereščák - bývalý riešiteľ
      * Tomáš Baksay - bývalý riešiteľ
      * Jakub Vargosko - bývalý riešiteľ
      * Michal Kaščák - bývalý riešiteľ
      * Ľuboš Koščo - bývalý riešiteľ
        
*   **Licencia**: GNU GPLv3
*   **Implemetačné prostredie**: openjdk-7-jre-headless 
*   [Systémová príručka WIKI](JXCollSystemovaPrirucka401)
*   [Používateľská príručka WIKI](JXCollPouzivatelskaPriruckav401)
*   [Používateťská príručka PDF](https://git.cnl.sk/monica/slameter_collector/raw/master/doc/JXColl_v4.0.1_PP.pdf)
*   [Systémová príručka PDF](https://git.cnl.sk/monica/slameter_collector/raw/master/doc/JXColl_v4.0.1_SP.pdf)

 
## Stručný opis
----------------

Program JXColl (Java XML Collector) slúži na zachytávanie a spracovávanie informácii
o tokoch v sieťach získané exportérom. Tvorí zhromažďovací proces meracej architektúry nástroja SLAmeter, ktorý
na základe nastavených parametrov konfiguračného súboru vie dáta získané z aktuálnej sieťovej prevádzky
ukladať do databázy alebo ich sprístupniť pomocou vlastného protokolu pre priame spracovanie (protokol ACP) používateľovi. 
Údaje uložené v databáze (MongoDB) sú určené pre neskoršie vyhodnotenie prídavnými modulmi spomínanej 
meracej architektúry a sú v súlade s požiadavkami protokolu IPFIX. JXColl tiež generuje účtovacie záznamy, 
ktoré slúžia na analýzu sieťovej hierarchie konkrétnym používateľom z hľadiska protokolov, 
portov, IP adries a časových charakteristík. 

## Architektúra JXColl
----------------
Je znázornená na tomto [obrázku](https://git.cnl.sk/matus.husovsky/doc/raw/master/architektura_mongo.pdf). 
Jednotlivé zobrazené komponenty, resp. triedy sú opísané v [systémovej príručke](https://git.cnl.sk/monica/slameter_collector/wikis/JXCollSystemovaPrirucka401). 

## Technické požiadavky pre inštaláciu distribučnej verzie
----------------
* **Operačný systém**: Ubuntu 14.04.2 LTS (je možné použiť aj inú distribúciu linuxu, ale návod bol testovaný pre uvedenú verziu)
* **Hardvér**:
      * **procesor**: 1GHz
      * **pamäť**: 512MB (min. 256MB)
      * **diskový priestor**: 1 GB
      * **ostatné**: Sieťová karta

## Programové požiadavky pre inštaláciu distribučnej verzie
----------------
* **Softvérové závislosti**:
      * databáza MongoDB
      * Java Runtime Environment (JRE) verzie 1.7.0
      * balík lksctp-tools

* **Závislosti v rámci architektúry nástroja SLAmeter**
      * Exportér: [MyBeem](https://git.cnl.sk/monica/slameter_exporter/wikis/home) - Program umožňuje ukladanie prijatých dát do databázy alebo ich sprístupnenie priamym pripojením, ktoré budú následne vyhodnotené príslušnými prídavnými modulmi. Je implementáciou zhromažďovacieho procesu nástroja SLAmeter. Z toho vyplýva jeho závislosť na meracom a exportovacom procese - MyBeem, alebo iné implementácie.


## Inštalácia distribučnej verzie v systéme Ubuntu 14.04.2 LTS
---------------------------

### 1. Inštalácia MongoDB 
Najskôr treba naimportovať verejný GPG kľúč.
```bash
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
```
Ďalej pokračujeme
```bash
echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
```
Nasleduje update lokálnej databázy balíkov.
```bash
sudo apt-get update
```
Potom inštalácia MongoDB databázy.
```bash
sudo apt-get install -y mongodb-org
```

### 2. Inštalácia ďalších závislosti 
Inštaláciu Java JRE 7 a lksctp-tools vykonáme príkazom:
```bash
sudo apt-get install openjdk-7-jre-headless lksctp-tools
```

### 3. Inštalácia samotného JXColl

Postup je nasledovný:

I. Stiahnuť DEB balík zo systému GIT
```bash
sudo wget https://git.cnl.sk/monica/slameter_collector/raw/master/deb/jxcoll_4.0.1_i386.deb --no-check-certificate 
```

II. Spustiť stiahnutý DEB balík pomocou príkazu 
```bash
sudo dpkg -i jxcoll_4.0.1_i386.deb 
```

III. Nastaviť konfiguračný súbor `/etc/jxcoll/jxcoll_config.xml`. Najmä databázové pripojenie a protokol na počúvanie pre IPFIX správy. 

Opis parametrov konfiguračného súboru je dostupný v [používateľskej príručke](JXCollPouzivatelskaPriruckav401).

## Spustenie distribučnej verzie
Realizujeme príkazom:
```bash
sudo /etc/init.d/jxcolld start
```
alebo priamo príkazom
```bash 
jxcoll 
```
Viac informácii o možnostiach spustenia tohto programu s rozličnými parametrami je dostupných v  [používateľskej príručke](JXCollPouzivatelskaPriruckav401).

## Vlastný preklad/spustenie vývojovej verzie
Pre podrobné informácie o preklade kliknite [sem](prekladJXColl4)

## Iné

Ďalšie informácie o predošlých verziách tohto programu je možné nájsť [tu](http://wiki.cnl.sk/Monica/KolektorJXColl), alebo v príručkách ktoré sú dostupné [tu](https://git.cnl.sk/monica/slameter_collector/tree/master/doc).