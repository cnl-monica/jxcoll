package sk.tuke.cnl.bm.JXColl.NETFLOW;

import java.nio.ByteBuffer;

/**
 * <p>Title: JXColl</p>
 *
 * <p>Description: Java XML Collector for network protocols</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: TUKE, FEI, CNL</p>
 * One field in the template
 * @author Lubos Kosco
 * @version 0.1
 */
public class FieldObject {
    // poznamka : vsetko co je int by malo byt short, no vdaka tomu, ze nemame unsigned datove typy, musime drzat ako short

    private int type;
    private int length; // 1 2 4 16   vie dekodovat zvysok programu
    private byte data[];
    /**
     * construct a field object of type with length
     *
     * @param type int type of this field
     * @param length int length in bytes of this field
     */
    public FieldObject(int type, int length) {
        this.type=type;
        this.length=length;
        data = new byte[length];

        hlp = ByteBuffer.wrap(data);
        //ByteBuffer xx =    ByteBuffer.wrap(data);
        // xx.
    }
    private ByteBuffer hlp;
    /**
     * set the data of this field from a byte
     *
     * @param src byte input
     */
    public void setData(byte src){
        hlp.put(0,src);
    }
    /**
     * set the data of this field from a short
     *
     * @param src short input
     */
    public void setData(short src){
        hlp.putShort(0,src);
    }
    /**
     * set the data of this field from a int
     *
     * @param src int input
     */
    public void setData(int src){
        hlp.putInt(0,src);
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }


    }
