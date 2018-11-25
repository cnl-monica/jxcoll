/*  Copyright (C) 2013 MONICA Research Group / TUKE 
* 2010 Michal Kascak, Matúš Husovský
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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.IPFIX.FieldSpecifier;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.Support;


/**
 * Stara sa o spracovavanie zaznamov o tokoch poslanych protokolom IPFIX. Generuje uctovacie
 * zaznamy a agreguje datovy toky.
 * @author Michal Kascak
 */
public class AccountingManager {
    /** Slúži na vypisovanie logovacích správ do konzoly **/
    private static Logger log = Logger.getLogger(AccountingManager.class.getName());
    
    /** Cache učtovacích zaznamov*/
    private AccountingRecordsCache accRecordCache;
    /** Exporter učtovacích zaznamov*/
    private AccountingRecordsExporter accExporter;
    /** Element šablóny*/
    private FieldSpecifier fieldSpecifier;
    /** Účtovaci záznam*/
    private AccountingRecord accRecord;
    /** Zdrojová IP adresa toku*/
    private byte[] sourceIPv4Address;
    /** Cieľová IP adresa toku*/
    private byte[] destinationIPv4Address;
    /** Zdrojová MAC adresa toku*/
    private byte[] sourceMAC;
    /** Cieľová MAC adresa toku*/
    private byte[] destinationMAC;
    /** Pole bajtov identifikátora aplikácie toku*/
    private byte[] applicationIdArray;
    /** Pomocné pole identifikátora aplikácie toku*/
    private byte[] tempIdArray;
    /** Meno aplikácie toku*/
    private String applicationName;
    /** Identifikátor aplikácie toku*/
    private int applicationId;
    /** Transportný protokol toku*/
    private short protocolIdentifier;
    /** Zdrojový port toku*/
    private int sourcePort;
    /** Cieľový port toku*/
    private int destinationPort;
    /** Hodnota DSCP toku*/
    private short ipDiffServCodePoint;
    /** Hodnota multicast toku*/
    private boolean isMulticast;
    /** Počet oktetov toku*/
    private long octetDeltaCount;
    /** Počet paketov toku*/
    private long packetDeltaCount;
    /** Časová pečiatka začiatku toku*/
    private long firstFlowStartMilliseconds;
    /** Časová pečiatka ukončenia toku*/
    private long lastFlowEndMiliseconds;
    /** Pomocná premenná pre zdrojovú IP toku*/    
    private InetAddress sourceIPv4AddressInet;
    /** Pomocná premenná pre cieľovú IP toku*/ 
    private InetAddress destinationIPv4AddressInet;  
    
    /** Zoznam poli v spracovavanej sablone*/
    private List<FieldSpecifier> fields;
    /** Vytvara novu instanciu triedy */
    public AccountingManager() {
    	if(Config.doPGAccExport){
        accRecordCache = new AccountingRecordsCache();
        accExporter = new AccountingRecordsExporter(accRecordCache);
        }
    }
    
    //TODO: zapracovat aj optionsTemplate
    
