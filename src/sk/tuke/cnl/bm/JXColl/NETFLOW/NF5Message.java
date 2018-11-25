/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 *
 * @author esperian
 */
public class NF5Message{
    private int version;        //of protocol : for this type should be 5 ;)
    private int count;          //total no. of flows exported in this packet (1 to 30)
    private long sysuptime;         //time in ms since booted
    private long unixsec;        //current seconds since 0000 UTC 1970
    private long unixnsec;       //current seconds since 0000 UTC 1970
    private long flowseq;          //incremental counter of all Export Packets (check if there's one missing)
    private byte engine_type;    //type of flow switching engine
    private byte engine_id;               //id of flow switching engine
    private int reserved;
    private long receiveTime;
    private int sampling_interval;      //sampling mode and interval
                                                    /* first two bits represent mode:
                                                            00 - no sampling mode is configured
                                                            01 - 'packet interval' (one of every x packet is selected and placed in th netflow cache)
                                                            10 - reserved
                                                            11 - reserved
                                                    remaining 14 bits hold value of sampling interval (10-16382) (e.g. 0x000A to 0x3FFE)
         */
    
    private ArrayList<NF5FlowRecord> flow;
     
     
    public NF5Message(){
        flow=new ArrayList<>();
    }
    
    public void setHeader(ByteBuffer buffer){
       
        this.setVersion(Support.unsignShort(buffer.getShort()));
        this.setCount(Support.unsignShort(buffer.getShort()));
        this.setSysuptime(Support.unsignInt(buffer.getInt()));
        this.setUnixsec(Support.unsignInt(buffer.getInt()));
        this.setUnixnsec(Support.unsignInt(buffer.getInt()));
        this.setFlowseq(Support.unsignInt(buffer.getInt()));
        this.setEngine_type(buffer.get()); 
        this.setEngine_id(buffer.get());
        this.setSampling_interval(Support.unsignShort(buffer.getShort()));

    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count; 
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the sysuptime
     */
    public long getSysuptime() {
        return sysuptime;
    }

    /**
     * @param sysuptime the sysuptime to set
     */
    public void setSysuptime(long sysuptime) {
        this.sysuptime = sysuptime;
    }

    /**
     * @return the unixsec
     */
    public long getUnixsec() {
        return unixsec;
    }

    /**
     * @param unixsec the unixsec to set
     */
    public void setUnixsec(long unixsec) {
        this.unixsec = unixsec;
    }

    /**
     * @return the unixnsec
     */
    public long getUnixnsec() {
        return unixnsec;
    }

    /**
     * @param unixnsec the unixnsec to set
     */
    public void setUnixnsec(long unixnsec) {
        this.unixnsec = unixnsec;
    }

    /**
     * @return the flowseq
     */
    public long getFlowseq() {
        return flowseq;
    }

    /**
     * @param flowseq the flowseq to set
     */
    public void setFlowseq(long flowseq) {
        this.flowseq = flowseq;
    }

    /**
     * @return the engine_type
     */
    public byte getEngine_type() {
        return engine_type;
    }

    /**
     * @param engine_type the engine_type to set
     */
    public void setEngine_type(byte engine_type) {
        this.engine_type = engine_type;
    }

    /**
     * @return the engine_d
     */
    public byte getEngine_id() {
        return engine_id;
    }

    /**
     * @param engine_id the engine_id to set
     */
    public void setEngine_id(byte engine_id) {
        this.engine_id = engine_id;
    }

    /**
     * @return the reserved
     */
    public int getReserved() {
        return reserved;
    }

    /**
     * @param reserved the reserved to set
     */
    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    /**
     * @return the sampling_interval
     */
    public int getSampling_interval() {
        return sampling_interval;
    }

    /**
     * @param sampling_interval the sampling_interval to set
     */
    public void setSampling_interval(int sampling_interval) {
        this.sampling_interval = sampling_interval;
    }

    /**
     * @return the receiveTime
     */
    public long getReceiveTime() {
        return receiveTime;
    }

    /**
     * @param receiveTime the receiveTime to set
     */
    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * @return the flow
     */
    public ArrayList<NF5FlowRecord> getFlow() {
        return flow;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(ArrayList<NF5FlowRecord> flow) {
        this.flow = flow;
    }
    
    public void addFlow(NF5FlowRecord flow){
       getFlow().add(flow);
    }
}
