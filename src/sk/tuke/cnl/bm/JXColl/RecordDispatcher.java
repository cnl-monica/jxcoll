/*  Copyright (C) 2013 MONICA Research Group / TUKE 
 * 2010 Lubos Kosco, Adrian Pekar, Tomas Verescak, Pavol Beňko
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
package sk.tuke.cnl.bm.JXColl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import javax.activation.UnsupportedDataTypeException;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.DataException;
import sk.tuke.cnl.bm.JXColl.IPFIX.FieldSpecifier;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF5FlowRecord;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF5Message;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF9Template;
import sk.tuke.cnl.bm.JXColl.accounting.AccountingManager;
import sk.tuke.cnl.bm.JXColl.export.ACPServer;
import sk.tuke.cnl.bm.JXColl.export.MongoClient;
import sk.tuke.cnl.bm.Templates;

/**
 * Class responsibility is to distribute received and parsed data structured
 * in conformance with IPFIX or NetFlow messages into particular modules of JXColl.
 * @author Michal Kascak, Adrian Pekar, Tomas Verescak
 */
public class RecordDispatcher {

    private static Logger log = Logger.getLogger(RecordDispatcher.class.getName());
    private List<IEHelper> ieInfo;
//    private Hashtable<String, List> ieList;
    private Enumeration groupEnum;
    /** objekt pre pristup k databaze */
    private MongoClient mongoClient;
    /** Modul uctovania */
    private AccountingManager am;
    /** ACP modul */
    protected static ACPServer acpserver = null;
    /** Objekt obsahujuci informacie o informacnych elementoch */
    private IpfixElements elementsInfo;
    private static RecordDispatcher instance;
    /** Hash tabuľka obsahujúca ID IE a  hodnotu IE*/
    private Hashtable ACPdata = new Hashtable();
    static {
        instance = new RecordDispatcher();
    }

    public static RecordDispatcher getInstance() {
        if (instance == null) {
            return new RecordDispatcher();
        }
        return instance;
    }

