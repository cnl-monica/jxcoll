/* 
 * Copyright (C) 2010 Lubos Kosco
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
package sk.tuke.cnl.bm.JXColl;

//import java.nio.ByteBuffer;
//import java.nio.BufferOverflowException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.IPFIX.ExporterKey;

/**
 *  This class is a packet cache. It's crucial in
 * high speed networks, because it can hold {@link IJXConstants.CACHESIZE}
 * elements (packets), therefore acting as some kind of proxy for incoming
 * traffic. Is thread safe and the cache is a FIFO.
 */
public class PacketCache {

    private static Logger log = Logger.getLogger(PacketCache.class.getName());
//    private static HashMap<ExporterKey, PriorityBlockingQueue> multiCache = new HashMap<>();
    /**
     * Variable holding cache, works as FIFO queue. It utilizes the thread
     * safety and performance of {@link ArrayBlockingQueue}
     */
    private static ArrayBlockingQueue<PacketObject> cache = new ArrayBlockingQueue<>(IJXConstants.INPUT_QUEUE_SIZE);

//    /**
//     * Puts data into priority queue for this exporter defined by source address,
//     * source UDP port and observation domain identifier.
//     * @param data
//     * @param addr 
//     */
//    public static void writeToMultiCache(ByteBuffer data, InetSocketAddress addr, long timeReceived) {
//        PacketObject packet = new PacketObject(data, addr, timeReceived);
//        ExporterKey key = new ExporterKey(packet.getAddr().getAddress(), packet.getAddr().getPort(), packet.getObservationDomain());
//        if (!multiCache.containsKey(key)) {
//            // musime vytvorit novu priority queue
//            PriorityBlockingQueue<PacketObject> queue = new PriorityBlockingQueue<>(IJXConstants.INPUT_QUEUE_SIZE);
//            // pridame ju do HashMapy
//            multiCache.put(key, queue);
//            // pridame paket do fronty
//            queue.put(packet);
//        } else {
//            multiCache.get(key).put(packet);
//        }
//    }

//    /**
//     * Retrieves PacketObject from multiCache belonging to particular Exporter
//     * given by the passed ExporterKey object. Packet should have the least 
//     * sequence number in the priority queue.
//     * @param key ExporterKey defining exporter which packets you want to receive.
//     * @return 
//     */
//    public static PacketObject getFromMultiCache(ExporterKey key) throws InterruptedException {
//        if (!multiCache.containsKey(key)) {
//            return null;
//        } else {
//            return (PacketObject) multiCache.get(key).take();
//        }
//    }
//
//    public static boolean isExporterPresent(ExporterKey key) {
//        return multiCache.containsKey(key);
//    }
//
//    public static boolean isCacheEmpty(ExporterKey key) throws JXCollException {
//        if (isExporterPresent(key)) {
//            return multiCache.get(key).isEmpty();
//        } else {
//            throw new JXCollException("Cache does not exist for particular exporter!");
//        }
//    }
//
//    public static boolean removeCacheIfEmpty(ExporterKey key) throws JXCollException {
//        if (isCacheEmpty(key)) {
//            multiCache.remove(key);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static Set<ExporterKey> getExporterKeys() {
//        return multiCache.keySet();
//    }

    /**
     * write to cache (automatically wraps input to PacketObject)
     *
     * @param data byte[] input packet data as byte array
     * @param addr InetAddress address of packet
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     * @throws NullPointerException - if input is null.
     */
    public static void write(ByteBuffer data, InetSocketAddress addr) throws InterruptedException, NullPointerException {
        PacketObject hlp = new PacketObject(data, addr);
        cache.put(hlp);
        //log.debug("Data record was inserted into PacketCache.");
        //log.error("P: PacketCache size AFTER write: "+ getSize());

    }

    public static void write(ByteBuffer data, InetSocketAddress addr, long time) throws InterruptedException {
        PacketObject hlp = new PacketObject(data, addr, time);
        cache.put(hlp);
    }

    /**
     * write PacketObject to cache
     *
     * @param data PacketObject input
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     * @throws NullPointerException - if input is null.
     */
//     public static void write(PacketObject data) throws InterruptedException,NullPointerException {
//        cache.put(data);
//     }
    /**
     * returns the first packet from the queue
     *
     * @return PacketObject packet data and address
     * @throws InterruptedException in case of thread synchronization failure,
     *   or buffer problems - if interrupted while waiting for read from cache.
     */
    public static PacketObject read() throws InterruptedException {
        PacketObject pktObject = cache.take();
//        log.debug("packet retrieved from cache");
        return pktObject;
    }

    /**
     * use it to determine current size of cache (how many packets need to be
     * processed)
     *
     * @return current size of cache
     */
    public static int getSize() {
        return cache.size();
    }
}
