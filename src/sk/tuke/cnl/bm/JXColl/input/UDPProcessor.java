/*
 * Copyright (C) 2012 Lubos Kosco, Michal Kascak, Adrian Pekar, Tomas Verescak
 *
 * This file is part of JXColl.
 *
 * JXColl is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * JXColl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JXColl; If not, see <http://www.gnu.org/licenses/>.
 */
package sk.tuke.cnl.bm.JXColl.input;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixUdpTemplateCache;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.DataFormatException;
import sk.tuke.cnl.bm.TemplateException;
import sk.tuke.cnl.bm.JXColl.IpfixParser;
import sk.tuke.cnl.bm.JXColl.NetFlowParser;
import sk.tuke.cnl.bm.JXColl.PacketCache;
import sk.tuke.cnl.bm.JXColl.PacketObject;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * This class parses the contents of incoming data, processes them and sends to
 * export classes. Class refactored by Tomas Verescak.
 */
public class UDPProcessor extends Thread {

    private static Logger log = Logger.getLogger(UDPProcessor.class.getName());
//    private final int TEMPLATE_RECORD = 2, OPTIONS_TEMPLATE_RECORD = 3;
    /** Template cache */
    private IpfixParser parser = new IpfixParser(IpfixUdpTemplateCache.getInstance());
    private NetFlowParser parserNetFlow = new NetFlowParser();
    /** Flow Records Dispatcher */
//    private RecordDispatcherNew dispatcher = RecordDispatcherNew.getInstance(); // vytvori sa este pred zavolanim metody run()
//    private ExporterKey exporterKey;
    public UDPProcessor() {
        super("UDP Processor");
    }

    /**
     * Hlavná metóda vlákna. Vyberá dáta z PacketCache a predáva ich parseru.
     * V prípade zachytenia výnimky DataFormatException sa aktuálne spracovávaná
     * správa preskočí.
     */
    @Override
    public void run() {

        // pracuj pokial neukoncime vlakno prerusenim
        while (!interrupted()) {


//            Set<ExporterKey> exporters = PacketCache.getExporterKeys();
//            // ak nikto neposiela cez udp
//            if (exporters.isEmpty()) {
//                try {
//                    sleep(200);
//                    continue;   //ideme na dalsiu iteraciu
//                } catch (InterruptedException ex) {
//                    break;
//                }
//            }
//            for (ExporterKey exporter : exporters) {
//                // ak nas prerusili, tak skoncime
//                if (isInterrupted()) {
//                    break;
//                }

            /// ziskame packet z cache daneho exportera
            PacketObject p;
            try {
                p = PacketCache.read(); // bude cakat kym tam nieco nepride
            } catch (InterruptedException ex) {
                break;
            }

            log.debug(String.format("Packet read from %s:%d (%d bytes)",
                    p.getAddr().getAddress(), p.getAddr().getPort(), p.getPacket().remaining()));
//            if (packetObject == null) {
//                // znamena to ze pre dany exporter nie je v cace nic
//                continue;
//            }
//            try {
            // rob parsovanie
            // nacitajme data z packet cache
//                    PacketObject packetObject = PacketCache.read();
//                log.debug("Source UDP port: " + packetObject.getAddr().getPort());
            ByteBuffer packetData = p.getPacket();
            log.debug("Length of packet: " + packetData.remaining());
            // zistime ci je to IPFIX packet
            int version = Support.unsignShort(packetData.getShort(0));
            int length = Support.unsignShort(packetData.getShort(2));
//                int version = (packetData[0] << 8) + packetData[1]; // 16 bytes, so high portion + low portion
            //int size = (packet[2] << 8) + packet[3];   // v NetFlow 9 je to count, v IPFIX je to celkova velkost: message + sets
            // nedava stale spravnu hodnotu, treba prekontrolovat vypocet
            //@TODO:IPFIX sequence number discontinuities SHOULD be logged.
            if (version == 0x000a) {
                if (length != packetData.remaining()) {
                    log.error("Malformed IPFIX packet, packet does not have stated length! Should be: " + length + ", actually is: " + packetData.remaining());
                }
                //log.debug("Incoming IPFIX packet ...");
                //log.info("Incoming IPFIX packet with size: " + size + " bytes"); // problemovy vypis, vid. vissie
//                    IPFIXMessage message = parseIPFIXMessage(packetData, packetObject.getAddr(), packetObject.getTimeReceived());

                try {
                    
                    parser.parseIpfixMessage(packetData, p.getAddr(), p.getTimeReceived());

                } catch (TemplateException | DataFormatException ex) {
                    // napr.ked ide sablony je <= 255
                    log.error(ex.getMessage());
                    log.info("Data corrupted! Skipping this IPFIX message!");
                    continue;
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(UDPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }

              
            }else if(version == 5){
                log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  version 5");
                try {
                    parserNetFlow.parseNetflow5Message(packetData, p.getAddr(), p.getTimeReceived());
                } catch (DataFormatException ex) {
                    java.util.logging.Logger.getLogger(UDPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TemplateException ex) {
                    java.util.logging.Logger.getLogger(UDPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if(version == 9){
                log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  version 9");
                try {
                    parserNetFlow.parseNetflow9Message(packetData, p.getAddr(), p.getTimeReceived());
                } catch (DataFormatException ex) {
                    java.util.logging.Logger.getLogger(UDPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TemplateException ex) {
                    java.util.logging.Logger.getLogger(UDPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                log.error("Version: " + version + " is unknown, probably not an IPFIX PACKET !!!");
            }


//            } catch (InterruptedException ex) {
//                interrupt();

//            }//for
        }//while

        // when the thread is at the end of its life
        // uzatvaranie spojenia s DB bolo presunute do JXColl.stopJXColl()
//        dispatcher.closeDBConnection();
//        ipfixTemplateCache.cancelCleaningTask();
//        log.info("Cache cleaning task was stopped!");
//        log.debug("NET XML PARSER Stopped!");
    }
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final UDPProcessorNew other = (UDPProcessorNew) obj;
//        if (!Objects.equals(this.exporterKey, other.exporterKey)) {
//            return false;
//        }
//        return true;
//    }
//    public int hashCode() {
//        int hash = 3;
//        hash = 37 * hash + Objects.hashCode(this.exporterKey);
//        return hash;
//    }
//    public ExporterKey getExporterKey() {
//        return exporterKey;
//    }
}
