/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm.JXColl.IPFIX;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author veri
 */
public class IPFIXOptionsTemplateRecord extends IPFIXTemplateRecord {

    /**
     * Identifikator sablony (musi byt vacsie ako 255)
     */
//    private int templateID;
    /**
     * Pocet poli sablony (vratane scope poli).
     */
//    private int fieldCount;
    /**
     * Pocet scope poli sablony (nesmie byt 0).
     */
    private int scopeFieldCount;
    /**
     * Zoznam scope poli sablony
     */
    private List<FieldSpecifier> scopeFields;

    /**
     * Zoznam poli sablony
     */
//    private List<FieldSpecifier> fields;
    /** Vytvara novu instanciu triedy. */
    public IPFIXOptionsTemplateRecord() {
        super(); // vytvori linkedlist pre fieldy
        scopeFields = new LinkedList<>();
    }

    /**
     * Vytvara novu instanciu triedy.
     * @param scopeFieldCount Pocet scope poli sablony
     * @param fieldCount Pocet poli sablony
     */
    public IPFIXOptionsTemplateRecord(int fieldCount, int scopeFieldCount) {
        super(fieldCount); // nastavi field count a vytvori arraylist
        this.scopeFieldCount = scopeFieldCount;
        scopeFields = new ArrayList<>(scopeFieldCount);
    }

    /**
     * Vytvara novu instanciu triedy.
     * @param templateID Identifik�tor sablony
     * @param fieldCount Pocet poli sablony
     * @param scopeFieldCount Pocet scope poli sablony
     */
    public IPFIXOptionsTemplateRecord(int templateID, int fieldCount, int scopeFieldCount) {
        this(fieldCount, scopeFieldCount);
        this.templateID = templateID;
    }

    /**
     * Prida pole do sablony
     * @param field Pole sablony
     * @param scope Ak true, jedna sa o scope pole, opacne normalne pole
     */
    @Override
    public void addField(FieldSpecifier field) {
        if (field.isScope()) {
            fields.add(field);
            scopeFields.add(field);
        } else {
            fields.add(field);
        }
    }

    /**
     * Vrati referenciu na pole zo sablony podla indexu v zozname
     * @param index Index pola
     * @return Pole sablony
     */
    public FieldSpecifier getScopeField(int index) {
        return scopeFields.get(index);
    }

    /**
     * Vrati zoznam vsetkych poli v tejto sablone
     * @return Zoznam poli
     */
    public List<FieldSpecifier> getScopeFields() {
        return scopeFields;
    }

    /**
     * @return the scopeFieldCount
     */
    public int getScopeFieldCount() {
        return scopeFieldCount;
    }

    /**
     * @param scopeFieldCount the scopeFieldCount to set
     */
    public void setScopeFieldCount(int scopeFieldCount) {
        this.scopeFieldCount = scopeFieldCount;
    }

    @Override
    public String toString() {
        return String.format("Template[id:%s, fieldCount:%d, scopeFieldCound:%d, received: %s]",
                templateID, fieldCount, scopeFieldCount, DateFormat.getInstance().format(new Date(lastReceived)));
    }

    @Override
    public int getTemplateLength() {
        if (templateLength != 0) {
            return templateLength;
        }

        int length = 4; // hlavicka ma 4 bajty TemplateID + fieldCount
        if (scopeFieldCount != 0) { // ak to neni template withdrawal message tak ma aj scope field count
            length += 2;
        }
        for (FieldSpecifier fs : fields) {
            length += 4; // IE ID + length

            if (fs.isEnterpriseBit()) {
                length += 4; // Enterprise number
            }
        }
        return length;
    }
}
