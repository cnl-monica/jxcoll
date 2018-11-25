/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author esperian
 */
public class NF5TemplateRecord {
    private final int templateLength=20;
    private List<FieldObject> fields;

    public NF5TemplateRecord(){
        fields = new ArrayList<>(templateLength);
    }
    
    public void addField(int type,int length){
        fields.add(new FieldObject(type, length));
    }
    
    /**
     * @return the Fields
     */
    public List<FieldObject> getFields() {
        return fields;
    }

    /**
     * @param Fields the Fields to set
     */
    public void setFields(List<FieldObject> Fields) {
        this.fields = Fields;
    }

    /**
     * @return the templateLength
     */
    public int getTemplateLength() {
        return templateLength;
    }
}
