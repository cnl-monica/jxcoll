/*
 * Copyright (C) 2011 Tomas Verescak
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.DataFormatException;
import sk.tuke.cnl.bm.JXColl.IJXConstants;
import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixSingleSessionTemplateCache;
import sk.tuke.cnl.bm.TemplateException;
import sk.tuke.cnl.bm.JXColl.IpfixParser;
import sk.tuke.cnl.bm.JXColl.IpfixParser.TransportProtocol;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 *
 * @author Tomas Verescak
 */
public class TCPProcessor extends Thread {

    private Logger log = Logger.getLogger(TCPProcessor.class.getName());
//    private SocketChannel channel;
    private Socket socket;
    private InetSocketAddress exporterAddress;
//    private DataInputStream input;
//    private final static int TEMPLATE_RECORD = 2, OPTIONS_TEMPLATE_RECORD = 3;
//    private IpfixSingleSessionTemplateCache ipfixTemplateCache;
    private IpfixParser parser;
    boolean interrupted = false;
//    private RecordDispatcherNew dispatcher = RecordDispatcherNew.getInstance();

    public TCPProcessor(Socket socket, InetSocketAddress exporterAddress, ThreadGroup group) {
        super(group, "TCPProcessor: " + exporterAddress.getAddress().getHostAddress() + ":" + exporterAddress.getPort());
//        this.channel = channel;
        this.socket = socket;
        try {
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000);
        } catch (SocketException ex) {
            log.error("Could not set socket timeout!");
        }
        this.exporterAddress = exporterAddress;
        this.parser = new IpfixParser(new IpfixSingleSessionTemplateCache(), TransportProtocol.TCP);
//        this.dispatcher = RecordDispatcherNew.getInstance();
        // getOpts();

    }

    private void getOpts() {
        try {
            log.debug("SO_RCVBUFS: " + socket.getReceiveBufferSize());
            log.debug("SO_REUSEADDR: " + socket.getReuseAddress());
            log.debug("SO_LINGER: " + socket.getSoLinger());
            log.debug("SO_SOTIMEOUT: " + socket.getSoTimeout());
            log.debug("SO_KEEPALIVE: " + socket.getKeepAlive());
            log.debug("SO_TCPNODELAY: " + socket.getTcpNoDelay());
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
//        byte[] input = new byte[IJXConstants.INPUT_BUFFER_SIZE];
        BufferedInputStream in = null;
        try {
//            this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            in = new BufferedInputStream(socket.getInputStream());
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

//        int bytes = 0;
        ByteBuffer buffer = ByteBuffer.allocate(IJXConstants.INPUT_BUFFER_SIZE);

        // nekonecna slucka
        while (!interrupted()) {

            int version = 0;
            int length = 0;

            buffer.clear();
            buffer.limit(4); // nastavime limit na 4, to preto, aby sme precitali verziu a dlzku.

            log.debug("reading first 4 bytes...");

            int b = 0;
            while (buffer.hasRemaining()) {
                try {
                    b = in.read();
                    buffer.put((byte) b);

                } catch (SocketTimeoutException te) {
//                    log.debug("Timeout vyprsal, skusam znova!");
                    // ak nas prerusili, posleme FIN, ale prijimame dalej az kym nenarazime na EOF
                    if (isInterrupted()) {
                        log.debug("Sending RST to the other side!");
                        resetConnection();
                        return;
//                        if (!socket.isOutputShutdown()) {
//                            log.debug("We no longer want to receive data! Closing our side of communication!");
//                            shutdownOutput();
//                        }
//                        interrupt();
                        // neukoncime vlakno, to nech bezi dalej
                    }
                    continue;

                } catch (IOException ex) {
                    log.error("Nastala ina chyba, napr Connection Reset!");
                    closeConnection();
                    return;
                }

                // narazili sme na EOF (bol nam poslany FIN), ukoncime komunikaciu - posleme FIN
                if (b == -1) {
                    //EOF
                    log.info("Other side has closed the connection! Shutting down this thread!");
                    closeConnection();
                    return;
                }


            } //while - naplnanie prvych 4 bajtov



//            while (buffer.hasRemaining()) {
//                try {
//                    socket.bytes = channel.read(buffer);
//                } catch (SocketTimeoutException ste) {
//                    log.debug("Timeout vyprsal, skusam znova!");
//                } catch (IOException ioe) {
//                    log.error("Error while reading on channel: " + ioe);
//                }
//
//                log.debug("I am here!");
//
//                // ak nas prerusili, posleme FIN, ale prijimame dalej az kym nenarazime na EOF
//                if (interrupted == true) {
//                    log.debug("We no longer want to receive data! Closing our side of communication!");
//                    shutdownOutput();
//                    // neukoncime vlakno, to nech bezi dalej
//                }
//
//                // narazili sme na EOF (bol nam poslany FIN), ukoncime komunikaciu - posleme FIN
//                if (bytes == -1) {
//                    log.debug("Other side has closed the connection! Shutting down this thread!");
//                    closeConnection();
//                    return;
//                }
//            }//while

            // ak sme sa dostali az sem. mame prve 4 bajty precitane
            buffer.flip(); // limit = pozicia, pozicia = 0

            // precitame zakladne informacie
            version = Support.unsignShort(buffer.getShort()); // verzia, pre IPFIX by mala byt 10
            length = Support.unsignShort(buffer.getShort());  // dlzka, vratane hlavicky, vsetkych setov a pripadnych paddingov
            log.debug("length = " + length + ", version = " + version);



            /* AK JE TO IPFIX, PARSE. INAK JE TO CHYBA */
            if (version != 10) {
                log.error("Data that has been received, does not represent IPFIX Message. Version = " + version);
                // connection should be closed in that case.. corrupt data on input.
                log.debug(String.format("Abortively closing connection to! %s:%d", exporterAddress.getAddress().getHostAddress(), exporterAddress.getPort()));
                resetConnection();
                return;
                // tu nam program skonci

            } else { // ak je to IPFIX sprava

                // nastavime sa tak, aby sme precitali zvysok spravy a precitame ju
                buffer.limit(length);   // prave ohranicenie
                buffer.position(4);     // lave ohranicenie; 4 bajty sme uz precitali vyssie!
                log.debug("reading full data message...");

                while (buffer.hasRemaining()) {
                    try {
                        b = in.read();
                        buffer.put((byte) b);

                    } catch (SocketTimeoutException te) {
//                        log.debug("Timeout vyprsal, skusam znova!");

                        // ak nas prerusili, posleme FIN, ale prijimame dalej az kym nenarazime na EOF
                        if (isInterrupted()) {
                            log.debug("Sending RST to the other side!");
                            resetConnection();
                            return;
//                            if (!socket.isOutputShutdown()) {
//                                log.debug("We no longer want to receive data! Closing our side of communication!");
////                                shutdownOutput();
//                                 resetConnection();
//                            }
                            //interrupt();
                            // neukoncime vlakno, to nech bezi dalej
                        }
                    } catch (IOException ex) {
                        log.error("Nastala ina chyba, napr Connection Reset!");
                        closeConnection();
//                         resetConnection();
                        return;
                    }

                    // narazili sme na EOF (bol nam poslany FIN), ukoncime komunikaciu - posleme FIN
                    if (b == -1) {
                        //EOF
                        log.info("Other side has closed the connection! Shutting down this thread!");
                        closeConnection();
                        return;
                    }


                } //while - naplnanie prvych 4 bajtov


                // sem sme sa dostali, ak sme precitali celu spravu
                long receiveTime = System.currentTimeMillis();  // po precitani celej spravy zaznamename cas prijatia

                log.debug(String.format("Incoming IPFIX message (%d bytes) ...", length));



                try {

                    /* PARSING MESSAGE */
                    buffer.flip();          // nastavime pre citanie
                    buffer.position(0);     // uistime sa, ze citame od zaciatku
                    parser.parseIpfixMessage(buffer, exporterAddress, receiveTime);

                } catch (DataFormatException dfe) {
                    log.error(dfe.getMessage());
                    log.info("Corrupted data detected! Shutting down TCP connection to " + exporterAddress);
                    resetConnection();
                    return;

                    // ak uz sablona existuje
                } catch (TemplateException ex) {
                    log.error(ex.getMessage());
                    log.info("Shutting down TCP connection to " + exporterAddress);
                    resetConnection();
                    return;
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(TCPProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }

            }//if-else version == 10
        }//nekonecny cyklus
    }// run

//    private void shutdownOutput() {
//        try {
//            if (!socket.isOutputShutdown()) {
//                socket.shutdownOutput();
//            }
//        } catch (IOException ex) {
//            log.debug("Cannot shutdown output!: " + ex.getMessage());
//
//        }
//    }
    private void closeConnection() {
        try {
//            socket.shutdownInput();
            socket.close();
        } catch (IOException e) {
            log.debug("Cannot close socket!: " + e.getMessage());
        }
    }

    private void resetConnection() {
        try {
            socket.setSoLinger(true, 0);
            socket.close();
        } catch (IOException ex) {
            log.debug("Cannot close socket!: " + ex.getMessage());
        }
    }
}
