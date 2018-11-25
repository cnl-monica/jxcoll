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

import java.net.InetAddress;
import java.util.Date;

/**
 * Trieda reprezentuje uctovaci zaznam.
 * @author Michal Kascak
 */
public class AccountingRecord {
    /** Zdrojová IP adresa účtovacieho záznamu */
    private byte[] sourceIPv4Address;
    /** Cieľová IP adresa účtovacieho záznamu */
    private byte[] destinationIPv4Address;
    /** Zdrojová MAC adresa účtovacieho záznamu*/
    private byte[] sourceMAC;
    /** Cieľová MAC adresa účtovacieho záznamu */
    private byte[] destinationMAC;
    /** Transportný protokol účtovacieho záznamu */
    private short protocolIdentifier;
    /** Zdrojový port účtovacieho záznamu */ 
    private int sourcePort;             // Ci sa jedna o UDP alebo TCP port je uvedene v protocolIdentifier
    /** Cieľový port účtovacieho záznamu */ 
    private int destinationPort;
    /** Hodnota DSCP účtovacieho záznamu */ 
    private short ipDiffServCodePoint;
    /** Hodnota štartu(časovej známky) prvého toku záznamu */
    private long firstFlowStart;
    /** Hodnota ukončenia(časovej známky) posledného toku záznamu */
    private long lastFlowEnd;
    /** Hodnota multicastu účtovacieho záznamu*/
    private boolean isMulticast;
    /** Hodnota počtu bajtov účtovacieho záznamu*/
    private long octetDeltaCount;       // Max velkost prenesenych dat je 4,3GB pre jeden uctovaci zaznam
   /** Hodnota počtu paketov účtovacieho záznamu*/
    private long packetDeltaCount;      // Tato hodnota ani nie je potrebna pre uctovanie
    /** Hodnota počtu tokov účtovacieho záznamu*/
    private int flowCount = 0;
    /** Identifikátor aplikácie účtovacieho záznamu*/
    private int applicationId;
    /** Meno aplikácie účtovacieho záznamu*/
    private String applicationName;
    /** Vytvara novu instanciu triedy účtovacieho záznamu*/
    public AccountingRecord() {
    }
    
    /**
     * Vytvara novu instanciu triedy z nastavenych parametrov uctovacieho zaznamu
     * @param srcIP Zdrojova IP adresa
     * @param dstIP Cielova IP adresa
     * @param srcMAC Zdrojová MAC adresa
     * @param dstMAC Cieľová MAC adresa
     * @param protocol Transportny protokol
     * @param srcPort Zdrojovy port
     * @param dstPort Cielovy port
     * @param ipdscp IP Differentiated Services Code Point
     * @param flowTime cas exportu datoveho toku
     * @param isMulticast Flag ci sa jedna o multivastove spojenie
     * @param octetCount Pocet bytov flowu
     * @param packetCount Pocet paketov flowu
     * @param applicationId Id aplikacneho protokolu pre flow
     * @param applicationName Meno aplikacneho protokolu pre flow
     */
    public AccountingRecord(byte[] srcIP, byte[] dstIP,byte[] srcMAC, byte[] dstMAC, short protocol, int srcPort, int dstPort, 
            short ipdscp, long firstFlowStart,long lastFlowEnd, boolean isMulticast, long octetCount, long packetCount, int applicationId,
            String applicationName){
        this.sourceIPv4Address = srcIP;
        this.destinationIPv4Address = dstIP;
        this.sourceMAC = srcMAC;
        this.destinationMAC = dstMAC;
        this.protocolIdentifier = protocol;
        this.sourcePort = srcPort;
        this.destinationPort = dstPort;
        this.ipDiffServCodePoint = ipdscp;
        this.firstFlowStart = firstFlowStart;
        this.lastFlowEnd = lastFlowEnd;
        this.isMulticast = isMulticast;
        this.octetDeltaCount = octetCount;
        this.packetDeltaCount = packetCount;
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        flowCount = 1;
    }
    
    /**
     * Prida flow do uctovacieho zaznamu. Flow musi mat rovnake charakteristiky ako
     * flow, z ktoreho bol zaznam vytvoreny. K existujucemu zaznamu sa pripocita pocet bytov
     * a pocet paketov flowu.
     * @param flowTime cas exportu flowu
     * @param octetCount Pocet bytov flowu
     * @param packetCount Pocet paketov flowu
     */
    public void addFlow(long lastFlowEnd, long octetCount, long packetCount){
        this.lastFlowEnd = lastFlowEnd;
        this.octetDeltaCount += octetCount;
        this.packetDeltaCount += packetCount;
        flowCount++;
    }
    
