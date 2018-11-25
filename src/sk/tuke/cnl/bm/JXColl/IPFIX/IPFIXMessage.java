/* 
 * Copyright (C) 2010 Michal Kascak
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
package sk.tuke.cnl.bm.JXColl.IPFIX;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * Trieda reprezentujuca IPFIX spravu posielanu exporterom kolektoru. Jej struktura je definovana v IPFIX standarde.
 * @author Michal Kascak
 */
public class IPFIXMessage {

    /**
     * Kolekcia Set-ov v IPFIX sprave
     */
    private int versionNumber;
    private int length;
    private long exportTime;
    private long sequenceNumber;
    private long observationDomainID;
    private List<IPFIXSet> sets;
    private long receiveTime;

    /**
     * Konstruktor. Vytvara novu instanciu triedy.
     */
    public IPFIXMessage() {
        sets = new ArrayList<>();
    }

    /**
     * Nastavi hlavicku spravy
     * @param data Pole bytov hlavi�ky
     */
    public void setHeader(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        setHeader(buffer);
    }

    /**
     * Nastavi hlavicku spravy
     * @param buffer ByteBuffer bytov hlavicky.
     */
    public void setHeader(ByteBuffer buffer) {
        //int i = 0;
        versionNumber = Support.unsignShort(buffer.getShort()); //i+=2;
        length = Support.unsignShort(buffer.getShort());// i+=2;
        exportTime = Support.unsignInt(buffer.getInt());// i+=4;
        sequenceNumber = Support.unsignInt(buffer.getInt());// i+=4;
        observationDomainID = Support.unsignInt(buffer.getInt());// i+=4;
    }

    /**
     * Prida Set do spravy
     * @param set Set, ktora sa prida do spravy
     */
    public void addSet(IPFIXSet set) {
        sets.add(set);
    }

    /**
     * Vrati velkost hlavicky spravy IPFIX
     * @return Pocet bytov hlavicky
     */
    public int getHeaderSize() {
        return 16;
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getExportTime() {
        return exportTime;
    }

    public void setExportTime(long exportTime) {
        this.exportTime = exportTime;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getObservationDomainID() {
        return observationDomainID;
    }

    public void setObservationDomainID(long observationDomainID) {
        this.observationDomainID = observationDomainID;
    }

    public List<IPFIXSet> getSets() {
        return sets;
    }

    public void setSets(List<IPFIXSet> sets) {
        this.sets = sets;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }
    // </editor-fold>
}
