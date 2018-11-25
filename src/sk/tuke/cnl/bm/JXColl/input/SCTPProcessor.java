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

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import com.sun.nio.sctp.ShutdownNotification;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.DataFormatException;
import sk.tuke.cnl.bm.JXColl.IJXConstants;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXMessage;
import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixSingleSessionTemplateCache;
import sk.tuke.cnl.bm.TemplateException;
import sk.tuke.cnl.bm.JXColl.IpfixParser;
import sk.tuke.cnl.bm.JXColl.IpfixParser.TransportProtocol;

/**
 *
 * @author veri
 */
public class SCTPProcessor extends Thread {

    private static Logger log = Logger.getLogger(SCTPProcessor.class.getName());
    private SctpChannel channel;
    private InetSocketAddress exporterAddress;
//    private Set<SocketAddress> addresses;
//    private Boolean closed = false;
    private IpfixSingleSessionTemplateCache templateCache;
    private IpfixParser parser;

    /**
     * Trieda predstavuje vlákno spracúvajúce jednu SCTP ascociáciu.
     * @param channel Objekt slúžiaci na prácu s asociáciou
     * @param exporterAddress primárna adresa SCTP ascociácie
     * @param group skupina vlákien, ku ktorej toto vlákno patrí.
     */
    public SCTPProcessor(SctpChannel channel, InetSocketAddress exporterAddress, ThreadGroup group) {
        super(group, "SCTPProcessor: " + exporterAddress.getAddress().getHostAddress() + ":"  + exporterAddress.getPort());
        this.channel = channel;
        this.exporterAddress = exporterAddress;
        this.templateCache = new IpfixSingleSessionTemplateCache();
        this.parser = new IpfixParser(this.templateCache, TransportProtocol.SCTP);
//        try {
//            this.addresses = channel.getRemoteAddresses();
//            System.out.println("Channel.association().maxInboundStreams(): " + channel.association().maxInboundStreams());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        if (channel.isConnectionPending()) {
            log.debug("Connection is pending!");
        }
//        if (channel.isOpen()) {
//            log.debug("Channel is open!");
//        }
    }

    /**
     * Hlavná metóda vlákna. Pokiaľ sa vláno nepreruší, dochádza k prijímaniu správ
     * z SCTP asociácie a spracovanie pomocou vlastného parsera. Po prerušení vlákna
     * sa asociácia vypne a vlákno končí.
     */
    @Override
    public void run() {
        //ByteBuffer buffer = ByteBuffer.allocate(IJXConstants.INPUT_BUFFER_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(IJXConstants.INPUT_BUFFER_SIZE);
        MessageInfo info = null;
        AssociationHandler assocHandler = new AssociationHandler();


        // pokial mame co citat tak citame
        thread:
        while (!interrupted()) {
//            if (closed) {
//                log.info("Other side has closed the connection!");
//                break;
//
//            }

            buffer.clear();


            try {
                //arguments: ByteBuffer, variable to set in case of notification, notification handler implementation
                info = channel.receive(buffer, null, assocHandler);
                // ked dostaneme notifikaciu a navrat bude HandlerResult.RETURN, tak tato metoda vrati null
                if (info == null) {
                    log.info("Stopping this thread!");
                    break thread;
                }

                long time = System.currentTimeMillis();

                if (info.bytes() == -1) {
                    log.info("Other side has closed the connection. Stopping this thread!");
                    break thread;
                }
//                log.debug(String.format("addr: %s, assoc: %d, bytes: %d, complete: %s, isUnordered: %s, payload_proto: %d", info.address().toString(), info.association().associationID(), info.bytes(), info.isComplete(), info.isUnordered(), info.payloadProtocolID()));

                log.debug("Message was received on stream: " + info.streamNumber());


                buffer.flip(); // zmenime mod na citanie; limit = position, positon = 0

                byte[] data = new byte[buffer.remaining()];
                for (int i = 0; buffer.hasRemaining(); i++) {
                    data[i] = buffer.get();
                }

                //sparsujeme spravu
                parser.parseIpfixMessage(ByteBuffer.wrap(data), exporterAddress, time);


            } catch (DataFormatException dfe) {
                log.error(dfe.getMessage());
                log.info("Corrupted data detected! Shutting down SCTP connection to " + exporterAddress);
                break thread;
                
            } catch (TemplateException ex) {
                log.error(ex.getMessage());
                break thread;

            } catch (InterruptedIOException e) {
                interrupt();
                log.debug("Interrupted!");

            } catch (IOException ex) {
                log.error("Error while receiving data: " + ex);

            }
        } // end of while()

        // vyskusat este spustit channel.shutdown(); - to by malo poslat SHUTDOWN, ale kanal sa neukonci, data
        // prijate do poslania shutdownu od druhej strany sa este spracuju
        // po skonceni zavrieme socket
        try {
            
            if (channel.isOpen()) {
//                channel.setOption(SctpStandardSocketOptions.SO_LINGER, 0);
                channel.shutdown();
                channel.close();
            }
        } catch (IOException ex) {
            log.error("Error while closing the connection: " + ex);
        }
//        log.info("Shutting down SCTP Processor thread!");

    } // end of run()

    /**
     * Trieda určuje, aká akcia bude nasledovať po prijatí konkrétnych notifikácií.
     */
    class AssociationHandler extends AbstractNotificationHandler {

        /**
         * Určuje, čo sa má stať po prijatí SHUTDOWN notifikácie.
         * @param sn Objekt notifikácie
         * @param o kontext
         * @return 
         */
        public HandlerResult handleNotification(ShutdownNotification sn, Object o) {
            log.debug("Association has been shut down! Shutting down " + getName());
            return HandlerResult.RETURN;
        }

        /**
         * Určuje, čo sa má stať po prijatí konkrétnej notifikácie zmeny asociácie.
         * @param acn Objekt notifikácie
         * @param t kontext
         * @return 
         */
        @Override
        public HandlerResult handleNotification(AssociationChangeNotification acn, Object t) {
            AssocChangeEvent event = acn.event();

            switch (event) {
                case CANT_START:
                    log.debug("AssocChangeEvent: CANT_START");
                    return HandlerResult.RETURN;
                case COMM_LOST:
                    log.debug("AssocChangeEvent: COMM_LOST"); // moze ist o Connection Reset alebo ABORT alebo ze peer neni connectnuty - HEARTBEAT problem
                    log.info("Connection has been lost. Shutting down " + getName());
                    return HandlerResult.RETURN;
                case COMM_UP:
                    log.debug("AssocChangeEvent: COMM_UP");
                    log.debug("Association is now ready and data may be exchanged with the peer.");
                    return HandlerResult.CONTINUE;
                case RESTART:
                    log.debug("AssocChangeEvent: RESTART");
                    log.debug("SCTP has detected that the peer has restarted.");
                    return HandlerResult.CONTINUE;
                case SHUTDOWN:
                    log.debug("AssocChangeEvent: SHUTDOWN");
                    log.info("The other side has gracefully closed this association.");
                    return HandlerResult.RETURN;
                default:
                    return HandlerResult.CONTINUE;
            }
        }
    }

    @Override
    public void interrupt() {
        try {
            channel.shutdown();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        super.interrupt();
    }
    
    
}
