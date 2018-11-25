///*
// * Copyright (C) 2011 Tomas Verescak
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
//package sk.tuke.cnl.bm.JXColl.input;
//
//import sk.tuke.cnl.bm.JXColl.UDPProcessorOld;
//import java.io.IOException;
//import java.io.InterruptedIOException;
//import java.net.DatagramSocket;
//import java.net.InetSocketAddress;
//import java.net.SocketException;
//import java.nio.ByteBuffer;
//import java.nio.channels.DatagramChannel;
//import java.util.ArrayList;
//import java.util.List;
//import org.apache.log4j.Logger;
//import sk.tuke.cnl.bm.JXColl.Config;
//import sk.tuke.cnl.bm.JXColl.IJXConstants;
//import sk.tuke.cnl.bm.JXColl.IPFIX.ExporterKey;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixUdpTemplateCache;
//import sk.tuke.cnl.bm.JXColl.OWD.OWDCache;
//import sk.tuke.cnl.bm.JXColl.OWD.OWDListener;
//import sk.tuke.cnl.bm.JXColl.PacketCache;
//import sk.tuke.cnl.bm.JXColl.Support;
//
///**
// * Thread that receives IPFIX packets by UDP protocol
// * @author Tomas Verescak
// */
//public class UDPServerOld extends Thread {
//
//    private DatagramChannel channel;
//    private DatagramSocket socket;
//    private OWDListener owdListener;
//    protected InetSocketAddress exporterAddress;
//    private static Logger log = Logger.getLogger(UDPServerOld.class.getName());
//    public static List<UDPProcessorOld> dataProcessors = new ArrayList<>();
//
//    /** Address of exporter sending an IPFIX message  */
//    public UDPServerOld(int port) throws SocketException, IOException {
//        super("UDP Receiver");
//        if (Config.measureOwd) {
//            log.info("Creating listener for OWD measuerement.");
//            owdListener = new OWDListener();
//        }
//
//        channel = DatagramChannel.open();
//        channel.configureBlocking(true);
//        socket = channel.socket();
//        socket.bind(new InetSocketAddress(port));
//
//        log.info("Setting default input buffer: " + IJXConstants.INPUT_BUFFER_SIZE);
//        log.info("IPFIX template timeout set to: " + Config.IPFIX_TEMPLATE_TIMEOUT + " s");
//        log.info("Listening on port: " + port + " (UDP)");
//    }
//
//    @Override
//    public void run() {
//        ByteBuffer buffer = ByteBuffer.allocate(IJXConstants.INPUT_BUFFER_SIZE);
//        long time;
//        while (!interrupted()) {
//            try {
//                buffer.clear();
//                exporterAddress = (InetSocketAddress) channel.receive(buffer);
//                log.debug(String.format("Received packet from %s:%d", exporterAddress.getAddress(), exporterAddress.getPort()));
//                time = System.currentTimeMillis();
//
//            } catch (InterruptedIOException e) {
//                interrupt();	// znovu prerusime lebo pri hodeni vynimky sa interrupt status vynuluje
//                log.debug("Interrupted via InterruptedIOException");
//                OWDCache.closeDBConnection();
//                break;
//
//            } catch (IOException e) {
//                if (!isInterrupted()) {
//                    //interrupt();
//                    log.error(e);
//                    e.printStackTrace();
//                    break;
//                } else {
//                    log.debug("Interrupted via IOException");
//                    break;
//                }
//            }
//
//            buffer.flip(); // otocime buffer aby sme z neho mohli citat
//
//            // One Way Delay measurement part
//            if (Config.measureOwd) {
//                try {
//                    owdListener.preProcessing(buffer, (InetSocketAddress) exporterAddress);
//                } catch (InterruptedException ex) {
//                    log.debug("interrupted in NetConnect OWD preprocessing");
//                    interrupt();
//                } catch (Exception ex) {
//                    log.error(ex);
//                }
//
//            } else {
//
//                log.debug("buffer.remaining() = " + buffer.remaining());
//
//                byte[] data = new byte[buffer.remaining()];
//                for (int i = 0; buffer.hasRemaining(); i++) {
//                    data[i] = buffer.get();
//                }
//
//                ByteBuffer dataBuffer = ByteBuffer.wrap(data);
//                long odid = Support.unsignInt(dataBuffer.getInt(12));
//                ExporterKey exporter = new ExporterKey(exporterAddress.getAddress(), exporterAddress.getPort(), odid);
//
//                boolean exporterPresent = PacketCache.isExporterPresent(exporter);
//                // vlozime udaje do cache
//                PacketCache.writeToMultiCache(dataBuffer, exporterAddress, time);
//
//
//                // ak vlakno pre tento exporter nemame, tak ho vytvorime, a spustime
//                if (!exporterPresent) {
//                    UDPProcessorOld processor = new UDPProcessorOld(exporter);
//                    processor.start();
//                    dataProcessors.add(processor);
//                }
//
//
////                try {
//
////                    log.debug("Putting packet into cache: " + new Date(time));
//                //super.putIntoCache(buffer, (InetSocketAddress) exporterAddress); // metoda rodicovskej triedy
////                } catch (InterruptedException ex) {
////                    log.debug("interrupted while putting data into Cache!");
////                    interrupt();
////                }
//            }
//        }//while
//
//        // povypiname vsetky vlakna
//        interruptAllThreads();
//
//        dataProcessors.clear();
//        //vypneme cistiace vlakno
//        IpfixUdpTemplateCache.getInstance().cancelCleaningTask();
//        log.info("Cache cleaning task was stopped!");
//    }//run
//
//    /**
//     * Stops processor given by exporterKey and removes it from UDP processor list
//     * @param key ExporterKey
//     */
//    public static boolean removeProcessor(ExporterKey key) {
//        for (UDPProcessorOld processor : dataProcessors) {
//            if (processor.getExporterKey().equals(key)) {
//                processor.interrupt();
//                log.info("UDPProcessor for " + key + " is to be removed!");
//                return dataProcessors.remove(processor);
//            }
//        }
//        return false;
//    }
//
//    public void interruptAllThreads() {
//        // povypiname vsetky vlakna UDP procesorov
//        for (UDPProcessorOld udpProcessor : dataProcessors) {
//            if (udpProcessor.isAlive()) {
//                log.debug("Interrupting " + udpProcessor.getName());
//                udpProcessor.interrupt();
//                try {
//                    log.debug("Waiting for " + udpProcessor.getName());
//                    udpProcessor.join();
//                } catch (InterruptedException ex) {
//                }
//            }
//        }
//    }
//}
