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
import java.util.Iterator;
import java.util.List;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * Trieda reprezentujuca jeden Set v sprave IPFIX.
 * @author Michal Kascak
 */
public class IPFIXSet {

    /*
    Set ID value identifies the Set.  A value of 2 is reserved for 
    the Template Set.  A value of 3 is reserved for the Option  
    Template Set.  All other values from 4 to 255 are reserved 
    for future use.  Values above 255 are used for Data Sets.
     */
    /**
     * Identifikator Setu
     */
    private int setID;
    /**
     * Dlzka Setu
     */
    private int length;
    /**
     * Padding na konci Setu. Pocet nulovych bytov.
     */
    public int padding; // Count of padding octets filled with zeros
    /**
     * Zoznam zaznamov sablony v Sete. Zaznamy musia byt toho isteho druhu.
     */
    public List<IPFIXTemplateRecord> templateRecords;
    /**
     * Zoznam zaznamov moznosti sablony. Zzznamy musia byt toho isteho druhu.
     */
    public List<IPFIXOptionsTemplateRecord> optionsTemplateRecords;
    /**
     * Zoznam datovych zaznamov. Zaznamy musia byt toho isteho druhu.
     */
    public List<IPFIXDataRecord> dataRecords;
    private int startOffset;

    /**
     * Konstruktor. Vytvara novu instanciu triedy.
     */
    public IPFIXSet() {
        templateRecords = new ArrayList<>();
        optionsTemplateRecords = new ArrayList<>();
        dataRecords = new ArrayList<>();
    }

    /**
     * Konstruktor. Vytvara novu instanciu triedy.
     * @param setID Identifikator Setu
     * @param length Dlzka Setu
     */
    public IPFIXSet(int setID, int length) {
        this.setID = setID;
        this.length = length;

        switch (setID) {
            case 2:
                templateRecords = new ArrayList<>();
                break;
            case 3:
                optionsTemplateRecords = new ArrayList<>();
                break;
            default:
                if (setID > 255) {
                    dataRecords = new ArrayList<>();
                } else {
                    System.out.println("Invalid SetID. It is reserved for future use");
                }
        }

    }

    /**
     * Prida zaznam sablony do Setu.
     * @param record Zaznam sablony
     */
    public void addTemplateRecord(IPFIXTemplateRecord record) {
        if (getSetID() != 2) {
            System.out.println("This set CAN NOT contain template records");
            return;
        }

        if (templateRecords != null) {
            templateRecords.add(record);
        }
    }

    /**
     * Prida zaznam moznosti sablony do Setu.
     * @param record Zaznam moznosti sablony.
     */
    public void addOptionsTemplateRecord(IPFIXOptionsTemplateRecord record) {
        if (getSetID() != 3) {
            System.out.println("This set CAN NOT contain options template records");
            return;
        }

        if (optionsTemplateRecords != null) {
            optionsTemplateRecords.add(record);
        }

    }

    /**
     * Prida datovy zaznam do Setu.
     * @param record Datovy zaznam
     */
    public void addDataRecord(IPFIXDataRecord record) {
        if (getSetID() <= 255) {
            System.out.println("This set CAN NOT contain data records");
            return;
        }

        if (dataRecords != null) {
            dataRecords.add(record);
        }
    }

    /**
     * Nastavi hlavicku Setu.
     * @param data Pole bytov hlavicky Setu.
     */
    public void setHeader(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        setHeader(buffer);
    }

    /**
     * Nastavi hlavicku Setu.
     * @param buffer ByteBuffer bytov hlavicky Setu.
     */
    @Deprecated
    public void setHeader(ByteBuffer buffer) {
        int i = 0;
        setSetID(Support.unsignShort(buffer.getShort(i)));
        i += 2;
        setLength(Support.unsignShort(buffer.getShort(i)));
        i += 2;
    }

    /**
     * Nastavi hlavicku Setu.
     * @param setID Identifikator Setu
     * @param length Dlzka Setu
     */
    public void setHeader(int setID, int length) {
        this.setSetID(setID);
        this.setLength(length);
    }

    /**
     * Vrati velkost hlavicky Setu.
     * @return Pocet bytov hlavicky Setu
     */
    public int getHeaderSize() {
        return 4;
    }

    /**
     * @return the setID
     */
    public int getSetID() {
        return setID;
    }

    /**
     * @param setID the setID to set
     */
    public void setSetID(int setID) {
        this.setID = setID;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    public void setOffsets(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return startOffset + length;
    }

    public int getSmallestDataRecordSize() {
        int min = Integer.MAX_VALUE;
        for (IPFIXDataRecord dataRecord : dataRecords) {
            min = Math.min(min, dataRecord.recordSize());
        }
        return min;
    }

    public int getSmallestTemplateRecordSize() {
        int min = Integer.MAX_VALUE;
        for (IPFIXTemplateRecord template : templateRecords) {
            min = Math.min(template.getTemplateLength(), min);
        }
        return min;
    }

    public int getSmallestOptionsTemplateRecordSize() {
        int min = Integer.MAX_VALUE;
        for (IPFIXOptionsTemplateRecord template : optionsTemplateRecords) {
            min = Math.min(template.getTemplateLength(), min);
        }
        return min;
    }
}
