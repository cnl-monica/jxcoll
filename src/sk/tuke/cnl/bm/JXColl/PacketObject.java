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

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.xbill.DNS.Serial;

/**
 * This class is the abstract object which is
 * stored in the {@linkplain PacketCache packet cache }.
 * Holds only references, so the network abstract layer needs to fill it with
 * objects which will not be overwritten. (!!!)

 */
public class PacketObject implements Comparable<PacketObject> {

    /**
     * Where did this packet came from ?
     */
    private ByteBuffer packet;      // data received
    private InetSocketAddress addr;
    private long sequenceNumber;    // data from received bytes
    private long exportTime;        // data from received bytes
    private long observationDomain; // data from received bytes
    private long timeReceived;      // not actually in IPFIX Message Header

    /**
     * Constructor doesn't make a copy of objects passed, be aware
     * of this fact ! Always make a copy of data.
     *
     * @param packet byte[] Data from a packet
     * @param addr InetAddress Where did this packet came from ?
     */
    public PacketObject(ByteBuffer packet, InetSocketAddress addr) {
        this.packet = packet;
        this.addr = addr;
        this.sequenceNumber = Support.unsignInt(packet.getInt(8));
        this.exportTime = Support.unsignInt(packet.getInt(4));
        this.observationDomain = Support.unsignInt(packet.getInt(12));
    }

    /**
     * Constructor adding also time, when the packet was received.
     * @param packet
     * @param addr
     * @param timeReceived 
     */
    public PacketObject(ByteBuffer packet, InetSocketAddress addr, long timeReceived) {
        this(packet, addr);
        this.timeReceived = timeReceived;
    }

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    public ByteBuffer getPacket() {
        return packet;
    }

    public void setPacket(ByteBuffer packet) {
        this.packet = packet;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    public void setAddr(InetSocketAddress addr) {
        this.addr = addr;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(long timeReceived) {
        this.timeReceived = timeReceived;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getExportTime() {
        return exportTime;
    }

    public long getObservationDomain() {
        return observationDomain;
    }

    // </editor-fold>
    /**
     * Porovna sekvencne cisla, ak su rovnake, tak rozhodne podla casu exportu.
     * Mozu byt rovnake sekvencne cisla, ak sa posiela sablona.
     * @param other
     * @return 
     */
    @Override
    public int compareTo(PacketObject other) {
        int result = Serial.compare(this.sequenceNumber, other.getSequenceNumber());
        if (result == 0) {
            return Long.compare(this.exportTime, other.getExportTime());
        }
        return result;
    }
}
