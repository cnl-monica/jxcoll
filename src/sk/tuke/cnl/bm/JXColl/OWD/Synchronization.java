/* Copyright (C) 2011  Adrian Pekar
 *
 * This file is part of JXColl v.3.6.
 *
 * JXColl v.3.6 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * JXColl v.3.6 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JXColl v.3.6; If not, see <http://www.gnu.org/licenses/>.
 *
 *              Fakulta Elektrotechniky a informatiky
 *                  Technicka univerzita v Kosiciach
 *
 *  Optimalizácia zhromažďovacieho procesu nástroja BasicMeter
 *                          Diplomová práca
 *
 *  Vedúci DP:        Ing. Juraj Giertl, PhD.
 *  Konzultanti DP:   Ing. Martin Reves, PhD.
 *
 *  Diplomant:       Adrián Pekár
 *
 *  Zdrojové texty:
 *  Súbor: Synchronization.java
 */

package sk.tuke.cnl.bm.JXColl.OWD;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.apache.log4j.Logger;

/**
 * Thread for exporter time synchronization. Acts as a synchronization server.
 *
 * @author Adrian Pekar
 */
public class Synchronization extends Thread{

    private static Logger log = Logger.getLogger(Synchronization.class.getName());
    private DatagramSocket ds;

    /**
     * Constructor for the class.
     * 
     * @param port int port for synchronization packets.
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public Synchronization(int port) throws SocketException, IOException {
        ds = new DatagramSocket(port);
    }

     public void run() {

        DatagramPacket packet = new DatagramPacket(new byte[66], 66); // packet header ma 42 bytov + 3 * 64 bit, prvy je sekvencne cislo, druhy je cas exportera, do tretieho pridavam aktualny cas

        log.info("Synchronization listening on: " + ds.getLocalSocketAddress());
        while(!interrupted()){
            try {
                // receive packet
                ds.receive(packet);
                
                // get current time in seconds
                BigInteger sec = new BigInteger(Long.toBinaryString((System.currentTimeMillis()*1000000) ), 2);
                // get current time in nanoseconds
                //BigInteger nSec = new BigInteger(Long.toBinaryString(System.nanoTime()), 2);

                // convert seconds from BigInteger to ByteArray
                byte[] bsec  = sec.toByteArray();
                // get the data part of the received packet into a byte array
                byte[] packetData = packet.getData();

                // change bite order
                byte[] curTime = new byte [8];
                curTime[0] = bsec[7];
                curTime[1] = bsec[6];
                curTime[2] = bsec[5];
                curTime[3] = bsec[4];
                curTime[4] = bsec[3];
                curTime[5] = bsec[2];
                curTime[6] = bsec[1];
                curTime[7] = bsec[0];

                // add current time to received packet
                System.arraycopy(curTime, 0, packetData, 16, 8);

                // send back packet with current time
                DatagramPacket sendPacket = new DatagramPacket(packetData, 24, packet.getSocketAddress());
                ds.send(sendPacket);
                // output
                log.debug("Synchronzation packet from " + packet.getSocketAddress() + ", responding with current time (sec): " + sec.longValue() );
                //System.out.println("Packet from " + packet.getSocketAddress() + ", size " + packet.getLength());
                //System.out.println("Current Time Seconds:"+sec.longValue());
                //System.out.println("Current Time NanoS:  "+nSec.longValue());
            } catch (IOException ex) {
               // Logger.getLogger(Synchronization.class.getName()).log(Level.SEVERE, null, ex);
               log.debug("Synchronization socekt after shutdown interruption closed!");
            }
        }
    }

     @Override
   public void interrupt(){
     super.interrupt();
     this.ds.close();  
   }
}
