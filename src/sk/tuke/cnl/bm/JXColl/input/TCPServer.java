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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.IJXConstants;

/**
 * Thread that receives IPFIX packets by TCP protocol
 * @author Tomas Veresack
 */
public class TCPServer extends Thread {

    private static Logger log = Logger.getLogger(TCPServer.class.getName());
//    private ServerSocketChannel serverChannel;
    private ServerSocket serverSocket;
    protected InetSocketAddress exporterAddress;
    private ArrayList<TCPProcessor> threads = new ArrayList<>();

    /**
     * Constructor just creates server socket and sets its timeout.
     * @param port 
     */
    public TCPServer(int port) throws SocketException, IOException {
        super("TCP Server");
        //@TODO: vyhodit z konstruktora vynimky
        log.info("Setting default input buffer: " + IJXConstants.INPUT_BUFFER_SIZE);

//        serverChannel = ServerSocketChannel.open();
//        serverChannel.configureBlocking(true);
//        serverChannel.socket().bind(new InetSocketAddress(port));
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);
        log.info("Listening on port: " + port + " (TCP)");
    }

    /**
     * Hlavná metóda vlákna. Pokiaľ nedôjde k prerušeniu, čaká na vytvorenie
     * asociácie exportérom. Vytvorený Channel predá novovytvorenému vláknu
     * SCTPProcessor a spustí ho. Dovoľuje pripojiť sa len toľkým exportérom,
     * ako je uvedené v konfiguračnom súbore.
     */
    @Override
    public void run() {
        ThreadGroup group = new ThreadGroup("TCP Processors");
        log.info("Waiting for TCP connection...");
        while (!interrupted()) {

            if (group.activeCount() == Config.maxConnections) {
                try {
                    sleep(200);
                } catch (InterruptedException ex) {
                }

            } else {
                try {

                    // exporter je moj socket
                    Socket exporter = null;
                    try {
                        exporter = serverSocket.accept();
                        log.info("Waiting for TCP connection...");
                    } catch (SocketTimeoutException soe) {
//                        log.debug("Cakam na nove spojenie...");
                        if (interrupted()) {
                            interruptAllThreads();
                            return;
                        }
                        //inak pokracuj v ackceptovani
                        continue;
                    }
//                    SocketChannel exporter = serverChannel.accept(); // tu mi to blokuje kym sa nevytvori spojenie...
//                    exporter.configureBlocking(true);
                    InetAddress adresa = exporter.getInetAddress();
                    int port = exporter.getPort();
                    exporterAddress = new InetSocketAddress(adresa, port);
//                    exporterAddress = (InetSocketAddress) exporter.getRemoteAddress();
                    log.info("TCP - Connected to exporter: " + exporterAddress);

                    TCPProcessor tcpProc = new TCPProcessor(exporter, exporterAddress, group);
                    tcpProc.start();
                    threads.add(tcpProc);

                } catch (InterruptedIOException ie) {
                    interrupt();	// znovu prerusime lebo pri hodeni vynimky sa interrupt status vynuluje
                    //group.interrupt();
                    interruptAllThreads();
                    //group.destroy();
                    log.debug("Interrupted via InterruptedException");
                } catch (IOException ex) {
                    log.error(ex);
                }
            }

            // we are still listening for next connection, even if other side closes the socket
            // thread only stops when we interrupt it
        }

        log.debug("TCP Receiver has ended!");
    }

    /**
     * Táto metóda preruší všetky bežiace vlákna SCTPProcessorov. Je volaná pri prerušení tohto 
     * vlákna.
     */
    public void interruptAllThreads() {
        // povypiname vsetky vlakna UDP procesorov
        for (TCPProcessor thread : threads) {
            if (thread.isAlive()) {
                log.debug("Interrupting " + thread.getName());
                thread.interrupt();
                try {
                    log.debug("Waiting for " + thread.getName());
                    thread.join();
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
