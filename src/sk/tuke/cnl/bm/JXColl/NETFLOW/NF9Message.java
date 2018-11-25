package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.nio.ByteBuffer;
import sk.tuke.cnl.bm.JXColl.Support;

/**
 * <p>Title: JXColl</p>
 *
 * <p>Description: Java XML Collector for network protocols</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: TUKE, FEI, CNL</p>
 * represents Netflow 9 Header
 * @author Lubos Kosco, Pavol Benko
 * @version 0.1
 */
public class NF9Message{
     private int version;
     private int count;
     private long sysuptime;
     private long unixsec;
     private long psequence;
     private long sourceid;
     private long receiveTime;
     
    public NF9Message() {
    }

    /**
      * read & fill all fields from a ByteBuffer
      *
      * @param buffer ByteBuffer input
     */
    public void setHeader(ByteBuffer buffer) {
        this.setUnixsec(0);      
        this.setPsequence(0);
        
        this.setVersion(Support.unsignShort(buffer.getShort()));
        this.setCount(Support.unsignShort(buffer.getShort()));
        this.setSysuptime(Support.unsignInt(buffer.getInt()));
        this.setUnixsec(Support.unsignInt(buffer.getInt()));
        this.setPsequence(Support.unsignInt(buffer.getInt()));
        this.setSourceid(Support.unsignInt(buffer.getInt()));
        
    }
    
    /**
     * return the size of this header
     *
     * @return int size in bytes
     */
    public int getSize() {
        return 20;
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
     * @return the psequence
     */
    public long getPsequence() {
        return psequence;
    }

    /**
     * @param psequence the psequence to set
     */
    public void setPsequence(long psequence) {
        this.psequence = psequence;
    }

    /**
     * @return the sourceid
     */
    public long getSourceid() {
        return sourceid;
    }

    /** 
     * @param sourceid the sourceid to set
     */
    public void setSourceid(long sourceid) {
        this.sourceid = sourceid;
    }
}
