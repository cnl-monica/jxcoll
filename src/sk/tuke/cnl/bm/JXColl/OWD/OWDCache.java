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
 *  Súbor: OWDCache.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.PacketCache;
import sk.tuke.cnl.bm.JXColl.export.MongoClient;

/**
 * This class is an inter-cache with methods for OWD match criteria and computing OWD values.
 * It's acting as some kind of proxy for incoming traffic.
 *
 * @author Adrian Pekar
 */
public class OWDCache {

    private static Logger log = Logger.getLogger(OWDCache.class.getName());
    private static LinkedList<OWDObject> cacheA = new LinkedList<OWDObject>();
    private static LinkedList<OWDObject> cacheB = new LinkedList<OWDObject>();
    private static MongoClient mongoClient;
    private static boolean listenerLock;
    private static boolean flushLock;

    /**
     * Set level of logging for this class.
     *
     * @param level String Log Level.
     */
    public void setlogl(String level) {
        log.setLevel(org.apache.log4j.Level.toLevel(level));
    }

    /**
     * The only constructor, it's making connection to the DB.
     */
    public OWDCache(){
        log.info("OWD: Creating database connection for OWD data...");
        mongoClient = new MongoClient();
    }

    /**
     * Method for OWD value computing. If the cacheB is empty it puts the new object to it's own cacheA.
     * If there is an element in the cacheB, it checks for the OWD matching criteria.
     * If the criteria are satisfied, OWD value will be computed and sent to the DB.
     *
     * @param timeStamp long time of write into the cache.
     * @param protocolIdentifier short protocolIdentifier.
     * @param sourceTransportPort int sourceTransportPort.
     * @param sourceIPv4Address Inet4Address sourceIPv4Address.
     * @param destinationTransportPort int destinationTransportPort.
     * @param destinationIPv4Address Inet4Address destinationIPv4Address.
     * @param ipVersion short ipVersion.
     * @param observationPointID long observationPointID.
     * @param flowID long flowID.
     * @param flowStart BigInteger flowStartNanoseconds.
     * @param flowEnd BigInteger flowEndNanoseconds.
     * @param firstpacketID byte[] firstPacketID.
     * @param lastPacketID byte[] lastPacketID.
     * @param packet byte[] data from packet.
     * @param addr InetAddress where did this packet came from.
     * @throws InterruptedException  if interrupted while waiting for write to cache.
     * @throws NullPointerException  if input is null.
     * @throws SQLException  if there is an error on database access or other errors.
     */
    public void pushA(long timeStamp, short protocolIdentifier, int sourceTransportPort, Inet4Address sourceIPv4Address,
            int destinationTransportPort, Inet4Address destinationIPv4Address, short ipVersion, long observationPointID, long flowID,
            BigInteger flowStart, BigInteger flowEnd, byte[] firstPacketID, byte[] lastPacketID, byte[] packet,
            InetSocketAddress addr) throws InterruptedException, NullPointerException, SQLException {
        boolean foundMatch = false;

//        log.debug("OWD: cacheA: writeTime: " + timeStamp);
//        log.debug("OWD: cacheA: protocolIdentifier: "+ protocolIdentifier);
//        log.debug("OWD: cacheA: sourceTransportPort: "+ sourceTransportPort);
//        log.debug("OWD: cacheA: sourceIPv4Address: " + sourceIPv4Address.getHostAddress());
//        log.debug("OWD: cacheA: destinationTransportPort: " + destinationTransportPort);
//        log.debug("OWD: cacheA: destinationIPv4Address: "+ destinationIPv4Address.getHostAddress());
//        log.debug("OWD: cacheA: ipVersion: " + ipVersion);
//        log.debug("OWD: cacheA: observationPointID: " + observationPointID);
//        log.debug("OWD: cacheA: flowID: " + flowID);
//        log.debug("OWD: cacheA: flowStartNanoseconds: " + flowStart);
//        log.debug("OWD: cacheA: flowEndNanoseconds: " + flowEnd);
//        log.debug("OWD: cacheA: firstPacketID: " + getHex(firstPacketID));
//        log.debug("OWD: cacheA: lastPacketID : " + getHex(lastPacketID));
        OWDObject tmp = new OWDObject(timeStamp, protocolIdentifier, sourceTransportPort, sourceIPv4Address, destinationTransportPort,
                destinationIPv4Address, ipVersion, observationPointID, flowID, flowStart, flowEnd, firstPacketID, lastPacketID, packet, addr);
synchronized(cacheA){
synchronized(cacheB){
        if (cacheB.isEmpty()) {
            //log.debug("OWD: cacheA: Writing to EMPTY A intercache...intercache size before writing: " + getNumberOfElementsA());
            cacheA.add(tmp);
            foundMatch = true;
        } else {
            for (int i = 0; i < cacheB.size(); i++) {

                if (compareKeys(tmp, cacheB.get(i))) {
                    if (compareFirstPacketID(tmp, cacheB.get(i))) {
                        long owd = (tmp.getFlowStart().subtract(cacheB.get(i).getFlowStart())).longValue();
//                        log.debug("OWD: cacheA: "+Long.toHexString(tmp.getFlowStart().longValue()));
//                        log.debug("OWD: cacheA: "+Long.toHexString(cacheB.get(i).getFlowStart().longValue()));
                        log.debug("OWD: cacheA: OWD for firstPacketID: " + Math.abs(owd));
                        double rid = mongoClient.getNextSequenceNumber("bm.owd_id_owd_seq");
                        mongoClient.insertdata("owd", "{'id_owd' : '" +rid + "', 'owdStartObservationPointID' : '" + Config.owdStartObsPoID + "', 'owdEndObservationPointID' : '" + Config.owdEndObsPoID + "', 'flowID' : '" + tmp.getFlowID() + "', 'owdNanoseconds' : '" + Math.abs(owd) + "'}");
                        foundMatch = true;
                    }
                    if (compareLastPacketID(tmp, cacheB.get(i))) {
                        long owd = (tmp.getFlowEnd().subtract(cacheB.get(i).getFlowEnd())).longValue();
//                        log.debug("OWD: cacheA: "+Long.toHexString(tmp.getFlowStart().longValue()));
//                        log.debug("OWD: cacheA: "+Long.toHexString(cacheB.get(i).getFlowStart().longValue()));
                        log.debug("OWD: cacheA: OWD for lastPacketID: " + Math.abs(owd));
                        double rid = mongoClient.getNextSequenceNumber("bm.owd_id_owd_seq");
                        mongoClient.insertdata("owd", "{'id_owd' : '" +rid + "', 'owdStartObservationPointID' : '" + Config.owdStartObsPoID + "', 'owdEndObservationPointID' : '" + Config.owdEndObsPoID + "', 'flowID' : '" + tmp.getFlowID() + "', 'owdNanoseconds' : '" + Math.abs(owd) + "'}");
                        foundMatch = true;
                    }
                    if (foundMatch) {
                        PacketCache.write(ByteBuffer.wrap(packet), addr);
                        PacketCache.write(ByteBuffer.wrap(cacheB.get(i).getPacket()), cacheB.get(i).getAddr());
                        cacheB.remove(i);
                    }
                }
            }
        }
        if (foundMatch == false) {
            //log.debug("OWD: cacheA: Writing to A intercache...intercache size before writing: " + getNumberOfElementsA());
            cacheA.add(tmp);
        }
}
}

    }

