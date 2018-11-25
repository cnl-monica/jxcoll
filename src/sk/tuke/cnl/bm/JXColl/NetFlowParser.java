/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm.JXColl;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.DataFormatException;
import sk.tuke.cnl.bm.JXColl.NETFLOW.FieldObject;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF5FlowRecord;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF5Message;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF9Message;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF9Template;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF9TemplateCache;
import sk.tuke.cnl.bm.TemplateException;
import sk.tuke.cnl.bm.Templates;

/**
 *
 * @author esperian
 */
public class NetFlowParser {

    private static Logger log = Logger.getLogger(NetFlowParser.class.getName());

//    private NF9Message h9 = new NF9Message();
//    private NF9TemplateCache templcache = new NF9TemplateCache();
//    private NF9Template template;
    private FieldObject fieldobj;
    private Calendar caldate = Calendar.getInstance();
    Support Support;
    private RecordDispatcher dispatcher = RecordDispatcher.getInstance();
    NF9TemplateCache cache = new NF9TemplateCache();
    
    public enum TransportProtocol {

        UDP, TCP, SCTP;
    }
//
    public NetFlowParser(){
    }
    
    public NF5Message parseNetflow5Message(ByteBuffer buffer, InetSocketAddress ipfixDevice, long time) throws DataFormatException, TemplateException {
        System.out.println(buffer.toString());
        NF5Message message = new NF5Message();
        //nastavime cas prijatia spravy
        message.setReceiveTime(time);
        //z buffra vyberieme data o NF5 sprave
        message.setHeader(buffer);
        //vypiseme ich na obrazovku
        netflow5MessageHeader(message);
        
        for(int i=0;i<message.getCount();i++){
                
            try {
                log.debug("***** Flow number "+(i+1)+" *****");
                message.addFlow(parseNF5Flow(buffer));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(NetFlowParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        dispatcher.dbExportNetflow5(message);
        System.out.println(buffer.toString());
        return message;
    }
        
    public NF5FlowRecord parseNF5Flow(ByteBuffer buffer) throws IllegalArgumentException, IllegalAccessException{
        NF5FlowRecord nf = new NF5FlowRecord();
        
            for(int i=0;i<nf.getNF5tem().getFields().size();i++){
                if(!nf.getClass().getDeclaredFields()[i].getName().equals("NF5tem")){
                int length=nf.getNF5tem().getFields().get(i).getLength();
                Field d =nf.getClass().getDeclaredFields()[i];
                d.setAccessible(true);
                switch(length){
                    case 1:                         
                        d.setByte(nf, buffer.get());
                        nf.getNF5tem().getFields().get(i).setData(d.getByte(nf));
                        break;
                    case 2: 
                        d.setShort(nf, buffer.getShort());
                        nf.getNF5tem().getFields().get(i).setData(d.getShort(nf));
                        break;
                    case 4: 
                        d.setInt(nf, buffer.getInt());
                        nf.getNF5tem().getFields().get(i).setData(d.getInt(nf));
                        break;
                }
              }
            }
            printFlowV5(nf);
        return nf;
    }
    
    public NF9Message parseNetflow9Message(ByteBuffer buffer, InetSocketAddress ipfixDevice, long time) throws DataFormatException, TemplateException {

        NF9Message message = new NF9Message();
        //nastavime cas prijatia spravy
        message.setReceiveTime(time);
        // z hlavicky vyberieme data o IPFIX sprave
        message.setHeader(buffer);
        // vypiseme ich na obrazovku
        netflow9MessageHeader(message);
        
        parseNF9(buffer, ipfixDevice,message);
        
        return message;
    }
    
    public void parseNF9(ByteBuffer buff, InetSocketAddress ipfixDevice,NF9Message nf) {
        Object[] data;
        //ulozime data z hlavicky
        Object[] headerData= new Object[7];
        headerData[0]=nf.getVersion();
        headerData[1]=nf.getCount();
        headerData[2]=nf.getSysuptime();
        headerData[3]=nf.getUnixsec();
        headerData[4]=nf.getPsequence();
        headerData[5]=nf.getSourceid();
        headerData[6]=nf.getReceiveTime();
        dispatcher.dbExportNetflow9(null, headerData, "HR");
        NF9Template template = null;
        for (int flows = 0; flows < nf.getCount();) {
        
        int flowsetID = buff.getShort();
        int length = buff.getShort();
        length-=4;
        log.debug("flowsetID "+flowsetID +" length "+length);
        
        switch(flowsetID){
            case 0:
                template = new NF9Template();
                log.debug("*** Template flowset ***");
                for (int j = length; j > 0; ) {  
                    
                    int templateId= buff.getShort();
                    int fieldCount= buff.getShort();
                    log.debug("Template ID "+templateId+ " Field Count "+fieldCount);

                    template.templateID=templateId;
                    template.fieldCount=fieldCount;
                    j-=((fieldCount*4)+4);
                    for(int i=0; i<fieldCount;i++){
                       int typeField= buff.getShort();
                       int lengthField = buff.getShort();
                       log.debug("Type Field " +typeField+" ("+Templates.getFiledName(typeField)+")"+ " Length Field "+lengthField);
                       template.addField(typeField, lengthField);
                    }
                    cache.addTemplate(template, ipfixDevice.getAddress(), nf.getSourceid());
                    
                    flows++;
                }
                break;
            case 1: 
                flowsetID= buff.getShort();
                length = buff.getShort();
                int olength = buff.getShort();
                
                log.debug("Optional template flowset");
                log.debug("flowsetID "+flowsetID +" length  "+length);
                buff.position(buff.position()+length);
                log.debug(buff.toString());
                System.out.println("TODO");
                break; 
            default : 
                if (cache.contains(flowsetID,ipfixDevice.getAddress(),nf.getSourceid()) ) {
                
                
                NF9Template tem= cache.getByID(flowsetID, ipfixDevice.getAddress(), nf.getSourceid());
                data= new Object[tem.fieldCount];
                
                
                log.debug("*** Data record ***");
                for (int q = 0; q < length/template.getSize(); q++) {
                for(int count=0; count<tem.fieldCount;count++){
                    
                    switch(tem.getField(count).getLength()){
                        case 1: data[count] =buff.get(); break;
                        case 2: data[count] =buff.getShort(); break;
                        case 4: data[count] =buff.getInt(); break;
                    }                     
                }
                
                printFlowV9(template, data);
                dispatcher.dbExportNetflow9(template, data, "DR");
                log.debug(buff.toString());
                flows++;
                }
                
                int zvysok = length % template.getSize();
                    //citaj padding !!!!
                log.debug("Padding seek: "+zvysok);
                if (zvysok>0 && zvysok<4){
                    for(int del=0;del<zvysok;del++){
                        buff.get();
                    }
                }
                log.debug(buff.toString());
                }else{
                log.warn("Template neexistuje !!");
                }
                break;
        }
        
        
        }
        
    }
        
    private void netflow9MessageHeader(NF9Message message) {
        log.debug("****** NETFLOW MESSAGE HEADER v.9 ******");
        log.debug("version number: " + message.getVersion());
        log.debug("count: " + message.getCount());
        log.debug("sysuptime: " + message.getSysuptime());
        log.debug("unixsec: " + message.getUnixsec());
        log.debug("psequence: " + message.getPsequence());
        log.debug("sourceid: " + message.getSourceid());

        Date date = new Date(message.getReceiveTime());
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        log.debug("receiveTime: " + dateFormatted);
    }
    
    private void printFlowV9(NF9Template template, Object[] o){
        
        for(int i=0;i<template.fieldCount; i++){
            if(template.getField(i).getType()==Templates.IPV4_SRC_ADDR || template.getField(i).getType()==Templates.IPV4_DST_ADDR ||
               template.getField(i).getType()==Templates.IPV4_NEXT_HOP){
                log.debug("Type: "+template.getField(i).getType() +" ("+Templates.getFiledName(template.getField(i).getType())+")"+"  "+Support.intToIp((int)o[i]));
            }else{
                log.debug("Type: "+template.getField(i).getType() +" ("+Templates.getFiledName(template.getField(i).getType())+")"+"  "+o[i]);
            }
        }
    }
    
    private void netflow5MessageHeader(NF5Message message) {
        log.debug("****** NETFLOW MESSAGE HEADER v.5 ******");
        log.debug("version number: " + message.getVersion());
        log.debug("count: " + message.getCount());
        log.debug("sysuptime: " + message.getSysuptime() +"milliseconds");
        log.debug("unixsec: " + Support.SecToTimeOfDay(message.getUnixsec()));
        log.debug("unixnsec: " + message.getUnixnsec());
        log.debug("flowseq: " + message.getFlowseq());
        log.debug("engine_type: " + message.getEngine_type());
        log.debug("engine_id: " + message.getEngine_id());
        log.debug("sampling_interval: " + message.getSampling_interval());
        
        Date date = new Date(message.getReceiveTime());
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        log.debug("receiveTime: " + dateFormatted);
    }
    
        public void printFlowV5(NF5FlowRecord flow){
        log.debug("****** Flow data record format ******");
        log.debug("srcaddr "+Support.intToIp(flow.getSrcaddr()));
        log.debug("dstaddr "+Support.intToIp(flow.getDstaddr()));
        log.debug("nexthop "+Support.intToIp(flow.getNexthop()));
        log.debug("input "+flow.getInput());
        log.debug("output "+flow.getOutput());
        log.debug("dPkts "+flow.getdPkts());
        log.debug("dOctets "+flow.getdOctets());
        log.debug("first "+flow.getFirst());
        log.debug("last "+flow.getLast());
        log.debug("srcport "+flow.getSrcport());
        log.debug("dstport "+flow.getDstport());
        log.debug("tcp_flags "+flow.getTcp_flags());
        log.debug("prot "+flow.getProt());
        log.debug("tos "+flow.getTos());
        log.debug("src_as "+flow.getSrc_as());
        log.debug("dst_as "+flow.getDst_as());
        log.debug("src_mask "+flow.getSrc_mask());
        log.debug("dst_mask "+flow.getDst_mask());
    }

}
