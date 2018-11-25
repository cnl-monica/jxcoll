///*
//* Copyright (C) 2010 Lubos Kosco, Michal Kascak, Adrian Pekar, Tomas Verescak
//*
//* This file is part of JXColl.
//*
//* JXColl is free software; you can redistribute it and/or modify
//* it under the terms of the GNU General Public License as published by
//* the Free Software Foundation; either version 3 of the License, or
//* (at your option) any later version.
//
//* JXColl is distributed in the hope that it will be useful,
//* but WITHOUT ANY WARRANTY; without even the implied warranty of
//* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//* GNU General Public License for more details.
//
//* You should have received a copy of the GNU General Public License
//* along with JXColl; If not, see <http://www.gnu.org/licenses/>.
//*/
//package sk.tuke.cnl.bm.JXColl;
//
//import java.nio.BufferUnderflowException;
//import java.util.logging.Level;
//import javax.activation.UnsupportedDataTypeException;
//import sk.tuke.cnl.bm.JXColl.IPFIX.FieldSpecifier;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXMessage;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXOptionsTemplateRecord;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXSet;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateCache;
//import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
//import sk.tuke.cnl.bm.JXColl.accounting.AccountingManager;
//import sk.tuke.cnl.bm.JXColl.export.*;
//import org.apache.log4j.Logger;
//import java.util.Calendar;
//import java.nio.ByteBuffer;
//import java.net.*;
//
///**
// * This class parses the contents of incoming data, processes them and sends to
// * export classes.
// */
//@Deprecated
//public class NetXMLParserOld extends Thread {
//
//    private static Logger log = Logger.getLogger(IPFIXParser.class.getName());
////    private volatile boolean stop = false;
//    private byte[] packet;
//    private PacketObject pkt;
//    //private Calendar caldate = Calendar.getInstance();
//
//
//    /**
//     * constructor for the class
//     */
////    public NetXMLParser() {
////        super("Net Parser");
////       // caldate.setTimeZone(java.util.TimeZone.getDefault());
////    }
//
//    /**
//     * Set level of logging for this class
//     *
//     * @param level String Log Level
//     */
//    public void setlogl(String level) {
//        log.setLevel(org.apache.log4j.Level.toLevel(level));
//    }
//
//    /**
//     * method running all the time, consuming, parsing, processing and exporting
//     * the data from cache
//     */
//    @Override
//    public void run() {
//
//        while (!interrupted()) {           
//            try {
//            	//log.debug("PacketCache status: " + PacketCache.getSize());
//                pkt = PacketCache.read();
//                packet = pkt.getPacket();
//                int version = (packet[0] << 8) + packet[1];
//                //int size = (packet[2] << 8) + packet[3];   // v netflow je to count nie size
//                                                           // nedava stale spravnu hodnotu, treba prekontrolovat vypocet
//                //IPFIX sequence number discontinuities SHOULD be logged.
//                // IPFIX message - Added by Michal Kascak
//                if (version == 0x000a) {
/////                    log.debug("Incoming IPFIX packet ...");
//                    //log.info("Incoming IPFIX packet with size: " + size + " bytes"); // problemovy vypis, vid. vissie
//                    parseIPFIX(packet, pkt.getAddr());
//                } else {
//                    log.error("Version: "+version+" is unknown, probably not an IPFIX PACKET !!!");
//                } // throw exception
//
//            } catch (InterruptedException ie) {
////                log.debug("interrupted while reading from PacketCache");
//                interrupt(); // added by Tomas Verescak
//                dispatcher.closeDBConnection(); // added by Tomas Verescak
//
//            }
//        } //end stop
//
//        log.debug("NET XML PARSER Stopped!");
//    } //end run
//
//
//    // Objects representing IPFIX message - Added by Michal Kascak
//    private IPFIXMessage ipfixMessage = new IPFIXMessage();
//    private IPFIXSet ipfixSet;
//    private IPFIXTemplateRecord ipfixTemplate;
//    private IPFIXOptionsTemplateRecord ipfixOptionsTemplate;
//    private IPFIXDataRecord ipfixData;
//    private FieldSpecifier fieldSpecifier;
//    private IPFIXTemplateCache ipfixTemplateCache = new IPFIXTemplateCache();
//
//    // Flow Records Dispatcher
//
//    private RecordDispatcher dispatcher = new RecordDispatcher(); // vytvori sa este pred zavolanim metody run()
//
//    // Added by Michal Kascak
//    //TODO: unsignX() vsetky typy - done BUT NOT TESTED
//    //TODO: paddingy v recordoch osetrit!!!
//    private void parseIPFIX(byte data[], InetSocketAddress ipmb) {
//        int index = 0;
//        byte[] tempBuff;
//        ByteBuffer buff = ByteBuffer.wrap(data);
//        ipfixMessage.setHeader(buff);
//        index = ipfixMessage.getHeaderSize();
//        log.debug("****** IPFIX MESSAGE HEADER ******");
//        log.debug("Version number: " + ipfixMessage.getVersionNumber());
//        log.debug("Length: " + ipfixMessage.getLength() + " bytes");
//        log.debug("Export time: " + Support.SecToTimeOfDay(ipfixMessage.getExportTime()));
//        
////      Sequence number - This value SHOULD be used by the Collecting Process to identify whether any
////      IPFIX Data Records have been missed. Template and Options Template Records do not
////      increase the Sequence Number.
//        log.debug("Sequence number: " + ipfixMessage.getSequenceNumber());
//
////      Collecting Processes SHOULD use the Transport Session and the
////      Observation Domain ID field to separate different export streams
////      originating from the same Exporting Process. The Observation
////      Domain ID SHOULD be 0 when no specific Observation Domain ID is
////      relevant for the entire IPFIX Message, for example, when exporting
////      the Exporting Process Statistics, or in case of a hierarchy of
////      Collectors when aggregated Data Records are exported.
//        log.debug("Observation domain ID: " + ipfixMessage.getObservationDomainID());
//        //log.debug("Header size (Index value): " + index + " bytes");
//        // Iteracia cez vsetky Sety
//        while (index < ipfixMessage.getLength()) { // Nahradene za (index < buff.capacity())
//        //log.debug("****** ****** ******");
//        ipfixSet = new IPFIXSet();
//        int startSetIndex = index;  //Zaciatok setu
//        log.debug("****** SET HEADER ******");
////      Set ID value identifies the Set. A value of 2 is reserved for the
////      Template Set. A value of 3 is reserved for the Option Template
////      Set. All other values from 4 to 255 are reserved for future use.
////      Values above 255 are used for Data Sets. The Set ID values of 0
////      and 1 are not used for historical reasons [RFC3954].
//        log.debug("Set ID: " + buff.getShort(index));
//        log.debug("Set length: "+buff.getShort(index + 2));
//        ipfixSet.setHeader(buff.getShort(index), buff.getShort(index + 2));
//        index += ipfixSet.getHeaderSize();
//        //log.debug("****** ****** ******");
//        switch (ipfixSet.getSetID()) {
//          // Template Record
//          case 2:
//        	  //iteracia cez vsetky template recordy
//                  log.debug("****** TEMPLATE SET ******");
//        	  log.debug("Template record with ID " + Support.unsignShort(buff.getShort(index)) + " recognized ...");
//                  log.debug("Field Count: "+ Support.unsignShort(buff.getShort(index+2)));
//        	  // !!! treba unsignovat?
//        	  while (index < (startSetIndex + ipfixSet.getLength())) {
//	        	  // TU sa ukladaju uz unsigned datove typy!!!
//		          //trebalo by to prepisat aby to bolo jednotne
//		          int templateID = Support.unsignShort(buff.getShort(index));
//		          index += 2;
//		          int fieldCount = Support.unsignShort(buff.getShort(index));
//		          index += 2;
//		          ipfixTemplate = new IPFIXTemplateRecord(templateID, fieldCount);
/////                          log.debug("****** FIELD SPECIFIER ******");
//		          //iteracia cez vsetky Specifier Fieldy
//		          for (int i = 0; i < ipfixTemplate.getFieldCount(); i++) {
//			          fieldSpecifier = new FieldSpecifier();
//			          // metoda si zisti, ci je enterprise bit rovny jednej
//			          tempBuff = new byte[8];
//                                  //RIESENIE PADDINGU:
//                                  //log.debug(index+" ErrOR is CommING "+ (16 + ipfixSet.length));
//                                  if (index + 4 >= (16 + ipfixSet.getLength()) ) {
//                                      for (int j = 0; j < 4; j++) {
//                                          tempBuff[j] = buff.get(index + j);
//                                      }
//                                  } else {                                      
//                                      for (int j = 0; j < 8; j++) {
//                                          tempBuff[j] = buff.get(index + j);
//                                      }
//                                  }
//			          fieldSpecifier.setFieldSpecifier(tempBuff);
////                                   log.debug("Field specifier - Enterprise bit: " + fieldSpecifier.isEnterpriseBit());
////                                   log.debug("Field specifier - Information element ID: " + fieldSpecifier.getElementID());
////                                   log.debug("Field specifier - Length: " + fieldSpecifier.getFieldLength());
////                                   log.debug("Field specifier - Enterprise number: "+fieldSpecifier.getEnterpriseNumber());
//			          if (!fieldSpecifier.isEnterpriseBit()) {
//			        	  index += 4;
//			          }
//			          else {
//			        	  index += 8;
//			          }
//			          ipfixTemplate.addField(fieldSpecifier);
//		          }
//		          ipfixSet.addTemplateRecord(ipfixTemplate);
//		          // Uchovanie sablony v Cache
//		          ipfixTemplateCache.addTemplate(ipfixTemplate, ipmb, ipfixMessage.getObservationDomainID());
//		          }
//		          // TU treba osetrit Padding (Set->(record)+Padding)
//		          // Because Template Sets are always 4-octet aligned by definition,
//		          // padding is only needed in case of other alignments e.g. on 8-
//		          // octet boundaries.
//                  //UPDATE!!! PADING BY UZ MAL BYT VYRIESENY VID. VYSSIE!!!
//		          break;
//          case 3:
//              log.debug("****** OPTIONS TEMPLATE SET ******");
//              log.debug("Options record recognized ...");
//              while(index < (startSetIndex + ipfixSet.getLength())){
//                  int templateID = Support.unsignShort(buff.getShort(index)); index+=2;
//                  int fieldCount = Support.unsignShort(buff.getShort(index)); index+=2;
//                  int scopeFieldCount = Support.unsignShort(buff.getShort(index)); index+=2;
//                  ipfixOptionsTemplate = new IPFIXOptionsTemplateRecord(templateID, fieldCount, scopeFieldCount);
//
//                  //iteracia cez vsetky Scope Fieldy - mali by byt inac interpretovane (???)
//                  for(int i = 0; i < ipfixOptionsTemplate.getScopeFieldCount(); i++){
//                      fieldSpecifier = new FieldSpecifier();
//
//                      tempBuff = new byte[8];
//                      for(int j = 0; j < 8; j++)
//                          tempBuff[j] = buff.get(index+j);
//                      // metoda si zisti, ci je enterprise bit rovny jednej
//                      fieldSpecifier.setFieldSpecifier(tempBuff);
//                      if(!fieldSpecifier.isEnterpriseBit())
//                          index += 4;
//                      else
//                          index += 8;
//                      ipfixOptionsTemplate.addField(fieldSpecifier, true);
//                  }
//
//                  //iteracia cez vsetky Option fieldy
//                  for(int i = 0; i < ipfixOptionsTemplate.getFieldCount(); i++){
//                      fieldSpecifier = new FieldSpecifier();
//                      tempBuff = new byte[8];
//                      for(int j = 0; j < 8; j++)
//                          tempBuff[j] = buff.get(index+j);
//                      // metoda si zisti, ci je enterprise bit rovny jednej
//                      fieldSpecifier.setFieldSpecifier(tempBuff);
//                      if(!fieldSpecifier.isEnterpriseBit())
//                          index += 4;
//                      else
//                          index += 8;
//                      ipfixOptionsTemplate.addField(fieldSpecifier, false);
//                  }
//                  ipfixSet.addOptionsTemplateRecord(ipfixOptionsTemplate);
//                  //TODO: uchovat optionsTemplateSet do cache
//              }
//              // TU treba osetrit Padding (Set->(record)+Padding)
//              break;
//          default:
//	          //Data Record
//	          if (ipfixSet.getSetID() > 255) {
//                          log.debug("****** DATA SET ******");
//		          log.debug("Data record recognized ...");
//		          byte[] fieldValue;
//                          //log.warn("index: "+index);
//                          //log.warn("otherindex: "+startSetIndex + support.unsignS(ipfixSet.length));
//		          while (index < (startSetIndex + ipfixSet.getLength())) {
//		        	  if (ipfixTemplateCache.contains(ipfixSet.getSetID(), ipmb, ipfixMessage.getObservationDomainID())) {
//				          ipfixTemplate = ipfixTemplateCache.getByID(ipfixSet.getSetID(), ipmb, ipfixMessage.getObservationDomainID());
//				          ipfixData = new IPFIXDataRecord(ipfixTemplate.getTemplateID());
//				          for(FieldSpecifier fs : ipfixTemplate.getFields()){
//				              tempBuff = new byte[fs.getFieldLength()];
//
//				              for(int j = 0; j < fs.getFieldLength(); j++)
//				                  tempBuff[j] = buff.get(index+j);
//				              fieldValue = tempBuff;
//				              index += fs.getFieldLength();
//				              ipfixData.addFieldValue(fieldValue);
//				              //Tento vypis nerespektuje unsigned datove typy
//				              //log.debug("Incoming data of size " + fs.fieldLength + " bytes: " + new BigInteger(fieldValue).intValue());
//
//				          }
//			          }
//			          if(ipfixData == null){
//			              log.error("Template not found for data record!");
//			              // Posun na dalsi Set
//			              index = index - ipfixSet.getHeaderSize() + ipfixSet.getLength();
//			              break;
//			          } else {
//			        	  //ipfixSet.addDataRecord(ipfixData); //Pricina padu No.1
//			        	  // Vsetky flow recordy sa distribuuju do modulov cez RecordDispatcher-a
//			        	  dispatcher.dispatchIPFIXRecord(ipfixTemplate, ipfixData, ipmb);
//				          // Udaje pre DC
////				          if(Config.doACPTransfer)
////				          ACPServer.processIPFIXData((Inet4Address) ipmb, ipfixTemplate, ipfixData);
//				          // Vkladam flowRecord (so sablonou) do procesu uctovania
//			              //if(Config.doPGAccExport)
//			        	  //accManager.processFlow(ipfixTemplate, ipfixData);
//
//
////FOR DEBUG
////                                      IpfixElements ei = IpfixElements.getInstance();
////
////                                      String value = "";
////                                      String dataType;
////                                      for (FieldSpecifier fs : ipfixTemplate.getFields()) {
////
////                                          ByteBuffer ed = ByteBuffer.wrap(ipfixData.getFieldValue(ipfixTemplate.getFieldSpecifierPosition(fs.getElementID())));
////                                          dataType = ei.getElementDataType(fs.getElementID());
////                            try {
////                                value = dispatcher.decodeIpfixType(dataType, ed);
////                            } catch (UnknownHostException ex) {
////                                java.util.logging.Logger.getLogger(NetXMLParser.class.getName()).log(Level.SEVERE, null, ex);
////                            } catch (UnsupportedDataTypeException ex) {
////                                java.util.logging.Logger.getLogger(NetXMLParser.class.getName()).log(Level.SEVERE, null, ex);
//                            } catch (BufferUnderflowException ex) {
////                                java.util.logging.Logger.getLogger(NetXMLParser.class.getName()).log(Level.SEVERE, null, ex);
////                            }
//                                      //if (fs.getElementID() == 4 || fs.getElementID() == 7 || fs.getElementID() == 8 || fs.getElementID() == 11 || fs.getElementID() == 12 ||fs.getElementID() == 138 || fs.getElementID() == 60 || fs.getElementID() == 138 || fs.getElementID() == 156 || fs.getElementID() == 157)
//                                      //    log.debug("name: " + ei.getElementName(fs.getElementID()) + " | length: " + fs.getFieldLength() + " | dataType: " + dataType + " | value: " + value);
//                                      
//                                //      }
////FOR DEBUG
//			          }
//                                  //log.warn("WHILEEND!"+index);
//		          }
//	          } else {
//	          log.error("SetID " + ipfixSet.getSetID() + " is reserved for future use or not used for historical reasons.");
//	          }
//	          //break;
//          }//koniec switcha
//        //ipfixMessage.addSet(ipfixSet); //Pricina padu No.2
//        }//koniec while pre sety
//    }//koniec metody
//}