    /**
     * Method for OWD value computing. If the cacheA is empty it puts the new object to it's own cacheB.
     * If there is an element in the cacheA, it checks for the OWD matching criteria.
     * If the criteria are satisfied, OWD value will be computed and sent to the DB.
     *
     * @param timeStamp long time of write into the cache.
     * @param protocolIdentifier short protocolIdentifier.
     * @param sourceTransportPort int sourceTransportPort.
     * @param sourceIPv4Address Inet4Address sourceIPv4Address.
     * @param destinationTransportPort int destinationTransportPort.
     * @param destinationIPv4Address Inet4Address destinationIPv4Address.
     * @param ipVersion short ipVersion.
     * @param observationPointID long observationPointID.
     * @param flowID long flowID.
     * @param flowStart BigInteger flowStartNanoseconds.
     * @param flowEnd BigInteger flowEndNanoseconds.
     * @param firstpacketID byte[] firstPacketID.
     * @param lastPacketID byte[] lastPacketID.
     * @param packet byte[] data from packet.
     * @param addr InetAddress where did this packet came from.
     * @throws InterruptedException  if interrupted while waiting for write to cache.
     * @throws NullPointerException  if input is null.
     * @throws SQLException  if there is an error on database access or other errors.
     */
    public void pushB(long timeStamp, short protocolIdentifier, int sourceTransportPort, Inet4Address sourceIPv4Address,
            int destinationTransportPort, Inet4Address destinationIPv4Address, short ipVersion, long observationPointID, long flowID,
            BigInteger flowStart, BigInteger flowEnd, byte[] firstPacketID, byte[] lastPacketID, byte[] packet,
            InetSocketAddress addr) throws InterruptedException, NullPointerException, SQLException {
        boolean foundMatch = false;

//        log.debug("OWD: cacheB: writeTime: " + timeStamp);
//        log.debug("OWD: cacheB: protocolIdentifier: "+ protocolIdentifier);
//        log.debug("OWD: cacheB: sourceTransportPort: "+ sourceTransportPort);
//        log.debug("OWD: cacheB: sourceIPv4Address: " + sourceIPv4Address.getHostAddress());
//        log.debug("OWD: cacheB: destinationTransportPort: " + destinationTransportPort);
//        log.debug("OWD: cacheB: destinationIPv4Address: "+ destinationIPv4Address.getHostAddress());
//        log.debug("OWD: cacheB: ipVersion: " + ipVersion);
//        log.debug("OWD: cacheB: observationPointID: " + observationPointID);
//        log.debug("OWD: cacheB: flowID: " + flowID);
//        log.debug("OWD: cacheB: flowStartNanoSeconds: " + flowStart);
//        log.debug("OWD: cacheB: flowEndNanoSeconds: " + flowEnd);
//        log.debug("OWD: cacheB: firstPacketID: " + getHex(firstPacketID));
//        log.debug("OWD: cacheB: lastPacketID : " + getHex(lastPacketID));
        OWDObject tmp = new OWDObject(timeStamp, protocolIdentifier, sourceTransportPort, sourceIPv4Address, destinationTransportPort,
                destinationIPv4Address, ipVersion, observationPointID, flowID, flowStart, flowEnd, firstPacketID, lastPacketID, packet, addr);
synchronized(cacheA){
synchronized(cacheB){
        if (cacheA.isEmpty()) {
            //log.debug("OWD: cacheB: Writing to EMPTY B intercache...intercache size before writing: " + getNumberOfElementsB());
            cacheB.add(tmp);
            foundMatch = true;
        } else {
            for (int i = 0; i < cacheA.size(); i++) {
                if (compareKeys(tmp, cacheA.get(i))) {
                    if (compareFirstPacketID(tmp, cacheA.get(i))) {
                        long owd = (tmp.getFlowStart().subtract(cacheA.get(i).getFlowStart())).longValue();
//                        log.debug("OWD: cacheB: "+Long.toHexString(tmp.getFlowStart().longValue()));
//                        log.debug("OWD: cacheB: "+Long.toHexString(cacheA.get(i).getFlowStart().longValue()));
                        log.debug("OWD: cacheB: OWD for firstPacketID: " + Math.abs(owd));
                        double rid = mongoClient.getNextSequenceNumber("bm.owd_id_owd_seq");
                        mongoClient.insertdata("owd", "{'id_owd' : '"+rid + "', 'owdStartObservationPointID' : '" + Config.owdStartObsPoID + "', 'owdEndObservationPointID' : '" + Config.owdEndObsPoID + "', 'flowID' : '" + tmp.getFlowID() + "', 'owdNanoseconds' : '" + Math.abs(owd) + "'}");
                        foundMatch = true;
                    }
                    if (compareLastPacketID(tmp, cacheA.get(i))) {
                        long owd = (tmp.getFlowEnd().subtract(cacheA.get(i).getFlowEnd())).longValue();
//                        log.debug("OWD: cacheB: "+Long.toHexString(tmp.getFlowStart().longValue()));
//                        log.debug("OWD: cacheB: "+Long.toHexString(cacheA.get(i).getFlowStart().longValue()));
                        log.debug("OWD: cacheB: OWD for lastPacketID: " + Math.abs(owd));
                        double rid = mongoClient.getNextSequenceNumber("bm.owd_id_owd_seq");
                        mongoClient.insertdata("owd", "{'id_owd' : '"+rid + "', 'owdStartObservationPointID' : '" + Config.owdStartObsPoID + "', 'owdEndObservationPointID' : '" + Config.owdEndObsPoID + "', 'flowID' : '" + tmp.getFlowID() + "', 'owdNanoseconds' : '" + Math.abs(owd) + "'}");
                        foundMatch = true;
                    }
                    if (foundMatch) {
                        PacketCache.write(ByteBuffer.wrap(packet), addr);
                        PacketCache.write(ByteBuffer.wrap(cacheA.get(i).getPacket()), cacheA.get(i).getAddr());
                        cacheA.remove(i);
                    }
                }
            }
        }
        if (foundMatch == false){
            //log.debug("OWD: cacheB: Writing to B intercache...intercache size before writing: " + getNumberOfElementsB());
            cacheB.add(tmp);
        }
}
}
    }

