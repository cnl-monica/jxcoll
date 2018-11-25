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

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Set;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.IJXConstants;

/**
 * Thread that receives IPFIX packets by SCTP protocol
 * @author veri
 */
public class SCTPServer extends Thread {

    private static Logger log = Logger.getLogger(SCTPServer.class.getName());
    protected InetSocketAddress exporterAddress;
    private SctpServerChannel serverChannel;
    public static ArrayList<SCTPProcessor> threads = new ArrayList<>();

    /**
     * Constructor just creates server socket.
     * @param port 
     */
    public SCTPServer(int port) throws SocketException, IOException {
        super("SCTP Server");
        log = Logger.getLogger(SCTPServer.class.getName());
        log.info("Setting default input buffer: " + IJXConstants.INPUT_BUFFER_SIZE);

        // create server channel and bind it to specific port number
        serverChannel = SctpServerChannel.open();
        serverChannel.configureBlocking(true);
        serverChannel.bind(new InetSocketAddress(port));
        log.info("Listening on port: " + port + " (SCTP)");


    }

    /**
     * Hlavná metóda vlákna. Pokiaľ nedôjde k prerušeniu, čaká na vytvorenie
     * asociácie exportérom. Vytvorený Channel predá novovytvorenému vláknu
     * SCTPProcessor a spustí ho. Dovoľuje pripojiť sa len toľkým exportérom,
     * ako je uvedené v konfiguračnom súbore.
     */
    @Override
    public void run() {
        ThreadGroup group = new ThreadGroup("SCTP Processors");

        while (!interrupted()) {

            // ak je pocet pripojenych exporterov viac ako max, tak nerob nic
            if (group.activeCount() >= Config.maxConnections) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {
                    interruptAllThreads();
                    return;
                }

                // inak cakaj na dalsieho
            } else {

                Set<SocketAddress> exporterAddresses = null;    // primarna adresa exportera
                SctpChannel exporter = null;                    // exporter je moj socket
                Association assoc = null;

                try {
                    log.info("Waiting for SCTP association...");
                    exporter = serverChannel.accept(); // tu mi to blokuje kym sa nevytvori spojenie...

                    exporterAddresses = exporter.getRemoteAddresses();
                    assoc = exporter.association();

                } catch (InterruptedIOException ie) {
                    log.debug("SCTP Receiver was interrupted! Interrupting all SCTP threads!");
                    interrupt();	// znovu prerusime lebo pri hodeni vynimky sa interrupt status vynuluje
                    //  group.interrupt();
                    interruptAllThreads();

                    return;

                    //group.destroy();
                } catch (ClosedByInterruptException ex) {
                    interruptAllThreads();
                    log.error(ex);
                    return;
                } catch (IOException ex) {
                    log.error(ex);
                    return;
                }

                exporterAddress = (InetSocketAddress) exporterAddresses.toArray(new SocketAddress[]{})[0];


                // vypiseme, kto sa k nam pripojil - asociacia
                StringBuilder infoLog = new StringBuilder("Connected to exporter on addresses (" + exporterAddress.getPort() + "): ");
                for (SocketAddress socketAddress : exporterAddresses) {
                    infoLog.append(String.format("%s, ", ((InetSocketAddress) socketAddress).getAddress().getHostAddress()));
                }
                infoLog.delete(infoLog.length() - 2, infoLog.length() - 1);
                log.info(infoLog.toString());// koniec vypisu adries peera

                log.debug("Association: " + assoc);

                SCTPProcessor sctpProc = new SCTPProcessor(exporter, exporterAddress, group);
                sctpProc.start();
                threads.add(sctpProc);

                if (group.activeCount() >= Config.maxConnections) {
                    log.info(String.format("Maximum SCTP connections (%d) reached!", Config.maxConnections));
                }

            }

            // we are still listening for next connection, even if other side closes the socket
            // thread only stops when we interrupt it
        }

        log.debug("SCTP Receiver has ended!");
    }

    /**
     * Táto metóda preruší všetky bežiace vlákna SCTPProcessorov. Je volaná pri prerušení tohto 
     * vlákna.
     */
    public void interruptAllThreads() {
        for (SCTPProcessor thread : threads) {
            if (thread.isAlive()) {
                log.debug("Interrupting " + thread.getName());
                thread.interrupt();
                log.debug("Waiting for " + thread.getName());
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                }
            }
        }
        
        try {
            serverChannel.close();
        } catch (IOException ex) {
            log.error(ex);
        }
    }
}
