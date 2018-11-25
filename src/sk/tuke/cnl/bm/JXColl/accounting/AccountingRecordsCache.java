/*  Copyright (C) 2013 MONICA Research Group / TUKE 
*  2010 Michal Kascak, Matúš Husovský
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

package sk.tuke.cnl.bm.JXColl.accounting;

import java.util.Date;
import java.util.Hashtable;

/**
 * Trieda reprezentuje cache uctovacich zaznamov.
 * @author Michal Kascak
 */
public class AccountingRecordsCache {
    
    /** Cache uctovacich zaznamov realizovany ako hash tabulka */
    private Hashtable<Integer,AccountingRecord> arCache;
    /** Vytvara novu instanciu triedy */
    public AccountingRecordsCache() {
        arCache = new Hashtable<Integer, AccountingRecord>();
    }
    
    /**
     * Prida uctovaci zaznam do cache, ak uz taky neobsahuje
     * @param key Kluc zaznamu
     * @param ar uctovaci zaznam
     */
    public void addAccountingRecord(int key, AccountingRecord ar){
        //check na ar ci nie je null
        if(!arCache.containsKey(key))
            arCache.put(new Integer(key), ar);
    }
    
    /**
     * Zisti, ci sa zaznam s danum klucom nachadza v cache
     * @param key Kluc uctovacieho zaznamu
     * @return true, ak sa zaznam s danym klucom v cache nachadza, false naopak
     */
    public boolean containsKey(int key){
        return arCache.containsKey(new Integer(key));
    }
    
    /**
     * Vrati uctovaci zaznam z cache, ak sa v nej nachadza
     * @param key Kluc uctovacieho zaznamu
     * @return uctovaci zaznam, null ak sa zaznam v cache nenachadza
     */
    public AccountingRecord getAccountingRecord(int key){
        if(arCache.containsKey(key))
            return (AccountingRecord)arCache.get(key);
        return null;
    }
    
    /**
     * Agreguje flow do existujuceho zaznamu v cache. Flow musi mat rovnaky charakteristiky
     * ako flow z ktoreho bol uctovaci zaznam vytvoreny.
     * @param key Kluc uctovacieho zaznamu, do ktoreho sa flow agreguje
     * @param flowTime cas vzniku flowu
     * @param octetCount Pocet bytov vo flowe
     * @param packetCount Pocet paketov vo flowe
    */
    public void aggregateFlow(int key, long flowTime, long octetCount, long packetCount){
        getAccountingRecord(key).addFlow(flowTime, octetCount, packetCount);
    }
    
    /**
     * Vyprazdni cache
    */
    public void clear(){
        arCache.clear();
    }
    
    /**
     * Vrati celu hash tabulku cache
     * @return Hash tabulka cache
    */
    public Hashtable<Integer, AccountingRecord> getArCache() {
        return arCache;
    }
}