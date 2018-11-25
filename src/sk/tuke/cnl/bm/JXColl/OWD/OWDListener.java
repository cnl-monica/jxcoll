/* Copyright (C) 2011  Adrian Pekar
 *
 * This file is part of JXColl v.3.6.
 *
 * JXColl v.3.6 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * JXColl v.3.6 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JXColl v.3.6; If not, see <http://www.gnu.org/licenses/>.
 *
 *              Fakulta Elektrotechniky a informatiky
 *                  Technicka univerzita v Kosiciach
 *
 *  Optimalizácia zhromažďovacieho procesu nástroja BasicMeter
 *                          Diplomová práca
 *
 *  Vedúci DP:        Ing. Juraj Giertl, PhD.
 *  Konzultanti DP:   Ing. Martin Reves, PhD.
 *
 *  Diplomant:       Adrián Pekár
 *
 *  Zdrojové texty:
 *  Súbor: OWDListener.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.IJXConstants;
import sk.tuke.cnl.bm.JXColl.PacketCache;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * This class provides inter-caching functionality for OWD measurement. It contains methods for packet categorization,
 * criteria check for OWDStart and OWDEnd Measure Points, Information element check, Template separation.
 *
 * @author Adrian Pekar
 */
public class OWDListener {

    private static Logger log = Logger.getLogger(OWDListener.class.getName());
    private OWDCache owdC = new OWDCache();
    private OWDFieldSpecifier fieldSpecifier;
    private OWDTemplateRecord owdTemplate;
    private OWDTemplateCache owdTemplateCache = new OWDTemplateCache();

    /**
     * Set level of logging for this class.
     *
     * @param level String Log Level.
     */
    public void setlogl(String level) {
        log.setLevel(org.apache.log4j.Level.toLevel(level));
    }

    /**
     * The only constructor for this class.
     */
    public OWDListener() {
    }


