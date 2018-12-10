# Systémová príručka programu JXColl v4.0.1
-----------------------


## Implementačné detaily jednotlivých súčastí
------------------------

   * [Dekódovanie IPFIX abstraktných dátových typov](DekodovanieDat)
   * [Modul pre meranie jednosmerného oneskorenia](OWDKomponent)
   * [Transportné protokoly TCP, SCTP a UDP](JXCollTransportneProtokoly)
   * [Dokumentácia k vytváraniu deb balíkov pre kolektor](https://git.cnl.sk/monica/slameter_collector/wikis/create_debian)
   * [Architektúra JXColl](https://git.cnl.sk/matus.husovsky/doc/raw/master/architektura_mongo.pdf) 

## Zoznam balíkov v JXColl
-------------------------

   * **sk.tuke.cnl.bm**   -   balik obsahujuci pomocne triedy
   * **sk.tuke.cnl.bm.JXColl**   -   hlavny balik obsajujuci hlavnu triedu, a spracovanie dat
   * **sk.tuke.cnl.bm.IPFIX**   -   balik obsahujúci objekty IPFIX správ
   * **sk.tuke.cnl.bm.OWD**   -   balik pre modul merania jednosmerného oneskorenia
   * **sk.tuke.cnl.bm.accounting**   -   balik pre účtovací modul
   * **sk.tuke.cnl.bm.export**   -   balík pre triedy vykonávajjúce export dát
   * **sk.tuke.cnl.bm.input**   -   balik pre triedy slúžiace na príjem dát z exportérov

### Triedy balíka sk.tuke.cnl.bm

   * **ACPIPFIXTemplate**   -   Trieda slúžiaca na definovanie šablóny pre ACP prenos.  V súčasnosti je táto podpora presunutá do súboru ipfixFields.xml
   * **Filter**   -   Objekt predstavujúci filter pre ACP protokol
   * **SimpleFilter**   -   Jednoduchý filter pre ACP protokol
   * **Templates**   -   Nepoužíva  sa
   * **Sampling**   -    Nepoužíva sa
   * **InetAddr**   -    Trieda obsahuje funkcie funkcie vzťahujúce sa na prácu s IP adresou.

### Triedy balíka sk.tuke.cnl.bm.JXColl

   * **JXColl**   -   hlavná trieda programu. Spúšťa a ukončuje všetky vlákna programu, zapína logging, načítava konfiguračný súbor.
   * **Config**   -   slúži na načítanie údajov z konfiguračného súboru do jeho verejných premenných, ktoré je možné v ktorejkoľvek časti programu načítať.
   * **IJXConstants**   -   obsahuje konštanty programu JXColl
   * **PacketCache**   -   Vyrovnávacia pamäť na báze fronty (queue), do ktorej sa ukladajú prijaté IPFIX správy a odkiaľ ich vyberá vlákno parsera
   * **NetXMLParser**   - Vlákno v slučke vyberajúce pakety z PacketCache a parsujúce IPFIX správy pričom dáta posiela RecordDispatcher-u na export. V súčasnosti sa pracuje na novšej prehľadnej verzii, ktorá dekomponuje problém na podproblémy.
   * **RecordDispatcher**   -   Objekt ktorý prijme od NetXMLParsera dáta so šablónou a vykonáva export do databázy, pomocou ACP protokolu resp ukladá účtovacie záznamy.
   * **PacketObject**   -  Trieda obsahujúca surové dáta prijaté od exportéra doplnené o jeho adresu.
   * **support**   -    Pomocná trieda obsahujúca najmä metódy slúžiace na získanie správnej číselnej hodnoty bezznamienkových typov v Jave
   * **IpfixElements**   -  Táto trieda získava informácie zo súboru ipfixFields.xml, ktorý obsahuje informácie o informačných elementoch. Umožňuje jednoduchý prístup k týmto údajom z ktoréhokoľvek miesta v programe.
   * **IPFIXDecoder**   -  Trieda slúžiaca na dekódovanie abstraktných typov IPFIX do textovej podoby uložiteľnej do databázy.
   * **Synchronisation**   - Trieda slúžiaca na synchronizáciu času exportéra voči kolektoru.  

  
### Triedy balíka sk.tuke.cnl.bm.JXColl.IPFIX

   * **IPFIXMessage**   -   Objekt predstavujúci celú IPFIX správu. Môže obsahovať viacero objektov typu IPFIXSet
   * **IPFIXSet**   -   Objekt predstavujúci IPFIX Set. Ten obsahuje záznam šablóny, options šablóny alebo dátový záznam
   * **IPFIXTemplateRecord**   -   Objekt predstavujúci záznam šablóny. Obsahuje viacero objektov typu FieldSpecifier
   * **IPFIXOptionsTemplateRecord**   -   Objekt predstavujúci záznam options šablóny. Obsahuje viacero objektov typu FieldSpecifier
   * **IPFIXDataRecord**   -    Objekt obsahujúci dáta - hodnoty informačných elementov definovaných v objekte prislúchajúcej šablóny.
   * **FieldSpecifier**   -    Objekt obsahujúci informácie o type (ID) a veľkosti dát informačného elementu. T a L hodnoty z modelu TLV (Type-Length-Value), V je v objekte IPFIXDataRecord
   * **IPFIXTemplateCache**   -    Cache uchovávajúca objekty šablón
   * **KeyObject**   -    Objekt kľúča používaný v Template cache


### Triedy balíka sk.tuke.cnl.bm.JXColl.OWD

   * **OWDCache**   -   Medzizásobník, v ktorom sa uchovávajú údaje po dobu nastavenú v konfiguračnom súbore. Obsahuje dva zásobníky, každý pre jeden z meracích bodov medzi ktorými sa zisťuje jednosmerné oneskorenie.
   * **OWDObject**   -   Slúži na uloženie hodnôt potrebných pre výpočet jednosmerného oneskorenia v objekte.
   * **OWDTemplateRecord**   -   Reprezentuje záznam šablóny
   * **OWDListener**   -   Vykonáva predspracovávanie údajov. Kontroluje a kategorizuje pakety prijaté v zhromažďovacom procese a podľa konfiguračných nastavení ich ukladá v zásobníkoch pre meranie jednosmerného oneskorenia - OWDCache
   * **OWDFieldSpecifier**   -   Slúži na prácu s elementmi šablóny a záznamu o toku.
   * **OWDFlushCacheABThread**   -   Vlákno, ktoré kontroluje objekty v zásobníkoch pre výpočet jednosmerného oneskorenia. Keď objekt je viac ako 10 sekúnd v zásobníku, dáta sa presunú na pôvodné spracovanie.
   * **OWDTemplateCache**   -    Zásobník na ukladanie šablón IPFIX správy pre účely merania jednosmerného oneskorenia
   * **Synchronization**   -   Vlákno, ktoré sa správa ako synchronizačný server. Na nastaviteľnom porte čaká na prijatie časových známok, na ktoré odpovie svojím lokálnym časom. Pomocou vlákna na meracích bodoch, medzi ktorými sa meria jednosmerné oneskorenie bude zabezpečený synchronizovaný čas.


### Triedy balíka sk.tuke.cnl.bm.JXColl.accounting

   * **AccountingManager**   -   Objekt starajúci sa o spracovanie účtovacích dát
   * **AccountingRecord**   -   Záznam účtovacích údajov
   * **AccountingRecordsCache*   -   Cache účtovacích záznamov
   * **AccountingRecordsExporter**   -    Objekt starajúci sa o expoort účtovacích záznamov



### Triedy balíka sk.tuke.cnl.bm.JXColl.export

   * **ACPServer**   -   Vlákno starajúce sa o prenos dát pomocou protoklu ACP. Jedná sa o server, na ktorý sa pripájajú analyzéry. Sám spúšťa 5 workerov, ktorí analyzéry obsluhujú.
   * **ACPIPFIXWorker**   -   Obsluhuje ACP protokol vrámci jedného analyzéra.
   * **PGClient**   -   Objekt starajúci sa o export dát do databázy.
   * **DBExport**   -    Objekt zabezpečujúci pripojenie na databázu.


### Triedy balíka sk.tuke.cnl.bm.JXColl.input

   * **UDPReceiver**   -   Vlákno prijímajúce dáta z exportérov pomocou transportným protokolom UDP
   * **TCPReceiver**   -   Vlákno čakajúce na pripojenie transportným protokolom TCP. Vytvorené spojenie predáva vláknu TCPProcessor.
   * **TCPProcessor**   -   Vlákno starajúce sa o prijímanie dát pomocou protokolu TCP v rámci jedného pripojenia exportéra k JXColl.
   * **SCTPReceiver**   -    Vlákno čakajúce na vytvorenie asociácie transportným protokolom SCTP. Vytvorenú asociáciu predáva vláknu SCTPProcessor.
   * **SCTPProcessor**   - Vlákno starajúce sa o prijímanie dát pomocou protokolu SCTP v rámci jednej asociácie exportéra k JXColl.
   * **Receiver**   - Rozhranie, ktoré implementujú všetky Receiver vlákna.

   