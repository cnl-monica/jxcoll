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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import sk.tuke.cnl.bm.JXColl.Config;

/**
 * Trieda reprezentujuca zaznam sablony.
 * @author Michal Kascak
 */
public class IPFIXTemplateRecord {

    /**
     * Identifikator sablony
     */
    protected int templateID;
    /**
     * Pocet poli sablony.
     */
    protected int fieldCount;
    /**
     * Zoznam vsetkych poli sablony
     */
    protected List<FieldSpecifier> fields;
    protected long lastReceived;
    protected long sequenceNumber;
    protected int dataRecordLength;
    protected boolean isOfVariableLength;
    protected int templateLength;

    /**
     * Vytvara novu instanciu triedy.
     */
    public IPFIXTemplateRecord() {
        fields = new ArrayList<>();
    }

    /**
     * Vytvara novu instanciu triedy.
     * @param fieldCount Pocet poli sablony
     */
    public IPFIXTemplateRecord(int fieldCount) {
        this.fieldCount = fieldCount;
        fields = new ArrayList<>(fieldCount);
    }

    /**
     * Vytvara novu instanciu triedy.
     * @param templateID Identifikator sablony
     * @param fieldCount Pocet poli sablony
     */
    public IPFIXTemplateRecord(int templateID, int fieldCount) {
        this(fieldCount);
        this.templateID = templateID;
    }

    /**
     * Vytvori a prida pole do sablony
     * @param elementID Identifikator elementu (pole sablony)
     * @param fieldLength Dlzka elementu v bytoch
     */
    public void addField(short elementID, short fieldLength) {
        FieldSpecifier field = new FieldSpecifier(elementID, fieldLength);
        addField(field);
    }

    /**
     * Vytvori a prida pole do sablony
     * @param elementID Identifikator elementu (pole sablony)
     * @param fieldLength Dlzka elementu v bytoch
     * @param enterprise Enterprise cislo pola
     */
    public void addField(short elementID, short fieldLength, int enterprise) {
        FieldSpecifier field = new FieldSpecifier(elementID, fieldLength, enterprise);
        addField(field);
    }

//    /**
//     * Vytvori a prida pole do sablony
//     * @param data Pole bytov pola sablony
//     */
//    @Deprecated
//    public void addField(byte[] data) {
//        FieldSpecifier field = new FieldSpecifier();
//        field.setFieldSpecifier(data);
//        addField(field);
//    }
    /**
     * Prida existujuce pole do sablony
     * @param field Pole sablony
     */
    public void addField(FieldSpecifier field) {
        fields.add(field);
//        setDataRecordLength(getDataRecordLength() + field.getFieldLength());
//        if (field.getFieldLength() == 65535) {
//            isOfVariableLength = true;
//        }
    }

    /**
     * Vrati referenciu na pole zo sablony podla indexu v zozname
     * @param index Index pola
     * @return Pole sablony
     */
    public FieldSpecifier getField(int index) {
        return fields.get(index);
    }

    /**
     * Vrati referenciu na pole zo sablony podla identifikatora pola
     * @param elementID Identifikator pola
     * @return Pole sablony, , null ak take pole v sablone neexistuje
     */
    public FieldSpecifier getFieldByElementID(int elementID) {
        for (Iterator iter = fields.listIterator(); iter.hasNext();) {
            FieldSpecifier item = (FieldSpecifier) iter.next();
            if (item.getElementID() == elementID) {
                return item;
            }
        }
        return null;
    }

    /**
     * Vrati zoznam vsetkych poli v tejto sablone
     * @return Zoznam poli
     */
    public List<FieldSpecifier> getFields() {
        return fields;
    }

    /**
     * Vrati index pola sablony podla jeho identifikatora
     * @param elementID Identidikotor pola
     * @return Index pola v zozname poli sablony, -1 ak take pole v sablone nieje
     */
    public int getFieldSpecifierPosition(int elementID) {
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            FieldSpecifier fs = (FieldSpecifier) iter.next();
            if (fs.getElementID() == elementID) {
                return fields.indexOf(fs);
            }
        }
        return -1;
    }

    public boolean isValid() {
        return (System.currentTimeMillis() - lastReceived) / 1000 < Config.IPFIX_TEMPLATE_TIMEOUT;
    }

    /**
     * @return the templateID
     */
    public int getTemplateID() {
        return templateID;
    }

    /**
     * @param templateID the templateID to set
     */
    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    /**
     * @return the fieldCount
     */
    public int getFieldCount() {
        return fieldCount;
    }

    /**
     * @param fieldCount the fieldCount to set
     */
    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(long lastReceived) {
        this.lastReceived = lastReceived;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getDataRecordLength() {
        int length = 0;
        for (FieldSpecifier fs : fields) {
            length += fs.getFieldLength();
        }
        return length;
    }

//    public void setDataRecordLength(int dataRecordLength) {
//        this.dataRecordLength = dataRecordLength;
//    }
    public boolean isOfVariableLength() {
        for (FieldSpecifier fs : fields) {
            if (fs.getFieldLength() == 65535) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Template[id:%s, fieldCount:%d, received: %s]", templateID, fieldCount, new Date(lastReceived));
    }

    public int getTemplateLength() {
        if (templateLength != 0) {
            return templateLength;
        }

        int length = 4; // hlavicka ma 4 bajty TemplateID + fieldCount
        for (FieldSpecifier fs : fields) {
            length += 4; // IE ID + length
            if (fs.isEnterpriseBit()) {
                length += 4; // Enterprise number
            }
        }
        return length;
    }
}
