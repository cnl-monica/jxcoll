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
 *  Súbor: OWDTemplateRecord.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for OWD Template Records
 * 
 */
public class OWDTemplateRecord {

    public int templateID;
    public int fieldCount;
    private List<OWDFieldSpecifier> fields;

    /**
     * Constructor for this class.
     */
    public OWDTemplateRecord() {
        fields = new LinkedList<OWDFieldSpecifier>();
    }

    /**
     * Constructor for this class.
     *
     * @param fieldCount int field count of the template.
     */
    public OWDTemplateRecord(int fieldCount){
        this.fieldCount = fieldCount;
        fields = new ArrayList<OWDFieldSpecifier>(fieldCount);
    }

    /**
     * Constructor for this class.
     *
     * @param templateID int template ID.
     * @param fieldCount int field count of the template.
     */
    public OWDTemplateRecord(int templateID, int fieldCount){
        this(fieldCount);
        this.templateID = templateID;
    }

    /**
     * Method which adds field to template.
     *
     * @param field OWDFieldSpecifier field to add.
     */
    public void addField(OWDFieldSpecifier field){
        getFields().add(field);
    }

    /**
     * Method which returns reference for the field from the template at the basis of it's index in the listing.
     *
     * @param index int of the field
     * @return OWDFieldSpecifier field in the template
     */
    public OWDFieldSpecifier getField(int index){
        return getFields().get(index);
    }

    /**
     * Method which returns reference for the field from the template at the basis of it's field ID.
     *
     * @param elementID int field ID.
     * @return OWDFieldSpecifier field in the template , null if there is no such a field.
     */
    public OWDFieldSpecifier getOWDFieldByElementID(int elementID){
        for (Iterator iter = getFields().listIterator(); iter.hasNext(); ) {
            OWDFieldSpecifier item = (OWDFieldSpecifier) iter.next();
            if (item.getElementID() == elementID) return item;
        }
        return null;
    }

    /**
     * Method which returns the list of all fields in the template.
     *
     * @return List list of all fields.
     */
    public List<OWDFieldSpecifier> getFields() {
        return fields;
    }

    /**
     * Method, which returns the index of the field at the basis of it's ID.
     *
     * @param elementID short ID of the field.
     * @return int index of the field in the template, -1 if there is no such a field.
     */
    public int getOWDFieldSpecifierPosition(short elementID){
        for(Iterator iter = fields.iterator(); iter.hasNext(); ){
            OWDFieldSpecifier fs = (OWDFieldSpecifier)iter.next();
            if(fs.getElementID() == elementID)
                return fields.indexOf(fs);
        }
        return -1;
    }

}