    /** Vytvara novu instanciu triedy, singleton */
    private RecordDispatcher() {
//        ieList = new Hashtable<String, List>();
        if (Config.doACPTransfer) {
            try {
                acpserver = new ACPServer(Config.acpport);
                acpserver.start();
                JXColl.acpserver = acpserver;
            } catch (IOException e) {
                log.error("ACP server could not start because of an " + e.getClass() + " : " + e);
                JXColl.stopJXColl();
            }
        }

        if (Config.doPGexport) {
            mongoClient = new MongoClient();
        }
        // informacie o IPFIX informacnych elementoch
        elementsInfo = IpfixElements.getInstance();

        if (Config.doPGAccExport) {
            am = new AccountingManager();
        }
    }
   
        
    public void dbExportNetflow5(NF5Message message) {
         String[] colNamesHeader = new String[message.getClass().getDeclaredFields().length-2];
         Object[] valuesHeader = new Object[message.getClass().getDeclaredFields().length-2];
         int i=0;
                        
         for(Field f:message.getClass().getDeclaredFields()){
                 if(!f.getName().equals("flow") && !f.getName().equals("receiveTime")){
                     try {
                         colNamesHeader[i]=f.getName();
                         f.setAccessible(true);
                         
                         if(f.getName().equals("unixsec")){
                         Object value = Support.SecToTimeOfDay(f.getLong(message));
                         valuesHeader[i]=value;
                         }else{                         
                         Object value = f.get(message);
                         valuesHeader[i]=value;
                         }
                          i++;
                         
                     } catch (IllegalArgumentException ex) {
                         java.util.logging.Logger.getLogger(RecordDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IllegalAccessException ex) {
                         java.util.logging.Logger.getLogger(RecordDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                
             
         }
           
         mongoClient.insertData("records_NF5Header", colNamesHeader, valuesHeader);
         
         i=0;
         String[] colNamesFlow;
         Object[] valuesFlow;
        
       for(NF5FlowRecord flow : message.getFlow()){
           colNamesFlow = new String[18];
           valuesFlow = new Object[18];
             for(Field f:flow.getClass().getDeclaredFields()){
                 try {
                     if(!f.getName().equals("NF5tem")&& !f.getName().equals("pad1") && !f.getName().equals("pad2")){
                     colNamesFlow[i]=f.getName();
                     f.setAccessible(true);
                     
                     if(f.getName().equals("srcaddr") || f.getName().equals("dstaddr") || f.getName().equals("nexthop")){
                        Object value = Support.intToIp(f.getInt(flow));
                        valuesFlow[i]=value;
                     }else{
                     Object value = f.get(flow);
                     valuesFlow[i]=value;}
                     i++;}
                 } catch (IllegalArgumentException ex) {
                     java.util.logging.Logger.getLogger(RecordDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IllegalAccessException ex) {
                     java.util.logging.Logger.getLogger(RecordDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             mongoClient.insertData("records_NF5Flow", colNamesFlow, valuesFlow);
             i=0;
         }
    }
    
    public void dbExportNetflow9(NF9Template template, Object[] data, String s) {
        
        if(s.equals("DR")){
            String[] colNamesFlow = new String[(template.fieldCount)];
            Object[] dataFlow = new Object[data.length];
            
            for(int i=0; i<template.fieldCount;i++){
                colNamesFlow[i]=Templates.getFiledName(template.getField(i).getType());
                
            }
            
            for(int i=0; i<template.fieldCount;i++){
                
                if(template.getField(i).getType()==Templates.IPV4_SRC_ADDR || template.getField(i).getType()==Templates.IPV4_DST_ADDR ||
                   template.getField(i).getType()==Templates.IPV4_NEXT_HOP){
                    dataFlow[i] = Support.intToIp((int)data[i]);
                   
                }else{
                    dataFlow[i] = data[i];
                    
                }
            }
            
            System.out.println(colNamesFlow.length+ " "+dataFlow.length);
            
            mongoClient.insertData("records_NF9Flown", colNamesFlow, dataFlow);
        }else{
            String[] colNamesFlow={"version","count","sysuptime","unixsec","psequence","sourceid","receiveTime"};
            
            mongoClient.insertData("records_NF9Header", colNamesFlow, data);
        }
    
    }
    
    
    /**
     * Posunie prijate data a im zodpovedajucu sablonu vsetkym aktivnym modulom
     * kolektora, ktore pracuju s IPFIX spravami
     *
     * @param template
     *            sablona data
     * @param data
     *            Datovy zaznam
     */
    public synchronized void dispatchIPFIXRecord(IPFIXTemplateRecord template,
            IPFIXDataRecord data, InetSocketAddress ipmb) throws UnsupportedEncodingException {

        // flowRecord (so sablonou) pre DB
        if (Config.doPGexport) {
            dbExport(template, data);
        }

        // flowRecord (so sablonou) pre ACP
        if (Config.doACPTransfer) {
            if(Config.doPGexport==false){
            ParseForACP(template, data);
            }
            acpserver.processIPFIXData((InetSocketAddress) ipmb, template, data);
        }

        // flowRecord (so sablonou) pre Accounting
        if (Config.doPGAccExport) {
            am.processFlow(template, data);
        }
    }

    /**
     * Metoda sluzi na export vsetkych nameranych dat poslanych protokolom IPFIX
     * do databazy a ulozenie do hashtable pre ACP. Pri prvom prechode funkciou sa 
     * generuje pamatovy zaznam o informacnych elementoch (ie) z XML suboru. Vytiahnu 
     * sa informacie o ie, ktore sa nachadzaju v sablone, dekoduju sa ich datove 
     * typy a prislusnost k skupine pre ulozenie hodnot do databazy.
     *
     * @param template
     *            sablona dat
     * @param dataRecord
     *            Datovy zaznam
     */
    private void dbExport(IPFIXTemplateRecord template, IPFIXDataRecord dataRecord) {
        String name, dataType, group;
        Object value;
        ACPdata.clear();
        log.debug("Processing data within data record!");

        //mapovanie Skupina -> zoznam ie pre tuto skupinu
        Hashtable<String, List> ieList = new Hashtable<>();

        // pre vsetky polozky sablony
        for (FieldSpecifier field : template.getFields()) {
            long enterpriseNumber = (field.isEnterpriseBit()) ? field.getEnterpriseNumber() : 0; // asi mozno vynechat
            //log.debug("Enterprise: " + enterpriseNumber);

            if (!elementsInfo.exists(field.getElementID(), enterpriseNumber)) { // by Tomas Verescak
                String enterprise = (enterpriseNumber != 0) ? String.format("[%d]", enterpriseNumber) : "";
                log.warn("Element with ID: " + field.getElementID() + enterprise + " is not supported, skipped! Update XML file!");
                continue;
            }

//            // informacny element typu paddingOctets mozeme preskocit!
//            if (field.getElementID() == 210) {
//                continue;
//            }



            // zistime meno a typ aktualneho informacneho elementu
            name = elementsInfo.getElementName(field.getElementID(), enterpriseNumber); // by Tomas Verescak
            dataType = elementsInfo.getElementDataType(field.getElementID(), enterpriseNumber);

            // hodnoty pre main tabulku, tieto hodnoty maju povodne inu tabulku ale my ich davame do main
            if (field.getElementID() == 1 || //octetDeltaCount
                    field.getElementID() == 2 || //packetDeltaCount
                    field.getElementID() == 4 || //protocolIdentifier
                    field.getElementID() == 7 || //sourceTransportPort
                    field.getElementID() == 8 || //sourceIPv4Address
                    field.getElementID() == 11 || //destinationTransportPort
                    field.getElementID() == 12 || //destinationIPv4Address
                    field.getElementID() == 85 || //octetTotalCount
                    field.getElementID() == 86 || //packetTotalCount
                    field.getElementID() == 136 || //flowEndReason
                    field.getElementID() == 138 || //observationPointId
                    field.getElementID() == 148 || //flowID
                    field.getElementID() == 152 || //flowStartMilliseconds
                    field.getElementID() == 153 || //flowEndMilliseconds
                    field.getElementID() == 156 || //flowStartNanoseconds       //added by Marek Marcin
                    field.getElementID() == 157    //flowEndNanoseconds         //added by Marek Marcin
                    ) {
                group = "main";
            } else {
                group = elementsInfo.getElementGroup(field.getElementID(), enterpriseNumber); // by Tomas Verescak
//                log.debug("grupa ziskana z ElementsInfo: " + group);
            }

            // ziskame si data konkretneho informacneho elementu
            int iePosition = template.getFieldSpecifierPosition(field.getElementID());
            ByteBuffer elementData = ByteBuffer.wrap(dataRecord.getFieldValue(iePosition));
            try {
                // dekodujeme ho
                value = IpfixDecoder.decode(dataType, elementData);

                log.debug("n: " + name + " | d: " + dataType + " | g: " + group + " | v: " + value);

                if (!ieList.containsKey(group)) {
//                    log.debug("Group " + group + "already in ieList!");
                    ieInfo = new ArrayList<>();
                    ieInfo.add(new IEHelper(name, value));
                    ieList.put(group, ieInfo);
                } else {
//                    log.debug("Group " + group + "is not in ieList! Adding it there!");
                    ieList.get(group).add(new IEHelper(name, value));
                }
            } catch (DataException bufe) { // by Tomas Verescak
                log.error("i.e. '" + name + "' (" + dataType + ") - received data has wrong datatype! (" + elementData.capacity() + " bytes)");
                log.error("Skipping this element DB exportation!");
                continue;
            } catch (UnsupportedDataTypeException udte) { // by Tomas Verescak
                log.error("i.e. '" + name + "' - Cannot decode datatype: " + dataType);
                log.error("Skipping this element DB exportation!");
                continue;
            }
            ACPdata.put(field.getElementID(), value);
        }// for every fieldspecifier

        log.debug("Data fields processing finished!");
//        log.debug("ieFields.size() = " + ieList.size());
        


            String[] colNames = null;
            Object[] values = null;
            groupEnum = ieList.keys();
            List<IEHelper> helperList;
            // Najprv sa insertuje main tabulka
            String actualGroup = "main";
            double refId = refId = mongoClient.getNextSequenceNumber("records_main_rid_seq");

            // ak sa vyskytuje main grupa
            if (ieList.containsKey(actualGroup)) {


//        for (String grupa : ieList.keySet()) {
//            System.out.print(grupa + ", ");
//        }

                helperList = ieList.get(actualGroup);
//                log.debug("helperList = " + helperList);
                colNames = new String[helperList.size() +1];
                values = new Object[helperList.size() +1];
                
                colNames[0] = "rid";                                                                            //mozno vyhodnejsie bude pouzit _id ako RID
                //colNames[0] = "_id";
                values[0] = refId;

                for (int i = 1; i < colNames.length; i++) {                   
                    colNames[i] = helperList.get(i-1).name;
                    values[i] = helperList.get(i-1).value;
                    //log.debug("RecordDispatcher252>>  c: "+ colNames[i] +"   v: "+ values[i] + "t: " + values[i].getClass().getName());
                }
                // ziskam id zaznamu v referencnej tabulke

                // najskor sa vlozia data pre tabulku records_main
                log.debug(" Storing " + (colNames.length) + " IPFIX fields into table: records_" + actualGroup);
                mongoClient.insertData("records_" + actualGroup, colNames, values);
                // RecordID.. pod tymto ID sa musia ulozit aj ostatne IE v inych tabulkach
                //refId = mongoClient.getCurrentSequenceNumber("records_main_rid_seq");                         //netreba na zaciatku sa do "refId" vlozi potrebna hodnota

            } else {
                // RecordID.. pod tymto ID sa musia ulozit aj ostatne IE v inych tabulkach
                //vlozime do main prazdny riadok a naneho sa potom budeme odkazovat

                colNames = new String[1];
                values = new Object[1];

                refId = mongoClient.getNextSequenceNumber("records_main_rid_seq");
                colNames[0] = "rid";                                                                            //mozno vyhodnejsie bude pouzit _id ako RID
                //colNames[0] = "_id";
                values[0] = refId;
                mongoClient.insertData("records_" + actualGroup, colNames, values);

            }


            //pokial mame skupiny
            while (groupEnum.hasMoreElements()) {
                actualGroup = (String) groupEnum.nextElement();
                if (actualGroup.equals("main")) {
                    continue; // hlavnu sme uz exportovali
                }

                helperList = ieList.get(actualGroup);   //zoznam ie pre danu skupinu
                colNames = new String[helperList.size() + 1]; // bude obsahovat RID
                values = new Object[helperList.size() + 1]; // bude obsahovat hodnotu RID, ktora bude zaradena ako posledna
                for (int i = 0; i < colNames.length - 1; i++) {
                    colNames[i] = helperList.get(i).name;
                    values[i] = helperList.get(i).value;
                }
                colNames[helperList.size()] = "rid";
                //colNames[helperList.size()] = "_id";
                values[helperList.size()] = refId;

                log.debug(" Storing " + (colNames.length - 1) + " IPFIX fields into table: records_" + actualGroup);
                mongoClient.insertData("records_" + actualGroup, colNames, values);
            }

        ieList.clear();
        // ieList = null;
        // data = null;
    }
    
     /**
     * Metóda slúži na parsovanie IE a hodnot pre ACP v prípade že export do 
     * databázy je vypnutý.
     * @return the data
     */
       private void ParseForACP(IPFIXTemplateRecord template,IPFIXDataRecord dataRecord){
         ACPdata.clear();
         String name, dataType;
         Object value;
            // pre vsetky polozky sablony
        for (FieldSpecifier field : template.getFields()) {
            long enterpriseNumber = (field.isEnterpriseBit()) ? field.getEnterpriseNumber() : 0;
            
            if (!elementsInfo.exists(field.getElementID(), enterpriseNumber)) {
                String enterprise = (enterpriseNumber != 0) ? String.format("[%d]", enterpriseNumber) : "";
                log.warn("Element with ID: " + field.getElementID() + enterprise + " is not supported, skipped! Update XML file!");
                continue;
            }
            name = elementsInfo.getElementName(field.getElementID(), enterpriseNumber); 
            dataType = elementsInfo.getElementDataType(field.getElementID(), enterpriseNumber);
            // ziskame si data konkretneho informacneho elementu
            int iePosition = template.getFieldSpecifierPosition(field.getElementID());
            ByteBuffer elementData = ByteBuffer.wrap(dataRecord.getFieldValue(iePosition));
            try {
                // dekodujeme ho
                value = IpfixDecoder.decode(dataType, elementData);

            } catch (DataException bufe) {
                log.error("i.e. '" + name + "' (" + dataType + ") - received data has wrong datatype! (" + elementData.capacity() + " bytes)");
                continue;
            } catch (UnsupportedDataTypeException udte) {
                log.error("i.e. '" + name + "' - Cannot decode datatype: " + dataType);
                continue;
            }
            ACPdata.put(field.getElementID(), value);
        }
    }

    /**
     * Uzavrie spojenie s databazou.
     * by Tomas Verescak
     */
    public void closeDBConnection() {
        mongoClient.disconnect();


    }
    
    /**
     *Metóda slúži na vrátenie Hashtable 
     * @return the data
     */
    public Hashtable getData() {
        return ACPdata;
    }

    private class IEHelper {

        private String name;
        private Object value;

        public IEHelper(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
