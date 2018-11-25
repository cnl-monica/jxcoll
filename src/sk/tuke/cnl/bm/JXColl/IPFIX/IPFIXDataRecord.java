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
package sk.tuke.cnl.bm.JXColl.IPFIX;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomas Verescak
 */
public class IPFIXDataRecord {

    private List<ByteBuffer> fields;
    private int referencedTemplateID;

    public IPFIXDataRecord() {
        fields = new ArrayList<>();
    }

    public IPFIXDataRecord(int templateID) {
        fields = new ArrayList<>();
        referencedTemplateID = templateID;
    }

    /**
     * Adds value of information element into list
     * @param data Hodnota elementu v poli bytov
     */
    public void addFieldValue(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        fields.add(buffer);
    }

    /**
     * Returns value of information element by index number in template
     * @param index Index number of data in data record. Matches index of element in template.
     * @return Value of data record
     */
    public byte[] getFieldValue(int position) {
        return fields.get(position).array();
    }

    /**
     * Gets template number this data record is bound with
     * @return the referencedTemplateID
     */
    public int getReferencedTemplateID() {
        return referencedTemplateID;
    }

    /**
     * Sets template number this data record is bound with
     * @param referencedTemplateID the referencedTemplateID to set
     */
    public void setReferencedTemplateID(int referencedTemplateID) {
        this.referencedTemplateID = referencedTemplateID;
    }

    /**
     * Retrieves number of IE in this data record
     * @return 
     */
    public int count() {
        return fields.size();
    }

    /**
     * Retrieves record size
     * @return 
     */
    public int recordSize() {
        int size = 0;
        for (ByteBuffer buffer : fields) {
            size += buffer.capacity();
        }
        return size;
    }
}
