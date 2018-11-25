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
import sk.tuke.cnl.bm.DataFormatException;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * Reprezentuje jeden element sablony v IPFIX sprave
 * @author Michal Kascak
 */
public class FieldSpecifier {

    /** Enterprise cislo pre elementy mimo specifikacie IPFIX */
    private boolean enterpriseBit;
    private int elementID;
    private int fieldLength;
    private long enterpriseNumber = 0;
    private boolean isScope;

    /** Vytvara novu instanciu triedy */
    public FieldSpecifier() {
    }

    /**
     * Vytvara novy element sablony
     * @param elementID Identifikator elementu
     * @param fieldLength Velkost hodnoty elementu (pocet bytov)
     */
    public FieldSpecifier(int elementID, int fieldLength) {
        this.enterpriseBit = false;
        this.elementID = elementID;
        this.fieldLength = fieldLength;
    }

    /**
     * Vytvara novy enterprise (mimo IPFIX specifikacie) element sablony
     * @param elementID Identifikator elementu
     * @param fieldLength Velkost hodnoty elementu (pocet bytov)
     * @param enterprise Enterprise cislo elementu
     */
    public FieldSpecifier(int elementID, int fieldLength, long enterprise) {
        this.enterpriseBit = true;
        this.elementID = elementID;
        this.fieldLength = fieldLength;
        this.enterpriseNumber = enterprise;
    }

    /**
     * Nastavi tento element sablony. Metoda si sama zisto ci sa jedna o enterprise element
     * @param buffer ByteBuffer elementu
     */
    public void setFieldSpecifier(ByteBuffer buffer) throws DataFormatException {
        if (buffer.remaining() < 4) {
            throw new DataFormatException("There is no more space in the buffer while parsing field specifier!");
        }
        short first2Bytes;
        first2Bytes = buffer.getShort();
        enterpriseBit = ((first2Bytes & 0x8000) == 0x8000) ? true : false;
        elementID = first2Bytes & 0x7FFF; // vyskrtneme MSB
        fieldLength = Support.unsignShort(buffer.getShort());

        if (enterpriseBit) {
            if (enterpriseBit) {
                if (buffer.remaining() < 4) {
                    throw new DataFormatException("There is no more space in the buffer while parsing field specifier!");
                }
            }
            enterpriseNumber = Support.unsignInt(buffer.getInt());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public boolean isEnterpriseBit() {
        return enterpriseBit;
    }

//    public void setEnterpriseBit(boolean enterpriseBit) {
//        this.enterpriseBit = enterpriseBit;
//    }
    public int getElementID() {
        return elementID;
    }

//    public void setElementID(int elementID) {
//        this.elementID = elementID;
//    }
    public int getFieldLength() {
        return fieldLength;
    }

//    public void setFieldLength(int fieldLength) {
//        this.fieldLength = fieldLength;
//    }
    public long getEnterpriseNumber() {
        return enterpriseNumber;
    }

//    public void setEnterpriseNumber(int enterpriseNumber) {
//        this.enterpriseNumber = enterpriseNumber;
//    }
    /**
     * 
     * @return 
     */
    public boolean isScope() {
        return isScope;
    }

    /**
     * 
     * @param isScope 
     */
    public void setScope(boolean isScope) {
        this.isScope = isScope;
    }
    // </editor-fold>
}
