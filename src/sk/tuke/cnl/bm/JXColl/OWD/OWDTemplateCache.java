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
 *  Súbor: OWDTemplateCache.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.net.InetAddress;
import java.util.Hashtable;

/**
 * Class for OWD Template Cache.
 *
 */
public class OWDTemplateCache {

    /**
     * Constructor for this class.
     */
    public OWDTemplateCache() {
    }

    private Hashtable<KeyObject,Hashtable<Integer,OWDTemplateRecord>> Exporters= new Hashtable<KeyObject,Hashtable<Integer,OWDTemplateRecord>>();
    private Hashtable<Integer,OWDTemplateRecord> Templates ;
    KeyObject key=new KeyObject();

    /**
     * Adds the template record into a cache on the basis of the sending exporter.
     *
     * @param templ OWDTemplateRecord template record.
     * @param ip InetAddress IP address of the exporter.
     * @param sourceID long ID of the exporter.
     */
    public void addTemplate(OWDTemplateRecord templ,InetAddress ip, long sourceID) {
        key.set(ip,sourceID);
        if (Exporters.containsKey(key) ) {
            Templates = (Hashtable<Integer,OWDTemplateRecord>) Exporters.get(key);
            if (Templates.containsKey(templ.templateID)) {
                //Templates.remove(templ.templateID);
                Templates.put(templ.templateID,templ);  // update obsahu templaty, ak uz existuje
                //System.out.println("update templaty");
                    } /**@todo ak uz danu template mame, update timestampu  , expiracia template na zaklade casu*/
            else Templates.put(templ.templateID,templ);
            }
            else {Templates = new Hashtable<Integer,OWDTemplateRecord>(); Exporters.put(key,Templates); Templates.put(templ.templateID,templ);  }
    }


    /**
     * Returns the number of templates in cache.
     * 
     * @return
     */
    public int getIPFIXTemplateCacheCount(){
    	return Templates.size();
    }

    /**
     * Returns all the templates for the exporter.
     *
     * @param ip InetAddress IP address of the exporter.
     * @param sourceID long ID of the exporter.
     * @return Hashtable list of templates in cache for the given exporter.
     */
    public Hashtable getTemplates(InetAddress ip, long sourceID) {
        key.set(ip,sourceID);
        Templates = (Hashtable<Integer,OWDTemplateRecord>) Exporters.get(key);
        return Templates;
    }

    /**
     * Checks for template with given ID for the given exporter in the cache.
     *
     * @param templID int template ID.
     * @param ip InetAddress IP address of the exporter.
     * @param sourceID long ID of the exporter.
     * @return true, if template is in the cache, false if not.
     */
    public boolean contains(int templID, InetAddress ip, long sourceID){
        key.set(ip,sourceID);
        if (Exporters.containsKey(key) ) {
            Templates = (Hashtable<Integer,OWDTemplateRecord>) Exporters.get(key);
            return Templates.containsKey(templID);
        }
        return false;
    }

    /**
     * Returns the templates at the basis of it's ID and exporter.
     *
     * @param templID int template ID.
     * @param ip InetAddress IP address of the exporter.
     * @param sourceID long ID of the exporter.
     * @return template record, null if there is no such a template in the cache.
     */
    public OWDTemplateRecord getByID(int templID, InetAddress ip, long sourceID){
        key.set(ip,sourceID);
        Templates =  (Hashtable<Integer,OWDTemplateRecord>) Exporters.get(key);
       return (OWDTemplateRecord) Templates.get (  templID );
    }

    /**
     * Key for getting the templatecache for exporter identified by it
     */
    // Cela vnutorna trieda trieda je skopirovana z triedy NF9TemplateCache
    //TODO: spravit z nej verejnu triedu?
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
