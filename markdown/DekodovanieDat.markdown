# Dekodovanie IPFIX dát v JXColl
----------------------

Abstraktné dátové typy IPFIX definované v [RFC5101](http://tools.ietf.org/html/rfc5101#section-6) a [RFC5102](http://tools.ietf.org/html/rfc5102#section-3.1) sú v JXColl dekódované pomocou triedy IPFIXDecoder.java. V nej je tento problém rozdelený do metód zaoberajúcich sa jednotlivými kategóriami abstraktných typov IPFIX. Tieto kategórie sú:

   * Bezznamienkové celočíselné typy (unsigned integral types) 
   * Znamienkové celočíselné typy (signed integral types)
   * Typy obsahujúce adresu (address types)
   * Desatinné čísla (floating point numbers)
   * Pravdivostná hodnota (boolean)
   * Reťazec znakov a oktetov (octetArray and string)
   * Typy obsahujúce čas (time values)

## Dekódovanie bezznamienkových celočíselných typov
-----------------------

Programovací jazyk Java nepozná bezznamienkové primitívne typy, iba znamienkové. Preto je potrebné zakódovať ich do väčších typov, napr. unsigned short uložíme do int a podobne. Na túto konverziu využívame metódy triedy support.

   * unsigned8 - predstavuje jednobajtové bezznamienkové  číslo. Uložíme do premennej short a získame textovú hodnotu.
   * unsigned16 - predstavuje dvojbajtové bezznamienkové číslo . Uložíme do premennej int a získame textovú hodnotu.
   * unsigned32 - predstavuje štvorbajtové bezznamienkové číslo. Uložíme do premennej long a získame textovú hodnotu.
   * unsigned64 - predstavuje osembajtové bezznamienkové čislo. Interpretujeme pomocou triedy BigInteger a získame textovú hodnotu.

## Dekódovanie znamienkových celočíselných typov
--------------

Využijeme štandardné primitívne typy programovacieho jazyka Java. 

   * unsigned8 - predstavuje jednobajtové znamienkové číslo. Uložíme do premennej byte a získame textovú hodnotu.
   * unsigned16 - predstavuje dvojbajtové znamienkové číslo . Uložíme do premennej short a získame textovú hodnotu.
   * unsigned32 - predstavuje štvorbajtové znamienkové číslo. Uložíme do premennej int a získame textovú hodnotu.
   * unsigned64 - predstavuje osembajtové znamienkové čislo. Uložíme do premennej int a získame textovú hodnotu.

## Dekódovanie typov obsahujúcich adresu
-------------

   * ipv4Address - získame textovú hodnotu IPv4 adresy zo 4-bajtového čísla pomocou triedy Inet4Address.
   * ipv6Address - získame textovú hodnotu IPv6 adresy zo 16-bajtového čísla pomocou triedy Inet6Address.
   * macAddress - získame textovú hodnotu MAC adresy zo 6-bajtového čísla pomocou vlastnej metódy.

## Dekódovanie desatinných čísel
-------------------

Na dekódovanie použijeme štandardné desatinné dátové typy programovacieho jazyka Java

   * float32 - získame textovú hodnotu 4-bajtového desatinného čísla interpretovaného ako typ float - desatinné číslo s jednoduchou presnosťou.
   * float64-  získame textovú hodnotu 4-bajtového desatinného čísla interpretovaného ako typ double - desatinné číslo s dvojitou presnosťou.


## Dekódovanie pravdivostnej hodnoty - boolean
------------

V protokole IPFIX je pravdivostná hodnota definovaná ako jednobajtové číslo s hodnotami 1(true) alebo 2(false). Ostatné hodnoty predstavujú nedefinovanú hodnotu. Textová reprezentácia po dekódovaní je 'true' alebo 'false'.


## Dekódovanie reťazca znakov a reťazca oktetov - string a octetArray
---------------

Reťazec znakov je definovaný ako postupnosť Unicode (UTF-8). Ide o dátový typ s premenlivou veľkosťou, preto pri parsovaní (Trieda NetXMLParser musela byť aktualizovaná) je potrebné zohľadniť nasledujúcu vec. Veľkosť informačného elementu s dátovým typom string alebo octetArray je nastavená na 65535. To pre zhromažďovací proces indikuje, že veľkosť dát je zahrnutá do dátovej časti (nie v šablóne). Veľkosť samotných dát je zakódovaná v prvom bajte dát. Vo väčšine prípadov sú dáta kratšie ako 255 bajtov a teda jeden bajt stačí. V tomto prípade nasledujúcich X bajtov predstavuje samotné dáta. Ak sú dáta dlhšie ako 255, prvý bajt je nastavený na číslo 255 a nasledujúce 2 bajty (bezznamienkové 16-bitové číslo) predstavujú dĺžku dát. Za nimi už nasledujú samotné dáta, viď [RFC5101](http://tools.ietf.org/html/rfc5101#section-7)


## Dekódovanie typov obsahujúcich čas.
------------

   * dateTimeSeconds - Predstavuje počet sekúnd od 00:00, 1.1.1970. Je zakódované ako 32-bitové číslo.
   * dateTimeMilliseconds - Predstavuje počet milisekúnd od 00:00, 1.1.1970. Je zakódované ako 64-bitové číslo.
   * dateTimeMicroseconds - Predstavuje počet mikrosekúnd od 00:00, 1.1.1900 zakódované ako 64-bitové čislo vo formáte NTP Timestamp. Pozri [RFC1305](http://www.ietf.org/rfc/rfc1305.txt), resp. [RFC2030](http://www.faqs.org/rfcs/rfc2030.html). Po dekódovaní je výstupom počet milisekúnd od 00:00, 1.1.1970.
   * dateTimeNanoseconds - Predstavuje počet nanosekúnd od 00:00, 1.1.1900 zakódované ako 64-bitové čislo vo formáte NTP Timestamp. Pozri [RFC1305](http://www.ietf.org/rfc/rfc1305.txt), resp. [RFC2030](http://www.faqs.org/rfcs/rfc2030.html). Po dekódovaní je výstupom počet milisekúnd od 00:00, 1.1.1970.

Na dekódovanie NTP Timestamp hodnôt bola použitá knižnica Apache Commons Net, konkrétne trieda org.apache.commons.net.ntp.TimeStamp.