    /**
     * Porovna tento objekt s objektom v parametri. Zhodovat sa musia ip adresy,
     * protokol, porty, dscp a multicast, MAC adresy
     * @param obj Objekt na porovnanie
     * @return true, ak su zaznamy rovnake, false inak
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof AccountingRecord)) return false;
        AccountingRecord ar = (AccountingRecord) obj;
        
        if(this.sourceIPv4Address.toString().equals(ar.getSourceIPv4Address().toString()) 
            && this.destinationIPv4Address.toString().equals(ar.destinationIPv4Address.toString())
            && this.sourceMAC.toString().equals(ar.sourceMAC.toString())
            && this.destinationMAC.toString().equals(ar.destinationMAC.toString())
            && this.protocolIdentifier == ar.getProtocolIdentifier()
            && this.sourcePort == ar.getSourcePort()
            && this.destinationPort == ar.getDestinationPort()
            && this.ipDiffServCodePoint == ar.getIpDiffServCodePoint()
            && this.isMulticast == ar.isIsMulticast()
            && this.applicationId == ar.getApplicationId())
            return true;
        return false;
    }

    /**
     * Vrati hash code tohto objektu
     * @return Hash code
     */
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + sourceIPv4Address.hashCode();
        hash = hash * 31 + destinationIPv4Address.hashCode();
        return hash;
    }
    
    /**
     * Get metoda
     * @return Zdrojova adresa uctovacieho zaznamu
     */
    public byte[] getSourceIPv4Address() {
        return sourceIPv4Address;
    }
    /**
     * Get metoda
     * @return Zdrojova MAC adresa uctovacieho zaznamu
     */
    public byte[] getSourceMACAddress() {
        return sourceMAC;
    }
    /**
     * Get metoda
     * @return Zdrojova MAC adresa uctovacieho zaznamu
     */
    public byte[] getDestinationMACAddress() {
        return destinationMAC;
    }
    /**
     * Get metoda
     * @return Cielova adresa uctovacieho zaznamu
     */
    public byte[] getDestinationIPv4Address() {
        return destinationIPv4Address;
    }
    /**
     * Get metoda
     * @return Trasportny protokol uctovacieho zaznamu. 6 - TCP, 17 - UDP
     */
    public short getProtocolIdentifier() {
        return protocolIdentifier;
    }
    /**
     * Get metoda
     * @return Zdrojovy port uctovacieho zaznamu
     */
    public int getSourcePort() {
        return sourcePort;
    }
    /**
     * Get metoda
     * @return Cielovy port uctovacieho zaznamu
     */
    public int getDestinationPort() {
        return destinationPort;
    }
    /**
     * Get metoda
     * @return IP Differentiated Services Code Point
     */
    public short getIpDiffServCodePoint() {
        return ipDiffServCodePoint;
    }
    /**
     * Get metoda
     * @return cas exportu prveho flowu v uctovacom zazname
     */
    public long getFirstFlowStart() {
        return firstFlowStart;
    }
    /**
     * Get metoda
     * @return cas kedy posledny exportovany tok bol ukoneceny
     */
    public long getLastFlowEnd() {
        return lastFlowEnd;
    }
    /**
     * Get metoda
     * @return True, ak sa jedna o multicastove spojenie, false opacne
     */
    public boolean isIsMulticast() {
        return isMulticast;
    }
    /**
     * Get metoda
     * @return identifikátor aplikácie účtovacieho záznamu
     */
    public int getApplicationId(){
        return applicationId;
    }
    /**
     * Get metoda
     * @return meno aplikácie účtovacieho záznamu
     */
    public String getApplicationName(){
        return applicationName;
    }    
    
    
    /**
     * Get metoda
     * @return Pocet bytov uctovacieho zaznamu
     */
    public long getOctetDeltaCount() {
        return octetDeltaCount;
    }
    /**
     * Get metoda
     * @return Pocet paketov uctovacieho zaznamu
     */
    public long getPacketDeltaCount() {
        return packetDeltaCount;
    }
    /**
     * Get metoda
     * @return Pocet datovych dokov v uctovacom zazname
     */
    public int getFlowCount() {
        return flowCount;
    }
    
}