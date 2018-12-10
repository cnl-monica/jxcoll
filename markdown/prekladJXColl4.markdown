# Preklad JXColl
---------

## Technické požiadavky pre inštaláciu vývojovej verzie
--------
* **Operačný systém:** Ubuntu 14.04.2 LTS (je možné použiť aj inú distribúciu linuxu, ale návod bol testovaný pre uvedenú verziu)
   **Hardvér:**
   *   **procesor**: 1GHz
   *   **pamäť**: 512MB (min. 256MB)
   *   **diskový priestor**: 1 GB
   *   **ostatné**: Sieťová karta

## Programové požiadavky pre inštaláciu vývojovej verzie
--------
* **Softvérové závislosti:**
   *   databáza MongoDB
   *   Java Runtime Environment (JRE) verzie 1.7.0
   *   balík lksctp-tools
* **Závislosti v rámci architektúry nástroja SLAmeter:**
   *   Exportér: [MyBeem](https://git.cnl.sk/monica/slameter_exporter) - Program umožňuje ukladanie prijatých dát do databázy alebo ich sprístupnenie priamym pripojením, ktoré budú následne vyhodnotené príslušnými prídavnými modulmi. Je implementáciou zhromažďovacieho procesu nástroja SLAmeter. Z toho vyplýva jeho závislosť na meracom a exportovacom procese - MyBeem, alebo iné implementácie.




## Inštalácia vývojovej verzie v systéme Ubuntu 14.04.2 LTS
--------

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

### 3. Získanie vývojovej verzie JXColl zo systému GIT
Realizujeme na základe príkazu:
```bash
git clone https://git.cnl.sk/monica/slameter_collector.git -c http.sslVerify=false%ENDCODE%
```

### 4. Nastavenie konfiguračného súboru
Nastaviť konfiguračný súbor `/etc/jxcoll/jxcoll_config.xml`. Najmä databázové pripojenie a protokol na počúvanie pre IPFIX správy.
Opis parametrov konfiguračného súboru je dostupný v [používateľskej príručke](JXCollPouzivatelskaPriruckav401).

## Preklad zdrojových kódov v prostredí NetBeans
--------

   1. Používateľ si nainštaluje vývojárské prostredie NetBeans v8.0.2 a vyššie
   1. Otvorí stiahnutý repozitár ako projekt (návod vyššie) alebo pridá v NetBeans-e repozitár pre aktuálnu verziu JXColl https://git.cnl.sk/monica/slameter_collector.git 
   1. Pomocou tlačítka (Clean and Build) si preloží zdrojové súbory, výsledok (spustiteľný binárný súbor NetBeans uloží v priečinku dist pracovného priečinka programu)

## Vlastný preklad v iných IDE
---------

Preklad programu spočíva v nakopírovaní zdrojových súborov a spustení kompilátora jazyka Java s potrebnými parametrami a parametrom classpath nastaveným na prídavné knižnice. Odporúča sa použiť váš obľúbený java IDE, kde stačí jednoducho nastaviť verziu JDK na 7.0 alebo vyššie a do cesty classpath pridať cesty ku všetkým potrebným knižniciam.

## Spustenie vývojovej verzie JXColl z príkaz. riadku
Program spúšťame z priečinka `dist/` príkazom:
```bash
java -jar jxcoll.jar
```