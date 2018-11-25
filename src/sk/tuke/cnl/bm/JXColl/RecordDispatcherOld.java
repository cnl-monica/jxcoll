///* 
// * Copyright (C) 2010 Lubos Kosco, Adrian Pekar, Tomas Verescak
// *
// * This file is part of JXColl.
// *
// * JXColl is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 3 of the License, or
// * (at your option) any later version.
//
// * JXColl is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with JXColl; If not, see <http://www.gnu.org/licenses/>.
// */
//package sk.tuke.cnl.bm.JXColl;
//
//import java.io.IOException;
//import java.math.BigInteger;
//import java.net.Inet4Address;
//import java.net.Inet6Address;
//import java.net.InetSocketAddress;
//import java.net.UnknownHostException;
//import java.nio.BufferUnderflowException;
//import java.nio.ByteBuffer;
//import java.nio.charset.Charset;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import java.util.List;
//import javax.activation.UnsupportedDataTypeException;
//import org.apache.log4j.Logger;
//import org.dom4j.Document;
//import org.dom4j.Element;
//import sk.tuke.cnl.bm.JXColl.IPFIX.FieldSpecifier;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
//import sk.tuke.cnl.bm.JXColl.accounting.AccountingManager;
//import sk.tuke.cnl.bm.JXColl.export.ACPServer;
//import sk.tuke.cnl.bm.JXColl.export.PGClient;
//
///**
// * Class responsibility is to distribute received and parsed data structured
// * in conformance with IPFIX or NetFlow messages into particular modules of JXColl.
// * @author Michal Kascak, Adrian Pekar, Tomas Verescak
// */
//public class RecordDispatcher {
//
//    private static Logger log = Logger.getLogger(RecordDispatcher.class.getName());
//    /** XML dokument opisujuci vsetky informacne elementy IPFIX */
////    private Document ieXml = null;
////    private Element rootElement = null;
//    private List<IEHelper> ieInfo;
//    private Hashtable<String, List> ieList;
//    private Enumeration groupEnum;
//    /** objekt pre pristup k databaze */
//    private PGClient pgClient;
//    /** Modul uctovania */
//    private AccountingManager am;
//    /** ACP modul */
//    protected static ACPServer acpserver = null;
////    private String temp;
//    private IpfixElements elementsInfo;
//
//    /** Vytvara novu instanciu triedy */
//    @Deprecated
//    public RecordDispatcher() {
//        ieList = new Hashtable<String, List>();
//        if (Config.doACPTransfer) {
//            try {
//                acpserver = new ACPServer(Config.acpport);
//                acpserver.start();
//                JXColl.acpserver = acpserver;
//            } catch (IOException e) {
//                log.error("ACP server could not start because of an " + e.getClass() + " : " + e);
//                JXColl.stopJXColl();
//            }
//        }
//
//        if (Config.doPGexport) {
//            pgClient = new PGClient();
//        }
//        // informacie o IPFIX informacnych elementoch
//        elementsInfo = IpfixElements.getInstance();
//
//        if (Config.doPGAccExport) {
//            am = new AccountingManager();
//        }
//    }
//
//    /**
//     * Posunie prijate data a im zodpovedajucu sablonu vsetkym aktivnym modulom
//     * kolektora, ktore pracuju s IPFIX spravami
//     *
//     * @param template
//     *            sablona data
//     * @param data
//     *            Datovy zaznam
//     */
//    @Deprecated
//    public void dispatchIPFIXRecord(IPFIXTemplateRecord template,
//            IPFIXDataRecord data, InetSocketAddress ipmb) {
//
//        // flowRecord (so sablonou) pre DB
//        if (Config.doPGexport) {
//            dbExport(template, data);
//        }
//
//        // flowRecord (so sablonou) pre ACP
//        if (Config.doACPTransfer) {
//            acpserver.processIPFIXData((InetSocketAddress) ipmb, template, data);
//        }
//
//        // flowRecord (so sablonou) pre Accounting
//        if (Config.doPGAccExport) {
//            am.processFlow(template, data);
//        }
//    }
//
//    /**
//     * Metoda sluzi na export vsetkych nameranych dat poslanych protokolom IPFIX
//     * do databazy. Pri prvom prechode funkciou sa generuje pamatovy zaznam o
//     * informacnych elementoch (ie) z XML suboru. Vytiahnu sa informacie o ie,
//     * ktore sa nachadzaju v sablone, dekoduju sa ich datove typy a prislusnost
//     * k skupine pre ulozenie hodnot do databazy.
//     *
//     * @param template
//     *            sablona dat
//     * @param dataRecord
//     *            Datovy zaznam
//     */
//    private void dbExport(IPFIXTemplateRecord template, IPFIXDataRecord dataRecord) {
//
//        String name;
//        String dataType;
//        String group;
//        String value;
//        // String xpath;
//        // Element actualField;
//
//
//        log.debug("Processing data within data record!");
//
//        // pre vsetky polozky sablony
//        for (FieldSpecifier field : template.getFields()) {
//
//            if (!elementsInfo.exists(field.getElementID())) { // by Tomas Verescak
//                log.error("Element with ID: " + field.getElementID() + " is not supported, skipped! Update XML file!");
//                continue;
//            }
//
//            // informacny element typu paddingOctets mozeme preskocit!
//            if (field.getElementID() == 210) {
//                continue;
//            }
//
//
//            name = elementsInfo.getElementName(field.getElementID()); // by Tomas Verescak
//            dataType = elementsInfo.getElementDataType(field.getElementID());
//
//            // hodnoty pre main tabulku, tieto hodnoty maju povodne inu tabulku ale my ich davame do main
//            if (field.getElementID() == 1 || //octetDeltaCount
//                    field.getElementID() == 2 || //packetDeltaCount
//                    field.getElementID() == 4 || //protocolIdentifier
//                    field.getElementID() == 7 || //sourceTransportPort
//                    field.getElementID() == 8 || //sourceIPv4Address
//                    field.getElementID() == 11 || //destinationTransportPort
//                    field.getElementID() == 12 || //destinationIPv4Address
//                    field.getElementID() == 85 || //octetTotalCount
//                    field.getElementID() == 86 || //packetTotalCount
//                    field.getElementID() == 136 || //flowEndReason
//                    field.getElementID() == 138 || //observationPointId
//                    field.getElementID() == 148 || //flowID
//                    field.getElementID() == 152 || //flowStartMilliseconds
//                    field.getElementID() == 153 //flowEndMilliseconds
//                    ) {
//                group = "main";
//            } else {
//                // group = actualField.attributeValue("group");
//                group = elementsInfo.getElementGroup(field.getElementID()); // by Tomas Verescak
//            }
//            // log.debug("Element Data Type: " + dataType);
//
//            // log.debug("Parsed template field: name: " + name +
//            // " datatype: " + dataType + " group: " + group);
//
//            ByteBuffer elementData = ByteBuffer.wrap(dataRecord.getFieldValue(template.getFieldSpecifierPosition(field.getElementID())));
//            try {
//                value = IPFIXDecoder.decodeData(dataType, elementData);
//
////				log.debug("Parsed template field: name:          " + name);
////				log.debug("Parsed template field: datatype:      " + dataType);
////				log.debug("Parsed template field: group:         " + group);
////				log.debug("Parsed template field: decoded Value: " + value);
//
//                log.debug("name: " + name + " | datatype: " + dataType + " | group: " + group + " | value: " + value);
//
//                // log.debug("!!! Decoded Value: " + value);
//
//                // if(fs.elementID == 500)
//                // {
//                // //log.debug("###name:"+name);
//                // //log.debug("###datatype:"+dataType);
//                // log.debug("!!! DEBUG Value: "+value);
//                // }
//                // //log.debug("#####"+fs.elementID);
//                //
//                if (!ieList.containsKey(group)) {
//                    ieInfo = new ArrayList<IEHelper>();
//                    ieInfo.add(new IEHelper(name, value));
//                    ieList.put(group, ieInfo);
//                } else {
//                    ieList.get(group).add(new IEHelper(name, value));
//                }
//            } catch (JXCollDataException bufe) { // by Tomas Verescak
//                log.error("i.e. '" + name + "' (" + dataType + ") - received data has wrong datatype! (" + elementData.capacity() + " bytes)");
//                log.error("Skipping this element DB exportation!");
//                continue;
//            } catch (UnsupportedDataTypeException udte) { // by Tomas Verescak
//                log.error("i.e. '" + name + "' - Cannot decode datatype: " + dataType);
//                log.error("Skipping this element DB exportation!");
//                continue;
//            }
//        }
//        log.debug("Data fields processing finished!");
//
//        String[] colNames;
//        String[] values;
//        groupEnum = ieList.keys();
//        List<IEHelper> helperList;
//
//        // Najprv sa insertuje main tabulka
//        String actualGroup = "main";
//        helperList = ieList.get(actualGroup);
//        colNames = new String[helperList.size()];
//        values = new String[helperList.size()];
//        for (int i = 0; i < colNames.length; i++) {
//            colNames[i] = helperList.get(i).name;
//            values[i] = helperList.get(i).value;
//        }
//
//        // ziskam id zaznamu v referencnej tabulke
//        try {
//            pgClient.insertData("records_" + actualGroup, colNames, values);
//            long refId = pgClient.getCurrentSequenceNumber("records_main_rid_seq");
//            // insertData("records_" + actualGroup, colNames, values);
//
//            while (groupEnum.hasMoreElements()) {
//                actualGroup = (String) groupEnum.nextElement();
//                if (actualGroup.equals("main")) {
//                    continue;
//                }
//                helperList = ieList.get(actualGroup);
//                colNames = new String[helperList.size() + 1];
//                values = new String[helperList.size() + 1];
//                for (int i = 0; i < colNames.length - 1; i++) {
//                    colNames[i] = helperList.get(i).name;
//                    values[i] = helperList.get(i).value;
//                }
//                colNames[helperList.size()] = "rid";
//                values[helperList.size()] = Long.toString(refId);
//
//                // TODO fyzicky zapis na databazu
//                // insertData("records_" + actualGroup, colNames, values);
//                pgClient.insertData("records_" + actualGroup, colNames, values);
//            }
//        } catch (SQLException ex) {
//            log.error(ex.getMessage());
//        }
//        ieList.clear();
//        // ieList = null;
//        // data = null;
//    }
//
//    /**
//     * Uzavrie spojenie s databazou.
//     * by Tomas Verescak
//     */
//    public void closeDBConnection() {
//        pgClient.disconnect();
//    }
//
//    private class IEHelper {
//
//        private String name;
//        private String value;
//
//        public IEHelper(String name, String value) {
//            this.name = name;
//            this.value = value;
//        }
//    }
//}
