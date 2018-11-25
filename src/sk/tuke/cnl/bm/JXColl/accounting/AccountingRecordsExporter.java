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

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.export.MongoClient;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * Trieda exportuje uctovacie zaznamy z cache do databazy v pravidelnych intervaloch.
 * @author Michal Kascak
 */
public class AccountingRecordsExporter {
    private static Logger log = Logger.getLogger(AccountingRecordsExporter.class.getName());
    
    /** Casovac pre export zaznamov */
    private Timer timer;
    /** Objekt pre databazovu konektivitu */
    private MongoClient pgclient;
    /** Cache uctovacich zaznamov */
    private AccountingRecordsCache arCache;
    //private Hashtable<Integer, AccountingRecord> cache; 
    /**Začiatok prvého toku účtovacieho záznamu.*/
    private Date dateFirstFlowStartMilliseconds;
    /**Koniec posledného toku účtovacieho záznamu.*/
    private Date dateLastFlowEndMilliseconds;
    /**Hodnota stredného času účtovacieho záznamu*/
    private Date dateAverage;

    
    private static final int AR_EXPORT_INTERVAL = Config.accRecExportInterval*1000;
    
    /** Vytvara novu instanciu triedy, resetuje casovac */
    public AccountingRecordsExporter() {
        timer = new Timer();
        timer.schedule(new DoExportTimerTask(), AR_EXPORT_INTERVAL);
        pgclient = new MongoClient();
        pgclient.disconnect();  //Nepotrebujem mat hned otvorene spojenie
    }
    
    /** Vytvara novu instanciu triedy, resetuje casovac a predava referenciu na cache uctovacich zaznamov
     * @param cacheReference referencia na cache uctovacich zaznamov
     */
    public AccountingRecordsExporter(AccountingRecordsCache cacheReference) {
        timer = new Timer();
        arCache = cacheReference;
        
        timer.schedule(new DoExportTimerTask(), AR_EXPORT_INTERVAL);
        
        log.debug("Creating PGClient for Accounting.");
        
        pgclient = new MongoClient();
        log.debug("Disconnecting from DB for accounting.");
        
        pgclient.disconnect();
    }
    
    /**
     *  Exportuje vsetky uctovacie zaznamy v cache do dotabazy, cache sa vycisti
     */
    public void flushCacheToDB(){
        AccountingRecord ar;
        String json = "{";
        Object[] values = new Object[15];
        long averageDate;
        Enumeration e = arCache.getArCache().elements();
        log.debug("Connecting to DB for Accounting.");
        pgclient.dbConnect();
        
        //colNames = new String[12];
        String[] colNames = {"collectorID","sourceipv4address","destinationipv4address","sourcemacaddress",
                "destinationmacaddress","protocolidentifier","sourceport","destinationport","ipdiffservcodepoint",
                "datetime","ismulticast","octetdeltacount","packetdeltacount","flowcount","applicationName"};
        
        while(e.hasMoreElements()){
            ar = (AccountingRecord)e.nextElement();

            
            values[0] = Config.collectorID ;   //TEMP collector_ID
            values[1] = ar.getSourceIPv4Address() ;              //MAREK>>> upravit IP zo String na byte[] ak bude treba, viď "IpfixDecoder.java"
            values[2] = ar.getDestinationIPv4Address();    //MAREK>>> upravit IP zo String na byte[] ak bude treba
            values[3] = ar.getSourceMACAddress();            
            values[4] = ar.getDestinationMACAddress();
            values[5] = ar.getProtocolIdentifier() ;
            values[6] = ar.getSourcePort() ;
            values[7] = ar.getDestinationPort() ;
            values[8] = ar.getIpDiffServCodePoint() ;            
            averageDate = (ar.getFirstFlowStart()+ar.getLastFlowEnd())/2;
            //dateFirstFlowStartMilliseconds = new Date(ar.getFirstFlowStart());
            //dateLastFlowEndMilliseconds = new Date(ar.getLastFlowEnd());
            //dateAverage = new Date(averageDate);
            
            //System.out.println("Zaciatok prveho toku je: " + dateFirstFlowStartMilliseconds.toString());
            //System.out.println("Priemerny cas toku je: " + dateAverage.toString());
            //System.out.println("Koniec posledneho toku je : " + dateLastFlowEndMilliseconds.toString());
            
            //averageDate = (ar.getFirstFlowStart().getTime() + ar.getLastFlowStart().getTime())/2;         
            //averageDate = ar.getLastFlowStart().getTime();
            //json += "'datetime' : '" + Support.SecToTimeOfDay(averageDate / 1000) + "' , ";
            values[9] = averageDate ;
            values[10] = (ar.isIsMulticast() ? true : false) ;
            values[11] = ar.getOctetDeltaCount();
            values[12] = ar.getPacketDeltaCount();
            values[13] = ar.getFlowCount() ;
            values[14] = ar.getApplicationName();
            //pgclient.insertdata("ACC_REC", json);
            //log.debug("Inserting values: " + json);
            
            //pgclient.insertdata("ACC_RECORD",json);
            
            pgclient.insertData("acc_records", colNames, values);
        }
        
        pgclient.disconnect();
        log.debug("Disconnecting from DB for Accounting.");
        arCache.clear();
        timer.schedule(new DoExportTimerTask(), AR_EXPORT_INTERVAL);
    }
    
    /** Uloha pre casovac, ktory spusta export do databazy */
    private class DoExportTimerTask extends TimerTask{
        /** Vola metodu flushCacheToDB */
        public void run(){
            flushCacheToDB();
        }
    }
}