    /**
     * Returns the element at the specified position from cacheA.
     *
     * @param i int the specified position in cacheA.
     * @return OWDObject the element at the specified position.
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     */
    public static OWDObject readA(int i) throws InterruptedException {
		synchronized(cacheA){
			return cacheA.get(i);
		}
    }

     /**
     * Returns the element at the specified position from cacheB.
     *
     * @param i int the specified position in cacheB.
     * @return OWDObject the element at the specified position.
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     */
    public static OWDObject readB(int i) throws InterruptedException {
		synchronized(cacheB){
			return cacheB.get(i);
		}
    }

    /**
     * Removes the element at the specified position in cacheA.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param i int the specified position in cacheA.
     * @return OWDObject the element that was removed from cacheA.
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     */
    public static OWDObject pullA(int i) throws InterruptedException {
		synchronized(cacheA){
			return cacheA.remove(i);
		}
    }

    /**
     * Removes the element at the specified position in cacheB.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param i int the specified position in cacheB.
     * @return OWDObject the element that was removed from cacheB.
     * @throws InterruptedException - if interrupted while waiting for write to cache.
     */
    public static OWDObject pullB(int i) throws InterruptedException {
		synchronized(cacheB){
			return cacheB.remove(i);
		}
    }

    /**
     * Returns the number of elements in cacheA.
     *
     * @return int number of elements in cacheA.
     */
    public static int getNumberOfElementsA() {
		synchronized(cacheA){
			return cacheA.size();
		}
    }

