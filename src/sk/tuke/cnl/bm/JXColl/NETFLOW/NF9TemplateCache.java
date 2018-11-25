package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;

/**
 * <p>Title: JXColl</p>
 *
 * <p>Description: Java XML Collector for network protocols</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: TUKE, FEI, CNL</p>
 * NF9 Template cache
 * @author Lubos Kosco
 * @version 0.1
 */
public class NF9TemplateCache {

    private Hashtable<KeyObject,Hashtable<Integer,NF9Template>> Exporters= new Hashtable<KeyObject,Hashtable<Integer,NF9Template>>();

    private Hashtable<Integer,NF9Template> Templates ;
    KeyObject key=new KeyObject();
    
    /**
     * NF9TemplateCache empty constructor
     */
    public NF9TemplateCache() {
        
    }

    /**
     * add Template to the template cache for given exporter (identified by its
     * IP adress & source ID )
     *
     * @param templ NF9Template
     * @param ip InetAddress
     * @param sourceID long
     */
    public void addTemplate(NF9Template templ,InetAddress ip, long sourceID) {
        
        key.set(ip,sourceID);
        if (Exporters.containsKey(key) ) { 
            Templates = (Hashtable<Integer,NF9Template>) Exporters.get(key);
            
            if (Templates.containsKey(templ.templateID)) {
                
                
                Templates.put(templ.templateID,templ);  // update obsahu templaty, ak uz existuje
                
            } 
            else 
            {
                Templates.put(templ.templateID,templ); 
                
            }
        }
        else {
            Templates = new Hashtable<Integer,NF9Template>(); Exporters.put(key,Templates); Templates.put(templ.templateID,templ);  }
    }

    /**
     * get Templates for the given exporter
     *
     * @param ip InetAddress
     * @param sourceID long
     * @return Hashtable
     */
    public Hashtable getTemplates(InetAddress ip, long sourceID) {
        key.set(ip,sourceID);
        Templates = (Hashtable<Integer,NF9Template>) Exporters.get(key);
        return Templates;
    }

    /**
     * check if the cache for given exporter contains a template with the given id
     *
     * @param templID int
     * @param ip InetAddress
     * @param sourceID long
     * @return boolean
     */
    public boolean contains(int templID, InetAddress ip, long sourceID){
        key.set(ip,sourceID);
        if (Exporters.containsKey(key) ) {
            Templates = (Hashtable<Integer,NF9Template>) Exporters.get(key);
            return Templates.containsKey(templID);
        }
        return false;
    }
    
    public void removeTemplate(int templateID){
        if(Templates.containsKey(templateID)){
            Templates.remove(templateID);
        }
    }
    
    /**
     * get the template by its id number for exporter identified by the IP
     * address & its sourceID
     *
     * @param templID int
     * @param ip InetAddress
     * @param sourceID long
     * @return NF9Template
     */
    public NF9Template getByID(int templID, InetAddress ip, long sourceID){
        key.set(ip,sourceID);
        Templates =  (Hashtable<Integer,NF9Template>) Exporters.get(key);
       return (NF9Template) Templates.get (  templID );
    } 
    

    /**
     * <p>Title: JXColl</p>
     *
     * <p>Description: Java XML Collector for network protocols</p>
     *
     * <p>Copyright: Copyright (c) 2005</p>
     *
     * <p>Company: TUKE, FEI, CNL</p>
     * Key for getting the templatecache for exporter identified by it
     * @author Lubos Kosco
     * @version 0.1
     */
    private class KeyObject {

            // pre templaty takyto cache podla rfccka:
            // <Exporter, Export Interface, Template ID, Template ID, Template Def, Last Received>  ?? SourceID ???
	// podla IPFIXu: <Exporting Process, Observation Domain Source ID, Template ID, Template Definition, Last Received>
            //    treba doplnit export interface ;   zistit co znamena druhe template id  ...

        // WARN: iba na zaklade IPcky a source ID sa robi kluc !!!
        InetAddress ip;
        long sourceID;
        KeyObject(){
        }

        /**
         * KeyObject constructor, explicit fill of member variables
         *
         * @param ip InetAddress IP of exporter
         * @param sourceID long source ID of exporter (unique for every
         */
        KeyObject(InetAddress ip, long sourceID) {
            this.ip=ip; this.sourceID=sourceID;
        }

        /**
         * sets the member variables of this key
         *
         * @param ip InetAddress IP of exporter
         * @param sourceID long source ID of exporter (unique for every
         *   exporter)
         */
        public void set(InetAddress ip, long sourceID) {
            this.ip=ip; this.sourceID=sourceID;
        }

        /**
         * overriden equals method, needed for key comparing
         *
         * @param obj Key Object which is compared to this object
         * @return whether the objects are equal (true == yes)
         */
        public boolean equals(Object obj) {
            KeyObject cmp = (KeyObject) obj;
            if ( (sourceID==cmp.sourceID) && ip.equals(cmp.ip) )  return true;
            else return false;
        }

        /**
         * overriden hashCode method, provides own hashcode sum
         *
         * @return int containing hash sum of all members
         */
        public int hashCode() {
        int result = 0;
            result +=ip.hashCode();
            result +=sourceID;
        return result;
        }
        

    }

}
