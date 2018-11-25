/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.tuke.cnl.bm.JXColl.NETFLOW;

import sk.tuke.cnl.bm.Templates;

/**
 *
 * @author esperian
 */
public class NF5FlowRecord {


    private int srcaddr;
    private int dstaddr;
    private int nexthop;
    private short input;
    private short output;
    private int dPkts;
    private int dOctets;
    private int first;
    private int last;
    private short srcport;
    private short dstport;
    private byte pad1;
    private byte tcp_flags;
    private byte prot;
    private byte tos;
    private short src_as;
    private short dst_as;
    private byte src_mask;
    private byte dst_mask;
    private short pad2;
    private NF5TemplateRecord NF5tem;

    public NF5FlowRecord(){
        NF5tem = new NF5TemplateRecord();
        NF5tem.addField(Templates.IPV4_SRC_ADDR,4);
        NF5tem.addField(Templates.IPV4_DST_ADDR,4);
        NF5tem.addField(Templates.IPV4_NEXT_HOP,4);
        NF5tem.addField(Templates.INPUT_SNMP,2);
        NF5tem.addField(Templates.OUTPUT_SNMP,2);
        NF5tem.addField(Templates.IN_PKTS,4);
        NF5tem.addField(Templates.IN_BYTES,4);
        NF5tem.addField(Templates.FIRST_SWITCHED,4);
        NF5tem.addField(Templates.LAST_SWITCHED,4);
        NF5tem.addField(Templates.L4_SRC_PORT,2);
        NF5tem.addField(Templates.L4_DST_PORT,2);
        NF5tem.addField(100, 1);
        NF5tem.addField(Templates.TCP_FLAGS,1);
        NF5tem.addField(Templates.PROTOCOL,1);
        NF5tem.addField(Templates.SRC_TOS,1);
        NF5tem.addField(Templates.SRC_AS,2);
        NF5tem.addField(Templates.DST_AS,2);
        NF5tem.addField(Templates.SRC_MASK,1);
        NF5tem.addField(Templates.DST_MASK,1);
        NF5tem.addField(100, 2);
    } 
   
    /**
     * @return the NF5tem
     */
    public NF5TemplateRecord getNF5tem() {
        return NF5tem;
    }

    /**
     * @return the srcaddr
     */
    public int getSrcaddr() {
        return srcaddr;
    }

    /**
     * @param srcaddr the srcaddr to set
     */
    public void setSrcaddr(int srcaddr) {
        this.srcaddr = srcaddr;
    }

    /**
     * @return the dstaddr
     */
    public int getDstaddr() {
        return dstaddr;
    }

    /**
     * @param dstaddr the dstaddr to set
     */
    public void setDstaddr(int dstaddr) {
        this.dstaddr = dstaddr;
    }

    /**
     * @return the nexthop
     */
    public int getNexthop() {
        return nexthop;
    }

    /**
     * @param nexthop the nexthop to set
     */
    public void setNexthop(int nexthop) {
        this.nexthop = nexthop;
    }

    /**
     * @return the input
     */
    public short getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(short input) {
        this.input = input;
    }

    /**
     * @return the output
     */
    public short getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(short output) {
        this.output = output;
    }

    /**
     * @return the dPkts
     */
    public int getdPkts() {
        return dPkts;
    }

    /**
     * @param dPkts the dPkts to set
     */
    public void setdPkts(int dPkts) {
        this.dPkts = dPkts;
    }

    /**
     * @return the dOctets
     */
    public int getdOctets() {
        return dOctets;
    }

    /**
     * @param dOctets the dOctets to set
     */
    public void setdOctets(int dOctets) {
        this.dOctets = dOctets;
    }

    /**
     * @return the first
     */
    public int getFirst() {
        return first;
    }

    /**
     * @param first the first to set
     */
    public void setFirst(int first) {
        this.first = first;
    }

    /**
     * @return the last
     */
    public int getLast() {
        return last;
    }

    /**
     * @param last the last to set
     */
    public void setLast(int last) {
        this.last = last;
    }

    /**
     * @return the srcport
     */
    public short getSrcport() {
        return srcport;
    }

    /**
     * @param srcport the srcport to set
     */
    public void setSrcport(short srcport) {
        this.srcport = srcport;
    }

    /**
     * @return the dstport
     */
    public short getDstport() {
        return dstport;
    }

    /**
     * @param dstport the dstport to set
     */
    public void setDstport(short dstport) {
        this.dstport = dstport;
    }

    /**
     * @return the tcp_flags
     */
    public byte getTcp_flags() {
        return tcp_flags;
    }

    /**
     * @param tcp_flags the tcp_flags to set
     */
    public void setTcp_flags(byte tcp_flags) {
        this.tcp_flags = tcp_flags;
    }

    /**
     * @return the prot
     */
    public byte getProt() {
        return prot;
    }

    /**
     * @param prot the prot to set
     */
    public void setProt(byte prot) {
        this.prot = prot;
    }

    /**
     * @return the tos
     */
    public byte getTos() {
        return tos;
    }

    /**
     * @param tos the tos to set
     */
    public void setTos(byte tos) {
        this.tos = tos;
    }

    /**
     * @return the src_as
     */
    public short getSrc_as() {
        return src_as;
    }

    /**
     * @param src_as the src_as to set
     */
    public void setSrc_as(short src_as) {
        this.src_as = src_as;
    }

    /**
     * @return the dst_as
     */
    public short getDst_as() {
        return dst_as;
    }

    /**
     * @param dst_as the dst_as to set
     */
    public void setDst_as(short dst_as) {
        this.dst_as = dst_as;
    }

    /**
     * @return the src_mask
     */
    public byte getSrc_mask() {
        return src_mask;
    }

    /**
     * @param src_mask the src_mask to set
     */
    public void setSrc_mask(byte src_mask) {
        this.src_mask = src_mask;
    }

    /**
     * @return the dst_mask
     */
    public byte getDst_mask() {
        return dst_mask;
    }

    /**
     * @param dst_mask the dst_mask to set
     */
    public void setDst_mask(byte dst_mask) {
        this.dst_mask = dst_mask;
    }
}
