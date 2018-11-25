package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import sk.tuke.cnl.bm.*;
import java.util.*;
import sk.tuke.cnl.bm.JXColl.Config;

/**
 * <p>Title: JXColl</p>
 *
 * <p>Description: Java XML Collector for network protocols</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: TUKE, FEI, CNL</p>
 * class representing a Netflow 9 Template which also serves as data container
 * @author Lubos Kosco
 * @version 0.1
 */

public class NF9Template {
    /** Poznamka !!!   iba a IBA v tejto strukture su ukladane automaticky unsigned hodnoty !!!  vsade inde je ich nutne najprv unsignX-ovat */
    /** identifier of this template (int value from 255 and higher) */
     public Integer templateID;
     /** count of all fields int this template*/
     public int fieldCount;
     /** flagged bit field for easy recognition for filtering used in DC* classes ... */
     public int filter_priznak=SimpleFilter.MP_FLAG; // IP MP mam stale ;)

     private int size=0;
/* The Template lifetime at the Collecting Process MUST be at least 3 times higher that the Template refresh timeout configured on the Exporting Process.   */
     /** list of all fields*/
     public List<FieldObject> Fields;

    /**
     * constructor, if we don't know how many fields we have (the list is
     * created dynamically, little bit slower)
     */
    public NF9Template() {
        Fields = new LinkedList<FieldObject>();
    }

    /**
     * constructor for the template with known size (count) of fields
     * fixed size means we cannot really change the count of fields, once it's allocated with this constructor
     *
     * @param fieldCount int count of fields
     */
//    public NF9Template(int fieldCount) {
//        this.fieldCount=fieldCount;
//        Fields = new ArrayList<FieldObject>(fieldCount);
//        expireTime = new Date();
//        expireCount=0;
//    }

    /**
     * add a Field to the template
     *
     * @param type int type of this field
     * @param length int its length in bytes
     */
    public void addField(int type, int length) {
        size+=length;
        Fields.add( new FieldObject(type,length) );
        // filter priznak ... ak priznak & filter od analyzeru daju ako vysledok filter od analyzeru tak flow moze ist dalej (vyhovuje filtru)
        switch (type) {
        case Templates.IPV4_SRC_ADDR:filter_priznak|=SimpleFilter.SRC_IP_FLAG;
            break;
        case Templates.IPV4_DST_ADDR:filter_priznak|=SimpleFilter.DST_IP_FLAG;
            break;
        case Templates.L4_SRC_PORT:filter_priznak|=SimpleFilter.SRC_PORT_FLAG;
            break;
        case Templates.L4_DST_PORT:filter_priznak|=SimpleFilter.DST_PORT_FLAG;
            break;
        case Templates.PROTOCOL:filter_priznak|=SimpleFilter.PROTOCOL_FLAG;
            break;
        }
    }

    /**
     * returns the field by its index (in the list)
     *
     * @param index int absolute position of field in list
     * @return FieldObject field from index
     */
    public FieldObject getField(int index) {
        return (FieldObject) Fields.get(index);
    }

    /**
     * returns the first field in the field list with the given type
     *
     * @param type int type of this field
     * @return FieldObject first field with matching type
     */
    public FieldObject getFieldByType(int type) {
        for (Iterator iter = Fields.listIterator(); iter.hasNext(); ) {
            FieldObject item = (FieldObject) iter.next();
            if (item.getType()==type) return item;
        }
        return null; // teoreticky by sa to nemalo stat, kedze kontrolujeme flagy
    }

    /**
     * get all Fields as an iterator
     *
     * @return ListIterator
     */
    public ListIterator getFields() {
        return Fields.listIterator();
    }
    /**
     * return the size of this header
     *
     * @return int size in bytes
     */
    public int getSize(){
        return size;
    }
    
    
}