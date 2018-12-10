# Dokumentácia k vytváraniu deb balíkov pre kolektor
-----------------

Vytvorenie deb balíka pre JXColl je možné realizovať dvoma spôsobmi:

## Pomocou skriptu
----------------------

Stačí spustiť skript [buildDeb.sh](https://git.cnl.sk/monica/slameter_collector/blob/master/deb/buildDeb.sh), ktorý sa nachádza v priečinku jxcoll/deb.

```bash
sh buildDeb.sh
```

Výstupom tohto skriptu je súbor s názvom debian.deb, ktorý môžme následne premenovať podľa verzie JXColl.
Tento skript vykonáva nasledovné:

   1. v prípade, ak neexistuje priečinok debian, extrahuje ho z archívu debian.tar.gz, inak tento krok preskočí
   1. skopíruje binárny súbor z projektu do DEB balíčka (predpokladá sa, že bol program kompilovaný v Netbeans IDE pomocou Clean and Build tlačidla)
   1. skopíruje konfiguračný súbor z projektu do DEB balíčka
   1. skopíruje IPFIX definičný súbor z projektu do DEB balíčka
   1. vymaže prípadné dočasné súbory z DEB balíčka
   1. vygeneruje MD5 kontrolné súčty pre všetky súbory DEB balíčka
   1. zabezpečí maximálnu kompresiu manuálových stránok a changelog súborov
   1. skopíruje binárny súbor z projektu do DEB balíčka
   1. vytvorí samotný DEB balíček
   1. overí ho pomocou programu lintian - ten vypíše prípadne varovania a/alebo chyby
   1. archivuje vytvorený DEB balíček do archívu debian.tar.gz

Pred spustením skriptu je nutné skompilovať JXColl pomocou Netbeans IDE tlačidlom 'Clean and Build'. Prípadné zmeny control alebo changelog súboru, manuálových stránok je nutné vykonať ručne.
Manuálové stránky je vhodné editovať pomocou programu GmanEdit.
Po spustení skriptu sa vytvorí DEB balíček s názvom debian.deb. Ten je vhodné premenovať podľa aktuálnej verzie. Vytvorí sa aj archív debian.tar.gz, ktorý obsahuje najaktuálnejšiu adresárovú štruktúru DEB balíčka pre budúce využitie (ak neexistuje priečinok debian, vytvorí sa extrakciou z tohto archívu). Ak chceme len aktualizovať kód, stačí spustiť skript a ten sa o všetko postará, pričom vytvorí aj adresár debian. Súbory možno v ňom upravovať až kým nie sme s balíčkom spokojní. Ak je všetko hotové, v Netbeans IDE (!!!) vymažeme priečinok debian (vykoná sa SVN DELETE, namiesto obyčajného odstránenia zo súborového systému) a projekt commitneme.

## Manuálnym spôsobom
----------


Vytvoríme nasledujúcu štruktúru priečinkov s nasledujúcimi súbormi (- opis niektorých súborov). Pre zjednodušenie práce sa nasledujúca štruktúra so všetkými súbormi je stiahnuteľná z [tohto](https://git.cnl.sk/monica/slameter_collector/blob/master/deb/debian.tar.gz) miesta.

```
debian/DEBIAN/conffiles      - obsahuje cesty pre konfiguračné súbory (napr. /etc/jxcoll/jxcoll_config.xml)
debian/DEBIAN/control        - obsahuje informácie o balíku a programe
debian/DEBIAN/md5sums     - md5 hash pre každý súbor okrem súborov v priečinku DEBIAN. MD5 hash môžeme získať pomocou príkazu md5sum menoSuboru
debian/DEBIAN/postinst       - skript ktorý sa spustí po inštalácii balíka
debian/DEBIAN/postrm         - skript ktorý sa spustí po odstránení balíka
debian/DEBIAN/preinst        - skript ktorý sa spustí pred inštaláciou balíka  
debian/DEBIAN/prerm         - skript ktorý sa spustí pred odstránení balíka
debian/etc/init.d/jxcolld        - jxcoll daemon
debian/etc/jxcoll/ipfixFields.xml  
debian/etc/jxcoll/jxcoll.conf
debian/usr/bin/jxcoll - skript pre spustenie jxcoll pomocou príkazu jxcoll
debian/usr/lib/jxcoll/jxcoll.jar       - binarka, dbat na to aby mal root pravo spustit subor
debian/usr/lib/jxcoll/jxcollrun.sh - pomocný súbor pre spustenie jxcoll podporou logovania výstupov
debian/usr/lib/jxcoll/jxcoll.jar
debian/usr/lib/jxcoll/lib/apache-log4j-extras-1.1.jar
debian/usr/lib/jxcoll/lib/common-lang3.jar 	
debian/usr/lib/jxcoll/lib/commons-net-3.0.1.jar 
debian/usr/lib/jxcoll/lib/dnsjava-2.1.3.jar 	
debian/usr/lib/jxcoll/lib/dom4j-1.6.1.jar
debian/usr/lib/jxcoll/lib/java-json.jar 	
debian/usr/lib/jxcoll/lib/jaxen-1.1.1.jar
debian/usr/lib/jxcoll/lib/log4j-1.2.16.jar 
debian/usr/lib/jxcoll/lib/mongo-java-driver-2.11.3.jar 	
debian/usr/share/doc/jxcoll/changelog.Debian.gz
debian/usr/share/doc/jxcoll/changelog.gz - treba nainštalovať balík devscripts. Na úpravu changelog súborov viď. man debchange.
debian/usr/share/doc/jxcoll/copyright
debian/usr/share/man/man1/jxcoll.1.gz - na úpravu je potrebná znalosť vytvárania man stránok
debian/usr/share/man/man5/jxcoll.conf.5.gz - na úpravu je potrebná znalosť vytvárania man stránok 
debian/var/log/jxcoll/
```

### Control súbor

Príklad control súboru:
```
Package: jxcoll
Version: 4.0.1
Section: java
Priority: extra
Architecture: i386
Depends: default-jre-headless | openjdk-7-jre-headless | java7-runtime-headless
Suggests: mybeem (>=1.1-9)
Installed-size: 2200
Maintainer: Adrian Pekar <adrian.pekar@gmail.com>
Description: Java XML Collector (JXColl)
 Represents the 2nd layer of the MONICA architecture.
 It was designed for IPFIX traffic gathering. These traffic can be
 saved by JXColl into a database for further processing or can be
 also forwarded to connected analysers for real-time processing.
 .
 For details about JXColl and MONICA project please visit our homepage at:
 https://git.cnl.sk/monica/slameter_collector/
```

Aktuálnu verziu (Version) jxcoll je možne zistiť [tu](https://git.cnl.sk/monica/slameter_collector/wikis/jxcoll). **Veľkosť** (Installed-size) môžeme zistiť pomocou zistenia veľkosti súboru **/tmp/debian/** (v bájtoch).

Podrobnejší (p)opis control súborov sa nachádza na nasledujúcej stránke v sekcii 5.3: http://www.debian.org/doc/debian-policy/ch-controlfields.html

### Binárne súbory

Súbory, ktoré sa budú meniť pri nových verziach sú:

```
/debian/usr/lib/jxcoll/jxcoll.jar
/debian/usr/lib/jxcoll/lib/apache-log4j-extras-1.1.jar
/debian/usr/lib/jxcoll/lib/common-lang3.jar 	
/debian/usr/lib/jxcoll/lib/commons-net-3.0.1.jar 
/debian/usr/lib/jxcoll/lib/dnsjava-2.1.3.jar 	
/debian/usr/lib/jxcoll/lib/dom4j-1.6.1.jar
/debian/usr/lib/jxcoll/lib/java-json.jar 	
/debian/usr/lib/jxcoll/lib/jaxen-1.1.1.jar
/debian/usr/lib/jxcoll/lib/log4j-1.2.16.jar 
/debian/usr/lib/jxcoll/lib/mongo-java-driver-2.11.3.jar 	
```





Tieto súbory je potrebne aktualizovať z [GIT repozitára](https://git.cnl.sk/monica/slameter_collector/tree/master/bin).

### Skripty

Jednotlivé skripty:

```
/debian/DEBIAN/postinst
/debian/DEBIAN/postrm
/debian/DEBIAN/preinst  
/debian/DEBIAN/prerm
/debian/etc/init.d/jxcolld
/debian/usr/bin/jxcoll
/debian/usr/lib/jxcoll/jxcollrun.sh 
```

bude potrebné aktualizovať manuálne. **Tieto súbory okrem debian.tar.gz archívu sa zatiaľ nikde nenachádzajú!**

Keby sa v budúcnosti zmenili aj konfiguračné súbory, ich aktualizácia bude tiež nevyhnutná. [tu](https://git.cnl.sk/monica/slameter_collector/tree/master).

### 2. Export zdrojových súborov z GIT repozitára

Realizujeme na základe príkazu:

```
git clone https://git.cnl.sk/monica/slameter_collector.git -c http.sslVerify=false
```

Získané súbory treba nakopírovať na správne miesta vo vyššie uvedenej štruktúre!

### 3. Vytvorenie .deb inštalačného balíka

Po aktualizácii jednotlivých súborov a údajov deb balíka je potrebné tento balík vytvoriť pomocou príkazu:

```
dpkg-deb --build debian/
```

Vytvorí sa inštalačný balík s názvom **debian.deb**.

### 4. Kontrola .deb inštalačného balíka

Príkazom
```
lintian debian.deb
```
je možné prekontrolovať balík a zistiť či vyhovuje štandardným požiadavkám DEBIAN inštalačných balíkov. **Vlastníka každého súboru je potrebné nastaviť na root!**

### 5. Úprava názvu výsledného .deb inštalačného balíka

**debian.deb** premenujeme na **jxcoll_verzia_i386.deb**.