    /**
     * Returns the number of elements in cacheB.
     *
     * @return int number of elements in cacheB.
     */
    public static int getNumberOfElementsB() {
		synchronized(cacheB){
			return cacheB.size();
		}
    }

    /**
     * Method which compares two object's information elements for OWD computation.
     *
     * @param a OWDObject object A with information elements to compare.
     * @param b OWDObject object B with information elements to compare.
     * @return boolean true if there is a match, false if not.
     */
    public static boolean compareKeys(OWDObject a, OWDObject b) {

        if (a.getIpVersion() != b.getIpVersion()) {
            return false;
        }
        if (a.getProtocolIdentifier() != b.getProtocolIdentifier()) {
            return false;
        }
        if (a.getSourceIPv4Address().equals(b.getSourceIPv4Address())) {
            if (a.getSourceTransportPort() == b.getSourceTransportPort()) {
                if (a.getDestinationIPv4Address().equals(b.getDestinationIPv4Address())) {
                    if (a.getDestinationTransportPort() == b.getDestinationTransportPort()) {
                        return true;
                    }
                }
            }
        }

        if (a.getSourceIPv4Address().equals(b.getDestinationIPv4Address())) {
            if (a.getSourceTransportPort() == b.getDestinationTransportPort()) {
                if (a.getDestinationIPv4Address().equals(b.getSourceIPv4Address())) {
                    if (a.getDestinationTransportPort() == b.getSourceTransportPort()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method which returns the hexadecimal representation of a byte[] object.
     *
     * @param raw byte[] value we want to convert in hexadecimal representation.
     * @return String hexadecimal representation of the raw input data.
     */
    public static String getHex(byte[] raw) {
        String HEXES = "0123456789ABCDEF";

        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Method for comparing two FIRSTpacketIDs.
     *
     * @param a OWDObject object A.
     * @param b OWDObject object B.
     * @return boolean true if they are equal, false if not.
     */
    public static boolean compareFirstPacketID(OWDObject a, OWDObject b) {
        if(Arrays.equals(a.getFirstPacketID(), b.getFirstPacketID())){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method for comparing two LASTpacketIDs.
     *
     * @param a OWDObject object A.
     * @param b OWDObject object B.
     * @return boolean true if they are equal, false if not.
     */
    public static boolean compareLastPacketID(OWDObject a, OWDObject b) {
        if (Arrays.equals(a.getLastPacketID(), b.getLastPacketID())){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method for disconnecting from DB.
     */
    public static void closeDBConnection() {
		mongoClient.disconnect();
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * @return the lock
     */
    public static boolean getOwdListenerLock() {
        return listenerLock;
    }

    /**
     * @param lockA the lock to set
     */
    public static void setOwdListenerLock(boolean lockA) {
        listenerLock = lockA;
    }

    /**
     * @return the oLock
     */
    public static boolean getOwdFlushLock() {
        return flushLock;
    }

    /**
     * @param lockB the oLock to set
     */
    public static void setOwdFlushLock(boolean lockB) {
        flushLock = lockB;
    }// </editor-fold>

}