    /**
     * The main method for packet separation and preparation for OWD measurement. It checks feasibility of incoming packets,
     * makes packet separation for OWD Measurement.
     *
     * @param packet ByteBuffer the packet for processing.
     * @param addr InetSocketAddress the address where the packet came from.
     * @throws UnknownHostException - if the IP address of a host could not be determined.
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     * @throws NullPointerException - if input is null.
     * @throws SQLException - if there is an error on database access or other errors.
     */
    public void preProcessing(ByteBuffer packet, InetSocketAddress addr) throws UnknownHostException, InterruptedException, NullPointerException, SQLException {

        byte[] data = new byte[IJXConstants.INPUT_BUFFER_SIZE]; // reprezentacia packetu v byte array
        int i = 0;
        while (packet.hasRemaining()) {
            data[i++] = packet.get(); //nakopirujem packet z ByteBuffra do pole byte-ov
        }
        byte[] srcIP = new byte[4]; // array pre src ip adresu
        byte[] dstIP = new byte[4]; // array pre dst ip adresu
        byte[] firstPacketID = new byte[16]; // array pre firstPacketID
        byte[] lastPacketID = new byte[16]; // array pre lastPacketID

        int index = 16; // preskocim IPFIX hlavicku

        int version = (data[0] << 8) + data[1];

        if (version == 0x000a) {
            while (index < Support.unsignShort(packet.getShort(2))) {
                switch (Support.unsignShort(packet.getShort(index))) {
                    case 2:
                        log.debug("OWD: TEMPLATE SET recognized...");

                        ByteBuffer template = ByteBuffer.wrap(getTemplateSet(packet)); //vytiahnem z packetu templateSet
                        parseTemplateSet(template, addr); // parsnem templateSet a vytiahnem z neho potrebne udaje

                        byte[] templatE = template.array(); ////nakopirujem sablonu z ByteBuffra do pole byte-ov

                        //keby predosle kopirovanie nefungovalo dalsia alternativa:
//                        byte[] templatE = new byte[template.capacity()];
//                        i = 0; while (template.hasRemaining()) templatE[i++] = template.get(); //nakopirujem packet z ByteBuffra do pole byte-ov

                        //rozsirenie sablony pre 2x OWD hodnoty (pre firstpacketID match a pre lastPacketID match)
                        //ByteBuffer newT = expandTemplateSet(template);
                        //parseTemplateSet(newT, addr); // toto len test, ci to spravne prebehlo

                        try {
                            PacketCache.write(ByteBuffer.wrap(templatE), addr); //poslem LEN sablonu (moze sa stat, ze z intercache dostanu pakety s recordsetmi skor ako paket so sablonou) pre povodne spracovanie
                        } catch (InterruptedException e) {
                            throw e;
                        }
                        index += Support.unsignShort(packet.getShort(index + 2)); //nastavim index na koniec sablony
                        templatE = null;
                        break;

                    default:
                        log.debug("OWD: DATA SET recognized...");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw e;
                        }

                        switch (testRecordSet(packet, index, addr)) {

                            //ked je to packet pre owdStart
                            case 7:

//                                OWDCache.setOwdListenerLock(true);
//
//                                while (OWDCache.getOwdFlushLock() == true) {
//                                    //log.debug("OWD: sleep...");
//                                    Thread.sleep(500);
//                                }

                                index += 4; // preskocim hlavicku
                                log.debug("OWD: Packet for OWD Start measurement recognized...");

                                //zistim sourceIP
                                for (int j = 0; j < 4; j++) {
                                    srcIP[j] = packet.get(index + j + owdTemplate.getOWDFieldByElementID(8).getPosition());
                                }
                                //zistim destIP
                                for (int j = 0; j < 4; j++) {
                                    dstIP[j] = packet.get(index + j + owdTemplate.getOWDFieldByElementID(12).getPosition());
                                }
                                //zistim flowStart
                                BigInteger flowStart = new BigInteger(Long.toBinaryString(packet.getLong(index + owdTemplate.getOWDFieldByElementID(156).getPosition())), 2);
                                //zistim flowEnd
                                BigInteger flowEnd = new BigInteger(Long.toBinaryString(packet.getLong(index + owdTemplate.getOWDFieldByElementID(157).getPosition())), 2);
                                //zistim firstPacketID
                                System.arraycopy(data, index + owdTemplate.getOWDFieldByElementID(242).getPosition(), firstPacketID, 0, 16);
                                //zistim lastPacketID
                                System.arraycopy(data, index + owdTemplate.getOWDFieldByElementID(243).getPosition(), lastPacketID, 0, 16);

                                //poslem ho do cacheA
                                owdC.pushA(
                                        System.currentTimeMillis(),
                                        Support.unsignByte(packet.get(index + owdTemplate.getOWDFieldByElementID(4).getPosition())),
                                        Support.unsignShort(packet.getShort(index + owdTemplate.getOWDFieldByElementID(7).getPosition())),
                                        (Inet4Address) Inet4Address.getByAddress(srcIP),
                                        Support.unsignShort(packet.getShort(index + owdTemplate.getOWDFieldByElementID(11).getPosition())),
                                        (Inet4Address) Inet4Address.getByAddress(dstIP),
                                        Support.unsignByte(packet.get(index + owdTemplate.getOWDFieldByElementID(60).getPosition())),
                                        Support.unsignInt(packet.getInt(index + owdTemplate.getOWDFieldByElementID(138).getPosition())),
                                        Support.unsignLong(packet.getLong(index + owdTemplate.getOWDFieldByElementID(148).getPosition())),
                                        flowStart,
                                        flowEnd,
                                        firstPacketID,
                                        lastPacketID,
                                        data,
                                        addr);

//                                OWDCache.setOwdListenerLock(false);
                                index += -4; // spat na hlavicku

                                break;

                            // ked je to packet pre owdEnd
                            case 8:

//                                OWDCache.setOwdListenerLock(true);
//
//                                while (OWDCache.getOwdFlushLock() == true) {
//                                    //log.debug("OWD: sleep....");
//                                    Thread.sleep(500);
//                                }

                                index += 4; // preskocim hlavicku
                                log.debug("OWD: Packet for OWD End measurement recognized...");

                                //zistim sourceIP
                                for (int j = 0; j < 4; j++) {
                                    srcIP[j] = packet.get(index + j + owdTemplate.getOWDFieldByElementID(8).getPosition());
                                }
                                //zistim destIP
                                for (int j = 0; j < 4; j++) {
                                    dstIP[j] = packet.get(index + j + owdTemplate.getOWDFieldByElementID(12).getPosition());
                                }
                                //zistim flowStart
                                BigInteger fStart = new BigInteger(Long.toBinaryString(packet.getLong(index + owdTemplate.getOWDFieldByElementID(156).getPosition())), 2);
                                //zistim flowEnd
                                BigInteger fEnd = new BigInteger(Long.toBinaryString(packet.getLong(index + owdTemplate.getOWDFieldByElementID(157).getPosition())), 2);
                                //zistim firstPacketID
                                System.arraycopy(data, index + owdTemplate.getOWDFieldByElementID(242).getPosition(), firstPacketID, 0, 16);
                                //zistim lastPacketID
                                System.arraycopy(data, index + owdTemplate.getOWDFieldByElementID(243).getPosition(), lastPacketID, 0, 16);

                                //poslem ho do cacheB
                                owdC.pushB(
                                        System.currentTimeMillis(),
                                        Support.unsignByte(packet.get(index + owdTemplate.getOWDFieldByElementID(4).getPosition())),
                                        Support.unsignShort(packet.getShort(index + owdTemplate.getOWDFieldByElementID(7).getPosition())),
                                        (Inet4Address) Inet4Address.getByAddress(srcIP),
                                        Support.unsignShort(packet.getShort(index + owdTemplate.getOWDFieldByElementID(11).getPosition())),
                                        (Inet4Address) Inet4Address.getByAddress(dstIP),
                                        Support.unsignByte(packet.get(index + owdTemplate.getOWDFieldByElementID(60).getPosition())),
                                        Support.unsignInt(packet.getInt(index + owdTemplate.getOWDFieldByElementID(138).getPosition())),
                                        Support.unsignLong(packet.getLong(index + owdTemplate.getOWDFieldByElementID(148).getPosition())),
                                        fStart,
                                        fEnd,
                                        firstPacketID,
                                        lastPacketID,
                                        data,
                                        addr);

//                                OWDCache.setOwdListenerLock(false);
                                index += -4; // spat na hlavicku

                                break;
                            // ked to je normalny paket poslem ho na normalne spracovanie
                            default:
                                log.warn("OWD: Skipping preprocessing and OWD measurement!");
                                try {
                                    PacketCache.write(ByteBuffer.wrap(data), addr);
                                } catch (InterruptedException e) {
                                    throw e;
                                }
                                break;
                        }
                        // pripocitam dlzku aktualneho setu
                        //index += (support.unsignS(packet.getShort(index + 2)) - 4); //-4 lebo na zaciatku som pripocital hlavicku <--toto sposobovalo chyby, trebalo odpocitat -4 pri pushA a pushB
                        index += (Support.unsignShort(packet.getShort(index + 2)));
                }
            }
        } else {
            log.error("OWD: Version: " + version + " is unknown, probably not an IPFIX PACKET !!!");
        }
        data = null;
        srcIP = null;
        dstIP = null;
        firstPacketID = null;
        lastPacketID = null;
    }

    /**
     * Method which compares the template set, data set, and configuration file values.
     *
     * @param packet ByteBuffer the packet for processing.
     * @param index int start index of the set.
     * @param addr InetSocketAddress the address where the packet came from.
     * @return int code for the appropriate switch-case branch. (7 - for owdStart measure point, 8 - for owdEnd measure point,
     * other - skip owd measurement).
     */
    public int testRecordSet(ByteBuffer packet, int index, InetSocketAddress addr) {
        //int index = 16; //nastavime index na hlavicku record setu
        //check Temp in TempCache
        if (owdTemplateCache.contains(Support.unsignShort(packet.getShort(index)), addr.getAddress(), Support.unsignInt(packet.getShort(12)))) {
            //get Temp from TempCache
            owdTemplate = owdTemplateCache.getByID(Support.unsignShort(packet.getShort(index)), addr.getAddress(), Support.unsignInt(packet.getShort(12)));
            //check Temp has the needed information elements
            if (owdTemplate.getOWDFieldByElementID(4) != null
                    && owdTemplate.getOWDFieldByElementID(7) != null
                    && owdTemplate.getOWDFieldByElementID(8) != null
                    && owdTemplate.getOWDFieldByElementID(11) != null
                    && owdTemplate.getOWDFieldByElementID(12) != null
                    && owdTemplate.getOWDFieldByElementID(60) != null
                    && owdTemplate.getOWDFieldByElementID(138) != null
                    && owdTemplate.getOWDFieldByElementID(148) != null
                    && owdTemplate.getOWDFieldByElementID(156) != null
                    && owdTemplate.getOWDFieldByElementID(157) != null
                    && owdTemplate.getOWDFieldByElementID(242) != null && owdTemplate.getOWDFieldByElementID(242).isEnterpriseBit()
                    && owdTemplate.getOWDFieldByElementID(243) != null && owdTemplate.getOWDFieldByElementID(243).isEnterpriseBit()) {
                //check TempID,Addr,ObsDomainID for OWD Start
                if (Support.unsignShort(packet.getShort(index)) == Config.owdStartTempIDObsPoint
                        && addr.getAddress().toString().equals("/" + Config.owdStartHost)
                        && Support.unsignInt(packet.getShort(12)) == Config.owdStartObsDomID) {
                    Long owdStartOPID = Support.unsignInt(packet.getInt(index + 4 + owdTemplate.getOWDFieldByElementID(138).getPosition()));
                    //check owdStartObsPID
                    if (owdStartOPID.equals(Config.owdStartObsPoID)) {
                        return 7;
                    } else {
                        log.warn("OWD: RecordSet check: Config file value for START ObservationPointID do not match with packet value!");
                        return 4;
                    }
                } //check TempID,Addr,ObsDomainID OWD End
                else if (Support.unsignShort(packet.getShort(index)) == Config.owdEndTempIDObsPoint
                        && addr.getAddress().toString().equals("/" + Config.owdEndHost)
                        && Support.unsignInt(packet.getShort(12)) == Config.owdEndObsDomID) {
                    Long owdStartOPID = Support.unsignInt(packet.getInt(index + 4 + owdTemplate.getOWDFieldByElementID(138).getPosition()));
                    //check owdEndObsPID
                    if (owdStartOPID.equals(Config.owdEndObsPoID)) {
                        return 8;
                    } else {
                        log.warn("OWD: RecordSet check: Config file value for END ObservationPointID do not match with packet value!");
                        return 4;
                    }
                } else {
                    log.warn("OWD: RecordSet check: Packet not for OWD Start nor for OWD End measurement!");
                    return 3;
                }
            } else {
                log.warn("OWD: RecordSet check: One or more of the needed i.e. (s) for OWD measurement is (are) not exported!");
                log.warn("OWD: The needed i.e. for OWD measurement are: protocolIdentifier (4), sourceTransportPort (7), sourceIPv4Address (8), "
                        + "destinationTransportPort (11), destinationIPv4Address (12), ipVersion (60), observationPointId (138), "
                        + "flowID (148), flowStartNanoseconds (156), flowEndNanoseconds (157), firstPacketID (242), lastPacketID (243)");
                return 2;
            }
        } else {
            log.warn("OWD: RecordSet check: No template for this record set!");
            return 1;
        }
    }

    /**
     * Method which retrieves the template set from the packet.
     *
     * @param packet ByteBuffer the packet for processing.
     * @return byte[] template set.
     */
    public byte[] getTemplateSet(ByteBuffer packet) {
        ByteBuffer bf = ByteBuffer.allocateDirect(16 + Support.unsignShort(packet.getShort(18))); //vytvorim novy bytebuffer vo velksoti IPFIX hlavicka (16) + Template Set
        for (int i = 0; i < 16 + Support.unsignShort(packet.getShort(18)); i++) {
            bf.put(packet.get(i)); //prekopirujem ipfix hlavicku + template set do noveho bytebuffra
        }
        bf.rewind();
        bf.putShort(2, (short) bf.capacity()); // prepisem velkost noveho bytebuffra (uz bez data set)

        bf.rewind();
        byte[] templateSet = new byte[bf.capacity()];

        int i = 0;
        while (bf.hasRemaining()) {
            templateSet[i++] = bf.get(); //nakopirujem packet z ByteBuffra do pole byte-ov, ktory je uz pripraveny pre NetXMLParser
        }
        return templateSet;
    }

    /**
     * Method which parses the template and gets the needed information for data set processing.
     *
     * @param template ByteBuffer template set.
     * @param addr InetSocketAddress the address where the packet came from.
     */
    public void parseTemplateSet(ByteBuffer template, InetSocketAddress addr) {
        byte[] tempBuff;
        int setIndex = 20;

//        log.debug("OWD: Template record with ID " + support.unsignS(template.getShort(setIndex)) + " recognized ...");
//        log.debug("OWD: Set Length: "+ support.unsignS(template.getShort(setIndex-2)));
//        log.debug("OWD: Field Count: " + support.unsignS(template.getShort(setIndex + 2)));

        while (setIndex < template.capacity()) {
            int templateID = Support.unsignShort(template.getShort(setIndex));
            setIndex += 2;
            int fieldCount = Support.unsignShort(template.getShort(setIndex));
            setIndex += 2;
            owdTemplate = new OWDTemplateRecord(templateID, fieldCount);
//          log.debug("OWD: ****** FIELD SPECIFIER ******");
            int position = 0;
            for (int i = 0; i < owdTemplate.fieldCount; i++) {
                fieldSpecifier = new OWDFieldSpecifier();
                tempBuff = new byte[8];
                //log.debug("OWD: " + setIndex+" ErrOR is CommING "+template.capacity());
                if (setIndex + 4 >= template.capacity()) {
                    for (int j = 0; j < 4; j++) {
                        tempBuff[j] = template.get(setIndex + j);
                    }
                } else {
                    for (int j = 0; j < 8; j++) {
                        tempBuff[j] = template.get(setIndex + j);
                    }
                }
                fieldSpecifier.setFieldSpecifierOwd(tempBuff, position);
//                log.debug("OWD: Field specifier - Enterprise bit: " + fieldSpecifier.isEnterpriseBit());
//                log.debug("OWD: Field specifier - Information element ID: " + fieldSpecifier.getElementID());
//                log.debug("OWD: Field specifier - Length: " + fieldSpecifier.getFieldLength());
//                log.debug("OWD: Field specifier - Enterprise number: " + fieldSpecifier.getEnterpriseNumber());
//                log.debug("OWD: Field specifier - Start position in data record: " + fieldSpecifier.getPosition());
                position += fieldSpecifier.getFieldLength();
                if (!fieldSpecifier.isEnterpriseBit()) {
                    setIndex += 4;
                } else {
                    setIndex += 8;
                }
                owdTemplate.addField(fieldSpecifier);
            }
            owdTemplateCache.addTemplate(owdTemplate, addr.getAddress(), Support.unsignInt(template.getInt(12)));
        }
        tempBuff = null;
    }

    /**
     * Method which expands the template set with fields for 2 OWD values.
     *
     * @param template ByteBuffer the template for expansion.
     * @return ByteBuffer the expanded template.
     */
    public ByteBuffer expandTemplateSet(ByteBuffer template) {

        ByteBuffer newTemplate = ByteBuffer.allocate(template.capacity() + 16);
        newTemplate.put(template);
        final int header = 16;

        //newTemplate.putShort(header, (short) 333); // prepisem setID
        newTemplate.putShort(header + 2, (short) (Support.unsignShort(template.getShort(header + 2)) + 16)); // zvisim dlzku TemplateSet o 16
        //newTemplate.putShort(header+4, (short) 444); // prepisem templateID
        newTemplate.putShort(header + 6, (short) (Support.unsignShort(template.getShort(header + 6)) + 2)); //zvisim
        newTemplate.putShort(template.capacity(), (short) (244 | 0x8000));
        newTemplate.putShort(template.capacity() + 2, (short) 8);
        newTemplate.putInt(template.capacity() + 4, 26235);
        newTemplate.putShort(template.capacity() + 8, (short) (245 | 0x8000));
        newTemplate.putShort(template.capacity() + 10, (short) 8);
        newTemplate.putInt(template.capacity() + 12, 26235);

        return newTemplate;
    }
}