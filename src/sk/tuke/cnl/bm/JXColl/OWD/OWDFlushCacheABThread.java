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
 *  Súbor: OWDFlushCacheABThread.java
 */
package sk.tuke.cnl.bm.JXColl.OWD;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.PacketCache;

/**
 * Thread for wiping out OWD cacheA and cacheB from data, which are more than 2 x passiveTimeout seconds in the caches.
 *
 * @author Adrian Pekar
 */
public class OWDFlushCacheABThread extends Thread {

    private static Logger log = Logger.getLogger(OWDFlushCacheABThread.class.getName());
    private OWDObject owdA;
    private OWDObject owdB;

    /**
     * Set level of logging for this class.
     *
     * @param level String Log Level.
     */
    public void setlogl(String level) {
        log.setLevel(org.apache.log4j.Level.toLevel(level));
    }

    /**
     * The only constructor for this thread.
     */
    public OWDFlushCacheABThread() {
        super("ONE WAY DELAY FlushCache Thread.");
    }

    @Override
    public void run() {

        while (!interrupted()) {
            try {
                //log.debug("...sleep");
                Thread.sleep(1000);

//                if (OWDCache.getOwdListenerLock() == false) { // ked sa nepouzivaju cache OWD Listenerom
//                    OWDCache.setOwdFlushLock(true); // tak ho zacnes pouzivat ty
                // prejdem cacheA
                try {
                    for (int i = 0; i < OWDCache.getNumberOfElementsA(); i++) {
                        owdA = OWDCache.readA(i);
                        if ((System.currentTimeMillis() - owdA.getTimeStamp()) > (Config.owdPassiveTimeout * 2)) {
                            log.debug("OWD: cacheA: Packet is for " + (System.currentTimeMillis() - owdA.getTimeStamp()) / 1000F + " seconds in intercache A, wiping out...");
                            PacketCache.write(ByteBuffer.wrap(owdA.getPacket()), owdA.getAddr());
                            OWDCache.pullA(i);
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("interrupted in A block!");
                    interrupt();
                }
                // prejdem cacheB
                try {
                    for (int i = 0; i < OWDCache.getNumberOfElementsB(); i++) {
                        owdB = OWDCache.readB(i);
                        if ((System.currentTimeMillis() - owdB.getTimeStamp()) > (Config.owdPassiveTimeout * 2)) {
                            log.debug("OWD: cacheB: Packet is for " + (System.currentTimeMillis() - owdB.getTimeStamp()) / 1000F + " seconds in intercache B, wiping out...");
                            PacketCache.write(ByteBuffer.wrap(owdB.getPacket()), owdB.getAddr());
                            OWDCache.pullB(i);
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("interrupted in B block!");
                    interrupt();
                }
//                    OWDCache.setOwdFlushLock(false); // das vediet, ze ho uz nepouzivas
//                }
            } catch (InterruptedException e) {
                log.info("interrupted while sleeping!");
                interrupt();
            }

        }
        log.debug("ONE WAY DELAY FlushCache Stopped!");
    }
}
