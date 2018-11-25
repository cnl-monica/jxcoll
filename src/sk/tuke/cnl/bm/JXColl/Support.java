/* 
 * Copyright (C) 2010 Lubos Kosco
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import sk.tuke.cnl.bm.JXColl.NETFLOW.NF5FlowRecord;

/**
 * Support class containing some helper methods
 */
public class Support {

    public Support() {
    }

    /**
     * unsign a byte into a short
     *
     * @param data byte
     * @return short
     */
    public static short unsignByte(byte data) {
        return (short) (data >= 0 ? data : 256 + data);
    }

    /**
     * unsign a short into an int
     *
     * @param data short
     * @return int
     */
    public static int unsignShort(short data) {
        return (data >= 0 ? data : 65536 + data);
    }

    /**
     * unsign an int into a long
     *
     * @param data int
     * @return long
     */
    public static long unsignInt(int data) {
        //private long halflong = ((Integer.MAX_VALUE+(long)1)*2); //4294967296
        return (data >= 0 ? data : 4294967296L + data);
    }

    public static long unsignLong(long data) {
        //private long halflong = ((Integer.MAX_VALUE+(long)1)*2); //4294967296
        return (data >= 0 ? data : data & 0xfffffffffffffffL);
    }
//SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMMMM yyyy HH:mm:ss Z");
// ISO 8601 format : 1997-12-17 07:37:16-08
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    ;
/**
* convert unix seconds to a text format of datetime according to ISO 8601
*
* @param sec long unix seconds since epoch
* @return String datetime in text format
*/
public static String SecToTimeOfDay(long sec) {
        dateFormat.setTimeZone(TimeZone.getDefault());
//int milli = 1000;
        String date = dateFormat.format(new Date(sec * 1000));
        return date;
}

    public static String intToIp(int i){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        InetAddress add=null;
            try {
             add = InetAddress.getByAddress(buffer.array());
            } catch (UnknownHostException ex) {
            Logger.getLogger(NF5FlowRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        return add.getHostAddress();
    }

    
}
