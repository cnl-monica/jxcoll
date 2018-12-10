# Modul pre meranie jednosmerného oneskorenia
---------------

## Jednosmerné oneskorenie
--------------

Jednosmerné oneskorenie predstavuje čas, za ktorý sa dostane paket z jedného bodu do druhého (viď. obrázok). Určuje sa medzi dvoma synchronizovanými bodmi A a B počítačovej siete. 

![owd](https://git.cnl.sk/uploads/monica/slameter_collector/ee8ed3292f/owd.png)

Aby sa predošlo strate alebo reorganizácii paketov, musia byť pakety pre úspešnosť merania identifikované aj v zdrojovom a aj v cieľovom bode. Jednosmerné oneskorenie je možné rozdeliť na nasledujúce komponenty:

   * oneskorenie vyplývajúce z vysielania údajov − čas potrebný na vloženie všetkých bitov daného paketu na dátovú linku.
   * oneskorenie vyplývajúce zo šírenia údajov − čas potrebný na prenos paketu fyzickým médiom.
   * oneskorenie vyplývajúce zo spracovania údajov − čas potrebný na spracovanie paketu v každom uzle (smerovač, router) počas cesty.
   * oneskorenie vyplývajúce z čakania vo fronte − čas, počas ktorého čaká paket v každom uzle cesty pred posunutím ďalej.

Prvé dva komponenty sú takmer konštantné, lebo závisia od kapacity linky a od vzdialenosti uzlov. Posledné dva komponenty jednosmerného oneskorenia sú náhodné premenné, ktoré závisia od premenlivosti spracovávaných úloh v uzloch a od stavu siete. V súčasnosti môžeme povedať, že okrem niekoľko zriedkavých okolností, doba spracovania vďaka hardvérovej podpory má takmer konštantnú hodnotu.

Z dôvodu zložitosti merania jednotlivých komponentov zvlášť, jednosmerné oneskorenie budeme uvažovať ako celok zložený z vyššie uvedených častí. V tomto prípade
minimálna hodnota jednosmerného oneskorenia bude udávať približne hodnoty doby vysielania, prenosu, spracovania a doby čakania.

## Modul zhromažďovacieho procesu pre meranie jednosmerného oneskorenia
----------

Verzia JXColl v3.6 bola rozšírená o dva nové moduly určené pre meranie jednosmerného oneskorenia medzi dvoma meracími bodmi. Synchronizačný modul predstavuje
synchronizačný server, ktorý na každý prijatý paket odpovie svojím lokálnym časom. Modul pre meranie jednosmerného oneskorenia predspracováva údajov, na základe
ktorého určuje hodnoty owd a ukladá ich do databázy.

### Synchronizácia meracích bodov

Ako riešenie bolo vytvorené pre účely synchronizácie meracích bodov v kolektore samostatné a na funkčných častiach nezávislé vlákno. Toto vlákno bude počúvať
na vopred dohodnutom, ale zároveň ľubovoľne nastaviteľnom porte, ktorý po obdržaní každého paketu s časovou známkou vykoná nasledujúce kroky:

   1. zistí aktuálny čas prijatia danej časovej známky,
   1. vykoná nad zisteným časom úpravu,
   1. upravený čas následne zapíše do prijatej časovej známky na pozíciu určenú exportérom,
   1. pošle paket naspäť na tú adresu, od ktorej danú časovú známku obdržal.

Tieto kroky synchronizačného vlákna sú popísané aj algoritmom na nasledujúcom obrázku:

![alg2](https://git.cnl.sk/uploads/monica/slameter_collector/d31aed3e4a/alg2.png)

---+++ Výpočet jednosmerného oneskorenia

Samotný výpočet hodnôt jednosmerného oneskorenia sa uskutočňuje na základe obdržaných záznamov o tokoch. Tieto záznamy okrem iného obsahujú:
   * časové charakteristiky (_flowStart_ a _flowEnd_),
   * identifikátory prvého a posledného IP paketu toku (_firstPacketID_ a _lastPacketID_).

### Výber časovej charakteristiky

Na výber prenosu časovej charakteristiky je pomerne veľké množstvo IPFIX informačných elementov. Pre účely určenia jednosmerného oneskorenia sú z nich použiteľné len tie, ktoré sú podporované exportovacím procesom nástroja BasicMeter, teda ktoré exportér vie merať a následne exportovať.

Podporované informačné elementy vieme na základe ich presností zoradiť do nasledujúcich skupín:
   * flowStart − skupina absolútneho času začiatku toku,
   * flowEnd − skupina absolútneho času ukončenia toku,
   * flowStartDelta − relatívny čas začiatku toku,
   * flowEndDelta − relatívny čas ukončenia toku,
   * systemInitTime − absolútny čas inicializácie meracieho bodu,
   * flowDuration − skupina absolútneho času trvania toku.
Z týchto informačných elementov boli na prenos časovej známky vybrané skupiny absolútneho času začiatku (flowStart) a ukončenia (flowEnd) toku. Z ponúknutých
časových presností − milisekundy, mikrosekundy a nanosekundy − bola vybraná najmenšia a najpresnejšia hodnota, čo predstavuje časová presnosť v nanosekundách. Pri výbere nanosekúnd napomáhala aj skutočnosť, že z časovej charakteristiky v nanosekundách je možné pomocou jednoduchého prevodu odvodiť charakteristiku
aj v ostatných presnostiach. Na určenie hodnôt jednosmerného oneskorenia sa tak používajú informačné elementy _flowStartNanoseconds_ a _flowEndNanoseconds_.

### Spôsob identifikácie zodpovedajúcich časových známok

Výpočet jednosmerného oneskorenia vykonáme odpočítaním dvoch zodpovedajúcich časových známok. Absolútna hodnota ich rozdielu bude udávať hodnotu jednosmerného oneskorenia.
Časové známky sa považujú za zodpovedajúce, keď spĺňajú nasledujúce podmienky:
   1. záznamy o toku prišli z rôznych meracích bodov,
   1. obe sú absolútne časy začiatku (_flowStartNanoseconds_) alebo ukončenia (_flowEndNanoseconds_) toku,
   1. obe majú zhodné prislúchajúce identifikátory prvého (_firstPacketID_) alebo posledného (_lastPacketID_) IP paketu,
   1. obe patria do toho istého toku.

Prvá podmienka hovorí o tom, že keď sa meranie jednosmerného oneskorenia uskutočňuje medzi dvoma koncovými bodmi, záznamy o toku musia prichádzať od rôznych meracích bodov. Táto podmienka zároveň zabezpečuje, aby v dôsledku aktívneho exportu neprichádzali do zhromažďovacieho procesu informácie o tom istom
toku viackrát. Je to nežiaduce z dôvodu, aby hodnoty jednosmerného oneskorenia neboli nulové.

Druhá podmienka hovorí o tom, že zodpovedajúce časové charakteristiky musia byť
rovnakého charakteru, teda oba sú buď absolútne časy začiatku toku (flowStartNanoseconds), alebo absolútne časy ukončenia toku (flowEndNanoseconds).

Tretia podmienka hovorí, že časové známky musia mať rovnaké prislúchajúce identifikátory IP paketu. U dvoch absolútnych časoch začiatku toku sa musia rovnať
identifikátory prvých IP paketov toku a u dvoch absolútnych časoch ukončenia toku sa musia rovnať identifikátory posledných IP paketov toku. Prakticky to znamená,
že v prípade dvojice informačných elementov flowStartNanoseconds sa musia rovnať informačné elementy firstPacketID a v prípade dvojice informačných elementov
flowEndNanoseconds sa musia rovnať informačné elementy lastPacketID.

Štvrtá podmienka hovorí, že informácie slúžiace na výpočet jednosmerného oneskorenia, ktoré sa v kolektore získavajú z IPFIX správ, musia popisovať ten istý tok,
teda paket musí patriť do rovnakého toku v oboch meracích bodoch. Nasledujúca časť práce sa bude podrobnejšie venovať spôsobu identifikácie paketových párov.
Keď sú všetky podmienky splnené, dostaneme dve zodpovedajúce časové známky, ktoré patria tomu istému IP paketu, ktorý prešiel obidvoma meracími bodmi; a tak
môže byť jednosmerné oneskorenie vypočítané. V prípade, že niektorá z podmienok nebude splnená, hodnota jednosmerného oneskorenia sa nevypočíta.

### Identifikácia paketových párov

V práci (Husivarga, 2008) sú uvedené podmienky identifikácie paketových párov, ktoré sa určujú na základe vybraných políčok hlavičky IP paketu. Tieto polia sú nasledovné:
   * verzia (version) − špecifikuje verziu IP,
   * protokol (protocol) − špecifikuje protokol (ICMP, UDP, TCP, atď.),
   * zdrojová adresa (source address) − špecifikuje zdrojovú IP adresu,
   * cieľová adresa (destination address) − špecifikuje cieľovú IP adresu.
Kontrola, či informácie dvoch časových charakteristík v kolektore popisujú ten istý tok, spočíva v porovnaní kľúčových informačných elementov. Tieto elementy boli
určené na základe vybraných políčok IP paketu pre identifikáciu paketových párov (Husivarga, 2008). Keďže exportér je schopný merania aj jednosmerných, aj
obojsmerných tokov, splnenie podmienok je potrebné overiť aj v priamom, aj v opačnom smere.
Podmienky na identifikáciu paketových párov dvoch zodpovedajúcich časových charakteristík A a B v priamom smere sú nasledovné:
   * ipVersion A = ipVersion B,
   * protocolIdentifier A = protocolIdentifier B,
   * sourceIPv4Address A = sourceIPv4Address B,
   * sourceTransportPort A = sourceTransportPort B,
   * destinationIPv4Address A = destinationIPv4Address B,
   * destinationTransportPort A = destinationTransportPort A.
Podmienky na identifikáciu paketových párov dvoch zodpovedajúcich časových charakteristík A a B v opačnom smere sú nasledovné:
   * ipVersion A = ipVersion B,
   * protocolIdentifier A = protocolIdentifier B,
   * sourceIPv4Address A = destinationIPv4Address B,
   * sourceTransportPort A = destinationTransportPort B,
   * destinationIPv4Address A = sourceIPv4Address B,
   * destinationTransportPort A = sourceTransportPort A.


Celý proces určovania hodnôt jednosmerného oneskorenia v kolektore je opísané nasledujúcim algoritmom:

![alg1](https://git.cnl.sk/uploads/monica/slameter_collector/c50472904e/alg1.png)


### Spôsob vyprázdnenia zásobníkov

V kolektore bolo vytvorené ďalšie vlákno, ktoré je opísané algoritmom na obrázku:

![alg3](https://git.cnl.sk/uploads/monica/slameter_collector/fa9de2cfee/alg3.png)

Vlákno v každej sekunde prehľadáva zásobníky startCache a EndCache, a podľa časovej známky, ktorá je zaznamenaná pri každom zápise, určuje, či daný dátový záznam už prekročil maximálnu dobu čakania alebo nie. Maximálna doba čakania predstavuje hodnotu dvakrát väčšiu, ako je hodnota passiveTimeout exportovacieho procesu. V prípade, že je doba čakania prekročená, vlákno skupinu hodnôt zo zásobníka vymaže a dátový záznam, ktorý k nej patrí, pošle pre pôvodné spracovanie.


### Príklad použitia

Modul pre meranie jednosmerného oneskorenia je plne automatizovaný. Pred spustením JXColl treba správne nastaviť konfiguračné parametre (viď. parametre v používateľskej príručke JXColl v3.6) pre meracie body medzi
ktorými sa má jednosmerné oneskorenie merať a zapnúť modul pre synchronizáciu času na meracích bodoch. Hodnoty jednosmerného oneskorenia sa ukladajú do
databázy spolu s identifikátorom toku. Pomocou týchto hodnôt je neskôr možné identifikovať páry owd-flowID. Počas merania, údaje o toku zostanú neporušené,
treba však rátať so zvýšenými nárokmi na hardvérové prostriedky.
