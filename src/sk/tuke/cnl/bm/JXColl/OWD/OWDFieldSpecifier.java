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
 *  Súbor: OWDFieldSpecifier.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.nio.ByteBuffer;

/**
 * Class for OWD Field Specifier.
 */
public class OWDFieldSpecifier {

    private boolean enterpriseBit;
    private short elementID;
    private short fieldLength;
    private int enterpriseNumber = 0;
    private int position;

    /**
     * Constructor for the class.
     */
    public OWDFieldSpecifier() {
    }

    /**
     * Vytvara novy element sablony
     * @param elementID Identifikator elementu
     * @param fieldLength Velkost hodnoty elementu (pocet bytov)
     */
//    public OWDFieldSpecifier(short elementID, short fieldLength) {
//        this.enterpriseBit = false;
//        this.elementID = elementID;
//        this.fieldLength = fieldLength;
//    }
//
//    /**
//     * Vytvara novy enterprise (mimo IPFIX specifikacie) element sablony
//     * @param elementID Identifikator elementu
//     * @param fieldLength Velkost hodnoty elementu (pocet bytov)
//     * @param enterprise Enterprise cislo elementu
//     */
//    public OWDFieldSpecifier(short elementID, short fieldLength, int enterprise) {
//        this.enterpriseBit = true;
//        this.elementID = elementID;
//        this.fieldLength = fieldLength;
//        this.enterpriseNumber = enterprise;
//    }

//    /**
//     * Nastavi tento element sablony
//     * @param data Pole bytov elementu
//     */
//    public void setFieldSpecifier(byte[] data) {
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        setFieldSpecifier(buffer);
//    }
//
//    /**
//     * Nastavi tento element sablony. Metoda si sama zisto ci sa jedna o enterprise element
//     * @param buffer ByteBuffer elementu
//     */
//    public void setFieldSpecifier(ByteBuffer buffer) {
//        int i = 0;
//        short first2Bytes;
//        first2Bytes = buffer.getShort(i);
//        if ((first2Bytes & 0x8000) == 0x8000) {
//            enterpriseBit = true;
//        } else {
//            enterpriseBit = false;
//        }
//
//        elementID = buffer.getShort(i);
//        i += 2;
//        elementID = (short) (elementID & 0x7FFF);
//        fieldLength = buffer.getShort(i);
//        i += 2;
//        if (enterpriseBit) {
//            enterpriseNumber = buffer.getInt(i);
//        }
//    }

    /**
     * Sets this element of the template and it's position in the Data Record Set.
     * The method finds out on his own whether there
     * is an enterprise bit or not.
     *
     * @param buf ByteBuffer of the element.
     * @param position int position of the element in Data Record Set.
     */
    public void setFieldSpecifierOwd(byte[] buf, int position) {
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        int i = 0;
        short first2Bytes;
        first2Bytes = buffer.getShort(i);
        if ((first2Bytes & 0x8000) == 0x8000) {
            enterpriseBit = true;
        } else {
            enterpriseBit = false;
        }

        elementID = buffer.getShort(i);
        i += 2;
        elementID = (short) (elementID & 0x7FFF);
        fieldLength = buffer.getShort(i);
        i += 2;
        if (enterpriseBit) {
            enterpriseNumber = buffer.getInt(i);
        }
        this.setPosition(position);
    }
    
     // <editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * @return the enterpriseBit
     */
    public boolean isEnterpriseBit() {
        return enterpriseBit;
    }

    /**
     * @param enterpriseBit the enterpriseBit to set
     */
    public void setEnterpriseBit(boolean enterpriseBit) {
        this.enterpriseBit = enterpriseBit;
    }

    /**
     * @return the elementID
     */
    public short getElementID() {
        return elementID;
    }

    /**
     * @param elementID the elementID to set
     */
    public void setElementID(short elementID) {
        this.elementID = elementID;
    }

    /**
     * @return the fieldLength
     */
    public short getFieldLength() {
        return fieldLength;
    }

    /**
     * @param fieldLength the fieldLength to set
     */
    public void setFieldLength(short fieldLength) {
        this.fieldLength = fieldLength;
    }

    /**
     * @return the enterpriseNumber
     */
    public int getEnterpriseNumber() {
        return enterpriseNumber;
    }

    /**
     * @param enterpriseNumber the enterpriseNumber to set
     */
    public void setEnterpriseNumber(int enterpriseNumber) {
        this.enterpriseNumber = enterpriseNumber;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }
    // </editor-fold>

}
