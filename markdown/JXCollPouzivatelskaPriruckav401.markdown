# Používateľská príručka programu JXColl v4.0.1
----------------------------

### Požiadavky na technické prostriedky
--------------------------

Spolahlivý beh samotného programu si vyžaduje nasledovnú hardvérovú konfiguráciu:

   * **Operačný systém:** Ubuntu 14.04.2 LTS (je možné použiť aj inú distribúciu linuxu, ale návod bol testovaný pre uvedenú verziu)
   * **Hardvér:**
      * procesor: 1GHz
      * pamäť: 512MB
      * diskový priestor: 1 GB
      * ostatné: Sieťová karta

### Požiadavky na programové prostriedky
---------------------

   * **Softvérové závislosti:**
      * databáza MongoDB
      * Java Runtime Environment (JRE) verzie 1.7.0
      * balík lksctp-tools

   * **Závislosti v rámci architektúry nástroja SLAmeter**
      * Exportér: [MyBeem](https://git.cnl.sk/monica/slameter_exporter/wikis/home) - Program umožňuje ukladanie prijatých dát do databázy alebo ich sprístupnenie priamym pripojením, ktoré budú následne vyhodnotené príslušnými prídavnými modulmi. Je implementáciou zhromažďovacieho procesu nástroja SLAmeter. Z toho vyplýva jeho závislosť na meracom a exportovacom procese - MyBeem, alebo iné implementácie.


Požiadavky na technické prostriedky sa líšia v závislosti od množstva súčasne bežiacich meraní pomocou priameho pripojenia a počtu aktívnych modulov programu.
JXColl pre spoľahlivý beh vyžaduje približne 120MB voľného pamäte RAM, avšak so zapnutým modulom pre meranie jednosmerného oneskorenia táto veľkosť sa
pohybuje okolo dvojnásobku tejto hodnoty.

Nainštalovaný program zaberá približne 2.3MB na pevnom disku. Uvedená kapacita disku je potrebná, ak sú dáta pomocou JXColl exportované na lokálnu databázu.
Je potrebné si uvedomiť, že JXColl daemon loguje do /var/log/jxcoll/ a pri nastavenej úrovni logovania ALL alebo DEBUG, môžu logovacie súbory mať značnú
veľkosť. Pri dosiahnutí veľkosti 100MB sa obsah log súboru zálohuje a skomprimuje. Archivuje sa posledných 10 rotácií (1GB log výstupu).

Monitorovanie rozsiahlejšej siete (napr. sieť poskytovateľa komunikačných služieb) si vyžaduje podstatne väčšie hardvérové nároky.


### Vlastná inštalácia
--------------

Vlastná inštalácia pozostáva z inštalácie DEB balíka v prostredí operačného systému Ubuntu alebo Debian. V prostredí iného operačného systému inštalácia pozostáva
z nakopírovania spustiteľného Java archívu `jxcoll.jar` do priečinka podľa vlastnej voľby. Následne treba nakopírovať súbor popisujúci podporované informačné
elementy protokolu IPFIX programom JXColl `ipfixFields.xml` a ukážkový konfiguračný súbor `jxcoll_config.xml`, ktorý je potrebné upraviť pre vlastné prostredie meraní.

### Použitie programu
--------------

JXColl je konzolová aplikácia. Na operačných systémoch Ubuntu/Debian v prípade inštalácie programu pomocou DEB inštalačného balíka, JXColl sa spúšťa na pozadí automaticky pri štarte systému ako daemon. JXColl daemon (jxcolld) je možné ovládať nasledovným príkazom:

```bash
sudo /etc/init.d/jxcolld <command> 
```

kde **command** treba zameniť za jedno z nasledovného :

   * **start** spustí JXColl daemon, ak ešte nebeží,
   * **stop** zastaví činnost JXColl daemon-a,
   * **restart** zastaví a znovu spustí JXColl daemon-a,
   * **status** zistí či JXColl daemon beží alebo nie,
   * **usage / help** zobrazí informácie o ovládaní JXColl daemon-a.

Hned po inštalácii nie je JXColl daemon spustený a v tomto prípade je potrebné
bud reštartovat pocítac, alebo použit vyššie uvedený init.d skript. Výstup JXColl
daemon-a sa dá prezriet v súbore:

```bash
/var/log/jxcoll/YYYYMMDD-HHmmss/jxcoll.log 
```

kde **Y** - rok, **M** - mesiac, **D** - den, **H** - hodina, **m** - minúta, **s** - sekunda spustenej inštancie JXColl daemon-a.

Inštalácia JXColl pomocou DEB balíka umožňuje aj ďalší spôsob spustenia programu, ktoré je možné dosiahnuť v príkazovom riadku zadaním príkazu:

```bash
jxcoll [--logtofile] 
```

Ak zadáme nepovinný argument --logtofile, výstup programu bude presmerovaný do log súboru popísaného vyššie. Ak používateľ nie je root, je potrebné mať v systéme pridelené sudo právo a JXColl spustiť príkazom:

```
sudo jxcoll [--logtofile] 
```

Tak ako väčšina aplikácií v prostredí operačného systému Linux, aj JXColl má k dispozícii manuálové stránky (man), ktoré je možné zobraziť pomocou príkazov:

```bash
man jxcoll 
```

a 

```bash
man jxcoll_config 
```

V prostredí iného operačného systému ako Ubuntu/Debian, alebo pri potrebe manuálneho spustenia, JXColl sa spúšťa pomocou Java interpretéra s voliteľným parametrom pozostávajúcim z cesty (relatívnej alebo absolútnej) ku konfiguračnému súboru:

```bash
java -jar jxcoll.jar [/cesta/ku/konfiguracnému/súboru/jxcoll_config.xml] [--logtofile] 
```

Ak sa nezadá cesta ku konfiguračnému súboru, aplikácia k nemu automaticky očakáva túto cestu:

```bash
/etc/jxcoll/jxcoll_config.xml 
```

Ak konfiguračný súbor nie je nájdený, aplikácia skončí s chybovým hlásením.

Dalšou podmienkou spustenia JXColl je súbor `ipfixFields.xml`. Cesta k tomuto súboru
sa nastavuje v konfiguračnom súbore jxcoll.conf. Ak pri spustení JXColl
sa súbor `ipfixFields.xml`nenachádza v adresári definovanom v konfiguračnom súbore,
aplikácia skončí s chybovým hlásením. V prípade absencie riadku s cestou k `ipfixFields.xml` v konfiguračnom súbore, JXColl automaticky predpokladá túto cestu:
`/etc/jxcoll/ipfixFields.xml`.

Ak sa ani tu XML súbor nenachádza, JXColl ukončí
svoju činnost. Bez tohto súboru nie je možné rozpoznať údaje z prijatých IPFIX
paketov.

Ak chceme program spúštat zo zdrojových súborov, je potrebná znalost nastavovania
ciest ku triedam pre Javu, eventuálne vediet kompilovat zdrojové súbory v Jave.

### Popis dialógu s používateľom
-----------------

Kedže program je konzolová aplikácia, neposkytuje žiadne grafické zobrazenie dialógu
pre používatela. Chybové a informacné hlásenia sú zobrazované v rovnakej
konzole v ktorej bol program spustený, prípadne v log súbore ak bol program spustený s voliteľným argumentom.

Ukončenie programu sa vykoná stlačením kombinácie kláves `CTRL + C` alebo poslaním
signálu SIGTERM alebo SIGINT konkrétnemu procesu: 

```bash
kill -SIGTERM pid_procesu_jxcoll 
```

Na operačných systémoch Ubuntu/Debian pri nainštalovanom DEB balíku je možné ukončenie JXColl 
daemon-a pomocou init.d skriptu (vid. vyššie).

### Opis konfiguračného súboru
----------------------

V dodanej verzii JXColl, pôvodný textový konfiguračný súbor jxcoll.conf bol nahradený XML súborom jxcoll config.xml, ktorý prináša štruktúrovanosť konfiguračných parametrov programu. Pomocou tejto zmeny štruktúry dát konfiguračného súboru sa stáva jednoznačnou a prehľadnejšou. Okrem toho, táto zmena bola nevyhnutná aj z dôvodu implementácie XML súboru v súčasne vyvíjanej medzivrstve (ECAM, viď. informačnú stránku MONICA výskumnej skupiny.), ktorá okrem JXColl už mala dávnejšie k dispozícii konfiguračné súbory ostatných časti nástroja BasicMeter v podobe XML dokumentu.Konfiguračný súbor je odovzdávaný ako parameter príkazového riadku. Jednotlivé konfiguračné parametre sa triedia podľa typu modulov, ktorých sa nastavenia týkajú. Tieto typy ako aj zoznam všetkých možných parametrov, ich popis, štandardné
hodnoty a možné voľby sa nachádzajú v nasledujúcej tabuľke. V prípade, že daná hodnota pre akýkoľvek parameter nie je uvedená v konfiguračnom súbore, parameter sa nastaví na štandardnú hodnotu. Ukážkový konfiguračný súbor na inštalačnom médiu obsahuje približné popisy parametrov a ich štandardné hodnoty. Parameter sa zapisuje vo formáte: <br>
< meno parametra > hodnota < meno parametra > <br>

Konfiguračný súbor môže obsahovať komentár, ktorý musí byť ohraničený znakmi: <br>
< ! -- komentár -- > <br>

Keďže heslá sú zadávané ako čistý text, je na používateľovi aby konfiguračnému súboru nastavil také práva, aby konfiguračný súbor bol prístupný len pre používateľov, ktorí môžu spúšťať program.

#### Všeobecné nastavenia celého programu (global)

| **Parameter** | **Štandardná hodnota** | **Prípustné hodnoty** | **Popis** |
|--------|--------|--------|--------|
| logLevel | ERROR| ALL, DEBUG, INFO, WARN, TRACE, ERROR, FATAL, OFF | úroveň logovania programu |
| ipfixFiledsXML | /etc/jxcoll/ipfixFields.xml | platná cesta v rámci súborového systému | cesta ku XML súboru popisujúceho IPFIX informačné elementy |
| ipfixTemplateTimeout | 300 | prirodzené celé císlo väcšie ako 0 | čas, po ktorom sa šablóna pre IPFIX paket považuje za neplatnú |
| listenPort | 9996 | prirodzené celé číslo z intervalu <0-65535> (ktoré nie je obsadené) | port, na ktorom beží vlákno prijímajúce dáta zo siete |
| receiveUDP | no | yes, no | príjem pomocou transportného protokolu UDP |
| receiveTCP | no | yes, no | príjem pomocou transportného protokolu TCP |
| receiveSCTP | no | yes, no | príjem pomocou transportného protokolu SCTP |
| maxConnections | 10 | 1-20 | maximálny počet pripojení (týka sa len TCP alebo SCTP) |
| *Modul: Modul pre synchronizáciu meracích bodov (sync)* ||||
| makeSync | no | yes, no | príznak, či sa kolektor správa ako synchronizačný server voči meracím bodom |
| listenSynchPort | 5544 | prir. číslo z intervalu 0-65535  (kt. nie je obsadené) | port, na ktorom bude počúvať synchronizačný server |




#### Modul pre meranie jednosmerného oneskorenia (owd)

| **Parameter** | **Štandardná hodnota** | **Prípustné hodnoty** | **Popis** |
| -------- |--------|--------|--------|
| measureOwd | no | yes , no | príznak, či kolektor má merať jednosmerné oneskorenie |
| owdStart_ObservationPointTemplateID | 256 | identifikátor šablóny, ktorý je v súlade s IPFIX špecifikáciou | identifikátor šablóny meracieho bodu, v ktorom sa začína meranie owd |
| owdStart_ObservationDomainID | 0 | identifikátor domény, ktorý je v súlade s IPFIX špecifikáciou | doména, v ktorej sa merací bod nachádza |
| owdStart_Host | 127.0.0.1 | názov alebo IP adresa v správnom formáte | názov alebo IP adresa meracieho bodu |
| owdStart_ObservationPointID | 123 | identifikátor meracieho bodu, ktorý je v súlade s IPFIX špecifikáciou | identifikátor meracieho bodu, v ktorom sa začína meranie owd |
| owdEnd_ObservationPointTemplateID | 257 | identifikátor šablóny, ktorý je v súlade s IPFIX špecifikáciou | identifikátor šablóny meracieho bodu, v ktorom sa končí meranie owd |
| owdEnd_ObservationDomainID | 0 | identifikátor domény, ktorý je v súlade s IPFIX špecifikáciou | doména, v ktorej sa merací bod nachádza |
| owdEnd_Host | 127.0.0.1 | názov alebo IP adresa v správnom formáte | názov alebo IP adresa meracieho bodu |
| owdEnd_ObservationPointID | 321 | identifikátor meracieho bodu, ktorý je v súlade s IPFIX špecifikáciou | identifikátor meracieho bodu, v ktorom sa končí meranie owd |
| passiveTimeout | 5000 | prirodzené celé číslo väčšie ako 0 | passiveTimeout, ktorý je nastavený aj na meracích bodoch |
| activeTimeout | 10000 | prirodzené celé číslo väčšie ako 0 | activeTimeout, ktorý je nastavený aj na meracích bodoch |

#### Modul pre priame spracovanie údajov (acp)

| **Parameter** | **Štandardná hodnota** | **Prípustné hodnoty** | **Popis** |
| -------- |--------|--------|--------|
| acpTransfer | no | yes, true, no, false | príznak zapnutia / vypnutia služby pre priame pripojenie na JXColl |
| acpPort | 2138 | prirodzené celé číslo <0-65535> (ktoré nie je obsadené) | port, na ktorom beží služba pre priame pripojenie na JXColl |
| acpLogin| bm | reťazec udávajúci prihlasovacie meno | prihlasovacie meno pre priame pripojenie na JXColl |
| acpPassword | bm | reťazec udávajúci heslo | heslo pre priame pripojenie na JXColl |

#### Modul pre export údajov do databázy (database)

| **Parameter** | **Štandardná hodnota** | **Prípustné hodnoty** | **Popis** |
| -------- |--------|--------|--------|
| dbExport | yes | yes, true, no, false | príznak, či sa exportujú výsledky meraní do databázy PostgreSQL |
| dbHost | localhost | názov alebo IP adresa databázového servera | databázový server PostgreSQL |
| dbPort | 5432 | port služby PostgreSQL | port, na ktorom beží databáza PostgreSQL |
| dbName | bm | reťazec udávajúci názov databázy | databáza pre ukladanie výsledkov meraní v PostgreSQL |
| dbLogin | bm | reťazec udávajúci prihlasovacie meno | prihlasovacie meno do databázy PostgreSQL |
| dbPassword | bm | reťazec udávajúci heslo | heslo do databázy PostgreSQL |

#### Modul pre účtovaciu aplikáciu (accounting)

| **Parameter** | **Štandardná hodnota** | **Prípustné hodnoty** | **Popis** |
| -------- |--------|--------|--------|
| accExport | no | yes, true, no, false | príznak, či sa exportujú výsledky meraní pre účtovanie do databázy postgresql |
| AccRecordExportInterval | 60 | prirodzené celé císlo väčšie ako 0 | čas v sekundách, po ktorom sa záznamy pre účtovanie majú uložiť do databázy |
| collectorID | 1 | prirodzené celé číslo väčšie ako 0 | identifikátor zhromažďovacieho procesu |



### Popis správ pre systémového programátora
-----------------------

V dodanej verzii JXColl sa zmenil aj spôsob zobrazovania správ. Logovací subsystém však zostal nedotknutý. Správy oproti starej verzie programu sú teraz prehľadnejšie a kratšie.

Počas behu programu sa vypisujú rôzne hlásenia od chybových až po informačné.
Logovací subsystém programu je možné inicializovat rôznymi úrovňami. Ich popis
je uvedený v tabuľke nižšie. Každá úroveň zahŕňa v sebe aj úrovne na nižšom stupni,
takže napr. pre úroveň ERROR sa budú zobrazovať aj hlásenia typu FATAL. Na
reálnu prevádzku je vhodné nastaviť úroveň ERROR.



| **Typ hlásenia** | **Popis** |
| -------- | -------- |
| ALL | vypisuje sa všetko |
| DEBUG | zobrazujú sa kompletné vypisy celého diania v programe |
| INFO | program informuje o svojej činnosti a akcii, ktorú práve vykonáva |
| WARN | vypíšu sa informácie o upozorneniach programu na možné chyby alebo zlú interpretáciu vstupných dát |
| TRACE | zobrazia sa informácie o stave programu |
| ERROR | sú vypísané hlásenia chýb majúcich vplyv na dáta |
| FATAL | hlásenia, ktoré sú pre beh programu smrteľné a zvyčajne znamenajú nezotaviteľnú chybu programu |
| OFF | vypnú sa všetky hlásenia programu |



### Chybové hlásenia
----------------------

Počas používania programu môže dôjst k nasledujúcim chybám.

**Chyba:**

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

**Popis a riešenie:**

V týchto prípadoch sa JXColl nedokáže napojit na databázu.
Bud je zle zadaná adresa, port servera, prihlasovacie údaje alebo je spojenie blokované/
nefunkcné.



**Chyba:**

```bash
[main] INFO sk.tuke.cnl.bm.JXColl.Config - Loading config file: /zla/cesta/k/jxcoll.conf
[main] ERROR sk.tuke.cnl.bm.JXColl.Config - Could not load property file: /zla/cesta/k/jxcoll.conf !
```

**Popis a riešenie:**

Nie je možné nacítat konfiguracný súbor. Treba sa uistiť, či súbor
`/etc/jxcoll/jxcoll.conf` existuje, alebo či je k nemu správne zadaná cesta v argumente programu.



**Chyba:**

```bash
[main] FATAL sk.tuke.cnl.bm.JXColl.IpfixElements - XML file ”/zla/cesta/k/ipfixFields.xml” was not found!
[main] FATAL sk.tuke.cnl.bm.JXColl.JXColl - JXColl could not start because of an error while processing XML file!
```

**Popis a riešenie:**

Nenašiel sa súbor =ipfixFields.xml=, ktorý slúži na rozpoznanie
údajov z IPFIX paketu. Treba sa uistit, či sa súbor nachádza v priečinku definovanom
v konfiguračnom súbore alebo v predvolenej ceste (`/etc/jxcoll/ipfixFields.xml`).



**Chyba:**

```bash
[ACP Thread 4] ERROR sk.tuke.cnl.bm.JXColl.export.ACPIPFIXWorker - IO EXCEPTION :null
[ACP Thread 4] DEBUG sk.tuke.cnl.bm.JXColl.export.ACPIPFIXWorker - Closing connection in try-catch
```

**Popis a riešenie:**

V tomto prípade modul, ktorý používa protokol ACP na priame
sprístupnenie nameraných dát, necakane prerušil spojenie. JXColl sa automaticky
zotaví a bude nadalej cakat pripojenie cez protokol ACP.



**Chyba:**

```bash
[[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.export.DBExport - Check if is DB connected failed: java.lang.NullPointerException
```

**Popis a riešenie:**

Pocas spracovania údajov sa došlo k prerušení spojenia s databázou.
Treba sa uistit, ci chyba nenastala v spojení.



**Chyba:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Element with ID: 74 is not supported, skipped! Update XML file!
```

**Popis a riešenie:**

Pocas spracovania údajov sa narazilo na nepodporovaný informačný element. JXColl tento element preskočí. 
Treba aktualizovat XML súbor `ipfixFields.xml` o informácie o tomto elemente, prípadne doimplementovať jeho podporu v JXColl.



**Chyba:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - i.e. ’icmpTypeCodeIPv6’ (unsigned16) - received data has wrong datatype! (4 bytes)
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Skipping this element DB exportation!
```

**Popis a riešenie:**

Počas spracovania údajov sa narazilo na informačný element, ktorého veľkosť nekorešponduje s očakávaným dátovým typom podľa XML súboru.
JXColl tento element preskočí. Nápravu je nutné vykonať pravdepodobne na strane exportéra.



**Chyba:**

```bash
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - ”i.e. ’mplsLabelStackSection5’ - Cannot decode datatype: octetArray
[Net Parser] ERROR sk.tuke.cnl.bm.JXColl.RecordDispatcher - Skipping this element DB exportation!
```

**Popis a riešenie:**

Počas spracovania údajov sa narazilo na informačný element, ktorého dátový typ JXColl nevie dekódovať. JXColl tento element preskočí. 
Nápravu je nutné vykonať na strane JXColl.



### Chybové hlásenia súvisiace s Java Virtual Machine (JVM)
-----------------------------

Program je interpretovaný v Java Virtual Machine. Chyby, ktoré môžu nastať a nie
sú ošetrené vlastnými chybovými hláseniami programu sú chyby, ktoré boli nepredvídané
a sú lahko rozoznatelné tým, že nie sú formátované v štýle loggera a zvyčajne
sú označené ako Java Error alebo Exception. Obyčajne sa vypíše aj čast zásobníka.
Bežne sú to tri riadky v hierarchii volania danej metódy, ktorá takto zlyhala. Takéto
chyby znamenajú poškodenie funkcie programu a je nutné ho reštartovat. Chybu je
možné opraviť len v zdrojovom kóde, teda sa berie ako programátorská chyba.