    /** 
     * Spracovava zaznam o datovom toku z IPFIX sablony a datoveho zaznamu.
     * Hodnoty z datoveho zaznamu su podla sablony spracovane a z nich sa vytvara
     * alebo agreguje uctovaci zaznam.
     * @param template sablona
     * @param data Data
     */
    public void processFlow(IPFIXTemplateRecord template, IPFIXDataRecord data) throws UnsupportedEncodingException{
        byte[] portBytes;
        int hashKey = 0;
        
        
            log.info("Processing flow for accounting");
            
            fields = template.getFields();
            
            //TODO osetrit ak getFIeldSpecifier vracia -1 (FieldSpecifier v danej sablone s danym elementID neexistuje)
            // Tu sa vyuziva skutocnost ze DataRecord vie o velkostiach jednotlivych poli
            // ak by nevedel, bolo by potrebne ziskat z fieldSpecifier velkost elementu
            short elementID = 8;
            sourceIPv4Address = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            try {
                sourceIPv4AddressInet = InetAddress.getByAddress(sourceIPv4Address);
            } catch (UnknownHostException ex) {
                java.util.logging.Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            elementID = 12;
            destinationIPv4Address = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            try {
                destinationIPv4AddressInet = InetAddress.getByAddress(destinationIPv4Address);
            } catch (UnknownHostException ex) {
                java.util.logging.Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            elementID = 56;
            sourceMAC = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            elementID = 80;
            destinationMAC = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            elementID = 4;
            protocolIdentifier = data.getFieldValue(template.getFieldSpecifierPosition(elementID))[0];

//            if (protocolIdentifier == 6) //TCP
//                elementID = 182;
//            else if(protocolIdentifier == 17) //UDP
//                elementID = 180;
   //         else if(protocolIdentifier == 1) //ICMP
   //           
            
            
            //Deprecated
            //portBytes = new byte[2]; 
            //portBytes = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            //sourcePort = (portBytes[0] << 8) + portBytes[1];
            elementID = 7;
            sourcePort = Support.unsignShort(ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getShort());
            
//            if (protocolIdentifier == 6) //TCP
//                elementID = 183;
//            else if(protocolIdentifier == 17) //UDP
//                elementID = 181;
            
            //portBytes = new byte[2];
            //portBytes = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            //destinationPort = (portBytes[0] << 8) + portBytes[1];
            elementID = 11;
            destinationPort = Support.unsignShort(ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getShort());
            
            elementID = 195;
            ipDiffServCodePoint = data.getFieldValue(template.getFieldSpecifierPosition(elementID))[0];
            elementID = 206;
            isMulticast = (data.getFieldValue(template.getFieldSpecifierPosition(elementID))[0] != 0);
            elementID = 95;

            tempIdArray = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            applicationIdArray = new byte[4];
            applicationIdArray[0] = 0;
            applicationIdArray[1] = tempIdArray[3];
            applicationIdArray[2] = tempIdArray[2];
            applicationIdArray[3] = tempIdArray[1];
            applicationId = byteArrayToInt(applicationIdArray);
            elementID = 96;
            applicationName = new String(data.getFieldValue(template.getFieldSpecifierPosition(elementID)), Charset.forName("UTF-8")).trim();
            //hashKey = generateHashKey(sourceIPv4Address, destinationIPv4Address, protocolIdentifier, sourcePort, destinationPort, ipDiffServCodePoint, isMulticast);
            //temporary
            hashKey = generateHashKey(sourceIPv4AddressInet, destinationIPv4AddressInet,sourceMAC,destinationMAC, (short)0, sourcePort, destinationPort, (short)0, false, applicationId);
            /*System.out.println(
                    "Hash key: " + hashKey + " for" + sourceIPv4AddressInet.toString() +
                            ' ' + destinationIPv4AddressInet.toString() + ' ' + sourcePort + ' ' + destinationPort 
                            );
           */
            elementID = 1;
            octetDeltaCount = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            //elementID = 85;
            //octetTotalCount = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            elementID = 2;
            packetDeltaCount = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            //packetTotalCount = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            
            elementID = 152;

            //flowStartMiliseconds = data.getFieldValue(template.getFieldSpecifierPosition(elementID));
            
            //longFlowStartMiliseconds = Long.parseLong(flowStartMiliseconds.toString());
             
            //timestampFlowStartMiliseconds.setTime(longFlowStartMiliseconds);
            //casvdoubli = Double.valueOf(flowStartMiliseconds.toString()).doubleValue();
            //log.debug("flowStartMiliseconds : " + flowStartMiliseconds.toString() + "  cas v double: " +casvdoubli + "pocet znakov v stringu:" +  flowStartMiliseconds.length);
            
            //log.debug("flowStartMicroseconds" + firstFlowStart.toString());
            //log.debug("flowStartMicroseconds" + firstFlowStart.toString());
            //log.debug("flowStartMicroseconds" + firstFlowStart.toString());
            //log.debug("flowStartMicroseconds" + firstFlowStart.toString());
                
            
               
            
            firstFlowStartMilliseconds = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            
            elementID = 153;
            
            lastFlowEndMiliseconds = ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elementID))).getLong();
            
            //timestampFlowStartMiliseconds= new Timestamp(l);
            
