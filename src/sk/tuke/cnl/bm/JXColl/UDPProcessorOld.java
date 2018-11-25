///*
// * Copyright (C) 2011 Lubos Kosco, Michal Kascak, Adrian Pekar, Tomas Verescak
// *
// * This file is part of JXColl.
// *
// * JXColl is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 3 of the License, or
// * (at your option) any later version.
//
// * JXColl is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with JXColl; If not, see <http://www.gnu.org/licenses/>.
// */
//package sk.tuke.cnl.bm.JXColl;
//
//import java.nio.ByteBuffer;
//import java.util.Objects;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixUdpTemplateCache;
//import org.apache.log4j.Logger;
//import sk.tuke.cnl.bm.JXColl.IPFIX.ExporterKey;
//import sk.tuke.cnl.bm.JXColl.IPFIX.TemplateException;
//
///**
// * This class parses the contents of incoming data, processes them and sends to
// * export classes. Class refactored by Tomas Verescak.
// */
//public class UDPProcessorOld extends Thread {
//
//    private static Logger log = Logger.getLogger(UDPProcessorOld.class.getName());
////    private final int TEMPLATE_RECORD = 2, OPTIONS_TEMPLATE_RECORD = 3;
//    /** Template cache */
//    private IpfixParser parser = new IpfixParser(IpfixUdpTemplateCache.getInstance());
//    /** Flow Records Dispatcher */
////    private RecordDispatcherNew dispatcher = RecordDispatcherNew.getInstance(); // vytvori sa este pred zavolanim metody run()
//    private ExporterKey exporterKey;
//
//    public UDPProcessorOld(ExporterKey key) {
//        super("UDP Processor " + key.getIpfixDevice() + ":" + key.getExporterSrcUdpPort());
//        this.exporterKey = key;
//        log.debug("Starting IPFIX Parser");
//    }
//
//    @Override
//    public void run() {
//
//        // pracuj pokial neukoncime vlakno prerusenim
//        while (!interrupted()) {
//
//
////            Set<ExporterKey> exporters = PacketCache.getExporterKeys();
////            // ak nikto neposiela cez udp
////            if (exporters.isEmpty()) {
////                try {
////                    sleep(200);
////                    continue;   //ideme na dalsiu iteraciu
////                } catch (InterruptedException ex) {
////                    break;
////                }
////            }
////            for (ExporterKey exporter : exporters) {
////                // ak nas prerusili, tak skoncime
////                if (isInterrupted()) {
////                    break;
////                }
//
//            /// ziskame packet z cache daneho exportera
//            PacketObject packetObject;
//            try {
//                packetObject = PacketCache.getFromMultiCache(exporterKey); // bude cakat kym tam nieco nepride
//            } catch (InterruptedException ex) {
//                break;
//            }
////            if (packetObject == null) {
////                // znamena to ze pre dany exporter nie je v cace nic
////                continue;
////            }
////            try {
//            // rob parsovanie
//            // nacitajme data z packet cache
////                    PacketObject packetObject = PacketCache.read();
////                log.debug("Source UDP port: " + packetObject.getAddr().getPort());
//            ByteBuffer packetData = packetObject.getPacket();
//            log.debug("Length of packet: " + packetData.remaining());
//            // zistime ci je to IPFIX packet
//            int version = Support.unsignShort(packetData.getShort(0));
//            int length = Support.unsignShort(packetData.getShort(2));
////                int version = (packetData[0] << 8) + packetData[1]; // 16 bytes, so high portion + low portion
//            //int size = (packet[2] << 8) + packet[3];   // v NetFlow 9 je to count, v IPFIX je to celkova velkost: message + sets
//            // nedava stale spravnu hodnotu, treba prekontrolovat vypocet
//            //@TODO:IPFIX sequence number discontinuities SHOULD be logged.
//            if (version == 0x000a) {
//                if (length != packetData.remaining()) {
//                    log.error("Malformed IPFIX packet, packet does not have stated length! Should be: " + length + ", actually is: " + packetData.remaining());
//                }
//                //log.debug("Incoming IPFIX packet ...");
//                //log.info("Incoming IPFIX packet with size: " + size + " bytes"); // problemovy vypis, vid. vissie
////                    IPFIXMessage message = parseIPFIXMessage(packetData, packetObject.getAddr(), packetObject.getTimeReceived());
//              
//                try {
//                    parser.parseIpfixMessage(packetData, packetObject.getAddr(), packetObject.getTimeReceived());
//                  
//                } catch (TemplateException | DataFormatException ex) {
//                    // napr.ked ide sablony je <= 255
//                    log.error(ex.getMessage());
//                    log.info("Data corrupted! Skipping this IPFIX message!");
//                    continue;
//                }
//
//            } else {
//                log.error("Version: " + version + " is unknown, probably not an IPFIX PACKET !!!");
//            }
//
//
////            } catch (InterruptedException ex) {
////                interrupt();
//
////            }//for
//        }//while
//
//        // when the thread is at the end of its life
//        // uzatvaranie spojenia s DB bolo presunute do JXColl.stopJXColl()
////        dispatcher.closeDBConnection();
////        ipfixTemplateCache.cancelCleaningTask();
////        log.info("Cache cleaning task was stopped!");
////        log.debug("NET XML PARSER Stopped!");
//    }
//
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final UDPProcessorOld other = (UDPProcessorOld) obj;
//        if (!Objects.equals(this.exporterKey, other.exporterKey)) {
//            return false;
//        }
//        return true;
//    }
//
//    public int hashCode() {
//        int hash = 3;
//        hash = 37 * hash + Objects.hashCode(this.exporterKey);
//        return hash;
//    }
//
//    public ExporterKey getExporterKey() {
//        return exporterKey;
//    }
//}
