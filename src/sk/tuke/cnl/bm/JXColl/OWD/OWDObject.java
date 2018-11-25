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
 *  Súbor: OWDObject.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class is the abstract object which is stored in the {@linkplain OWDCache cacheA and cacheB }.
 * Holds only references, so the network abstract layer needs to fill it with
 * objects which will not be overwritten.
 * 
 * @author Adrian Pekar
 */
public class OWDObject {

    /**
     * Information elements needed for OWD measurement
     */
    private long timeStamp;
    private short protocolIdentifier;
    private int sourceTransportPort;
    private Inet4Address sourceIPv4Address;
    private int destinationTransportPort;
    private Inet4Address destinationIPv4Address;
    private short ipVersion;
    private long observationPointID;
    private long flowID;
    private BigInteger flowStart;
    private BigInteger flowEnd;
    private byte[] firstPacketID = new byte [16];
    private byte[] lastPacketID = new byte [16];
    private byte[] packet;
    private InetSocketAddress addr;

    /**
     * The only constructor, it doesn't make a copy of objects passed, be aware
     * of this fact.
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
     */
    public OWDObject(long timeStamp, short protocolIdentifier, int sourceTransportPort, Inet4Address sourceIPv4Address,
            int destinationTransportPort, Inet4Address destinationIPv4Address, short ipVersion, long observationPointID,
            long flowID, BigInteger flowStart,
            BigInteger flowEnd, byte[] firstpacketID, byte[] lastPacketID, byte[] packet, InetSocketAddress addr){
        this.timeStamp = timeStamp;
        this.protocolIdentifier = protocolIdentifier;
        this.sourceTransportPort = sourceTransportPort;
        this.sourceIPv4Address = sourceIPv4Address;
        this.destinationTransportPort = destinationTransportPort;
        this.destinationIPv4Address = destinationIPv4Address;
        this.ipVersion = ipVersion;
        this.observationPointID = observationPointID;
        this.flowID = flowID;
        this.flowStart = flowStart;
        this.flowEnd = flowEnd;
        this.firstPacketID = firstpacketID;
        this.lastPacketID = lastPacketID;
        this.packet = packet;
        this.addr = addr;
    }
  
    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    /**
     * @return the timeStamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the protocolIdentifier
     */
    public short getProtocolIdentifier() {
        return protocolIdentifier;
    }

    /**
     * @return the sourceTransportPort
     */
    public int getSourceTransportPort() {
        return sourceTransportPort;
    }

    /**
     * @return the sourceIPv4Address
     */
    public Inet4Address getSourceIPv4Address() {
        return sourceIPv4Address;
    }

    /**
     * @return the destinationTransportPort
     */
    public int getDestinationTransportPort() {
        return destinationTransportPort;
    }

    /**
     * @return the destinationIPv4Address
     */
    public Inet4Address getDestinationIPv4Address() {
        return destinationIPv4Address;
    }

    /**
     * @return the ipVersion
     */
    public short getIpVersion() {
        return ipVersion;
    }

    /**
     * @return the observationPointID
     */
    public long getObservationPointID() {
        return observationPointID;
    }

    /**
     * @return the flowID
     */
    public long getFlowID() {
        return flowID;
    }

    /**
     * @return the flowStart
     */
    public BigInteger getFlowStart() {
        return flowStart;
    }

    /**
     * @return the flowEnd
     */
    public BigInteger getFlowEnd() {
        return flowEnd;
    }

    /**
     * @return the firstPacketID
     */
    public byte[] getFirstPacketID() {
        return firstPacketID;
    }

    /**
     * @return the lastPacketID
     */
    public byte[] getLastPacketID() {
        return lastPacketID;
    }

    /**
     * @return the packet
     */
    public byte[] getPacket() {
        return packet;
    }

    /**
     * @return the addr
     */
    public InetSocketAddress getAddr() {
        return addr;
    }// </editor-fold>

}