            //log.debug("flowStartMiliseconds" + l + "Timestamp" + timestampFlowStartMiliseconds.toString());
            
            log.debug("src IP address: " + sourceIPv4AddressInet);
            log.debug("dst IP address: " + destinationIPv4AddressInet);
            log.debug("src port:"+sourcePort);
            log.debug("dst port"+destinationPort);
            log.debug("octet delta count: " + octetDeltaCount);
            log.debug("packet delta count: " + packetDeltaCount);
            log.debug("application name: " + applicationName);
            
            if(accRecordCache.containsKey(hashKey)){
                //accRecordCache.aggregateFlow(hashKey, new Date(flowStartSeconds), octetTotalCount, packetTotalCount);
                log.debug("Adding flow record to existing accounting record with hashkey: " + hashKey);
                accRecordCache.aggregateFlow(hashKey, lastFlowEndMiliseconds, octetDeltaCount, packetDeltaCount);
            }
            else{
                //accRecord = new AccountingRecord(sourceIPv4Address, destinationIPv4Address, protocolIdentifier,
                //        sourcePort, destinationPort, ipDiffServCodePoint, new Date(flowStartSeconds), isMulticast,
                //        octetTotalCount, packetTotalCount);
                log.debug("Creating new accounting record with hashKey: " + hashKey);
                //accRecord = new AccountingRecord(sourceIPv4Address, destinationIPv4Address, (short)0,
                 //         sourcePort, destinationPort, (short)0, Calendar.getInstance().getTime(), false,
                 //         octetTotalCount, packetTotalCount);
                 accRecord = new AccountingRecord(sourceIPv4Address, destinationIPv4Address, sourceMAC, destinationMAC, protocolIdentifier,
                 sourcePort, destinationPort, ipDiffServCodePoint, firstFlowStartMilliseconds,lastFlowEndMiliseconds, isMulticast,
                 octetDeltaCount, packetDeltaCount, applicationId, applicationName);
                
                
                accRecordCache.addAccountingRecord(hashKey, accRecord);
            }
        
        
    }
    
    /**
     * Generuje hashovaci kluc uctovacieho zaznamu z jeho atributov na rozlzenie zaznamov
     * v hashovacej tabulke. Je unikatny pre kazdy zaznam.
     * @param srcIP Atribut zaznamu
     * @param dstIP Atribut zaznamu
     * @param protocol Atribut zaznamu
     * @param srcPort Atribut zaznamu
     * @param dstPort Atribut zaznamu
     * @param dscp Atribut zaznamu
     * @param multicast Atribut zaznamu
     * @return Kluc zaznamu
     */
    private int generateHashKey(InetAddress srcIP, InetAddress dstIP,byte[] srcMAC,byte[] dstMAC, short protocol, int srcPort, int dstPort, short dscp, boolean multicast, int applicationId){
        // Vhodny hash? 
        
        int hash = (srcIP.hashCode() + 1000009);
        hash ^=applicationId;
        hash ^= dstIP.hashCode() + 379;hash ^= Arrays.hashCode(srcMAC) + 12485;hash ^= Arrays.hashCode(dstMAC) + 7541699;
        hash ^= protocol; hash ^= srcPort + 2015; hash ^= dstPort; hash ^= dscp;  hash += (multicast ? 1009 : 2003);
        return hash;
    }
    /*
    public static int byteArrayToInt(byte[] b) {
        //ten dalsi by mal byt rychlejsi
        //return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return ByteBuffer.wrap(b).getInt();
    }
    */
    /**
     * Konverzia 4bajtového poľa do celočíselnej hodnoty. Špecifická konverzia
     * pre hodnotu identifikátora aplikácie.
     * @param encodedValue
     * @return 
     */
    public static int byteArrayToInt(byte[] encodedValue) {
    int index = 0;
    int value = encodedValue[index++] << Byte.SIZE * 3;
    value ^= (encodedValue[index++] & 0xFF) << Byte.SIZE * 2;
    value ^= (encodedValue[index++] & 0xFF) << Byte.SIZE * 1;
    value ^= (encodedValue[index++] & 0xFF);
    return value;
}

}
