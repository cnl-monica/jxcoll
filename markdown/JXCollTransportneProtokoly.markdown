# Podpora transportných protokolov v JXColl
--------------

## Podpora transportného protokolu SCTP
------------------------

[SCTP](https://tools.ietf.org/html/rfc4960) je spoľahlivý transportný protokol použiteľný na IP sieťach. Rieši niektoré problémy TCP a prikladá k tomu výhody UDP. Poskytuje vlastnosti pre vysokú dostupnosť, zvýšenú spoľahlivosť a zlepšenú bezpečnosť pri vytváraní spojenia.

V [architektúre JXColl](https://git.cnl.sk/matus.husovsky/doc/raw/master/architektura_mongo.pdf) je možné si všimnúť, že podpora pozostáva z 2 tried. Prvou z nich je SCTPReceiver a je to vlákno, ktoré beží počas behu JXColl (ak je podpora SCTP zapnutá v konfiguračnom súbore) a počúva na porte 4739. Táto trieda slúži ako server, ktorý čaká na SCTP pripojenia. Akonáhle exportér začne proces vytvárania asociácie, táto trieda vytvorí kanál, ktorý sa predá novovytvorenému vláknu SCTPProcessor. Tento kanál pozostáva z signle stream asociácie. Všetky detaily komunikácie a samotný príjem IPFIX správ cez SCTP protokol zabezpečuje toto vlákno SCTPProcessor. Týchto vlákien môže byť umiestnených do skupiny vlákien maximálne 20, resp. toľko, koľko je nastavených v konfiguračnom súbore (hodnota maxConnection).

SCTPProcessor prijíma správy od exportéra a ukladá ich do PacketCache. Ak prerušíme činnosť JXColl, všetky pripojenia sa uzavrú, zostávajúce správy z PacketCache sa exportujú a JXColl sa vypne. Takisto detekujeme ukončenie z druhej strany a vlákno vtedy ukončí svoju činnosť. V tomto prípade JXColl beží ďalej a čaká na ďalšie SCTP pripojenie.

## Podpora transportného protokolu TCP
-------------------

Podobne ako pri protokole SCTP, podpora tohto transportného protokolu je zložená z 2 tried - vlákien. TCPReceiver je serverové vlákno, ktoré počúva na porte 4739, ak je podpora TCP povolená v konfiguračnom súbore. Pri vytvorení spojenia vznikne nový kanál, ktorý sa predá novovytvorenému vláknu TCPProcessor. TCPProcessor sa stará o samotnú komunikáciu a vlastný príjem IPFIX správ. Počet týchto vlákien a teda celkový počet TCP pripojení je limitovaný nastavením hodnoty maxConnection v konfiguračnom súbore.

Prijaté IPFIX správy TCPProcessor ukladá do PacketCache. Ak prerušíme činnosť JXColl, všetky pripojenia sa uzavrú na strane kolektora a počúvame kým nezistíme, že aj druhá strana ukončila spojenie. Zostávajúce správy z PacketCache sa exportujú a JXColl sa vypne. 
Podobne ako pri SCTP, ak druhá strana ukončí spojenie, vlákno končí svoju činnosť, avšak JXColl beží ďalej a čaká na dalšie pripojenie.

## Podpora transportného protokolu UDP
-------------------------

UDP je nespoľahlivý a nespojovaný transportný protokol, preto neexistuje žiadny server, na ktorý by sa exportéri pripájali. Vlákno UDPReceiver počúva na porte 4739 a pri príchode IPFIX správy ju uloží do PacketCache. IPFIX správy sú ďalej spracovávané vláknom NetXMLParser.



