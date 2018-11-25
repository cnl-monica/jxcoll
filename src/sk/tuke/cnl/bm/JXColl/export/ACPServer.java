/* Copyright (C) 2013 MONICA Research Group / TUKE 
 * 2009  Adrián Pekár, Pavol Beňko
 *
 * This file is part of JXColl v.3.
 *
 * JXColl v.3 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * JXColl v.3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JXColl v.3; If not, see <http://www.gnu.org/licenses/>.
 *
 *              Fakulta Elektrotechniky a informatiky
 *                  Technicka univerzita v Kosiciach
 *
 *  Monitorovanie prevádzkových parametrov siete v reálnom čase
 *                          Bakalárska práca
 *
 *  Veduci DP:        Ing. Juraj Giertl, PhD.
 *  Konzultanti DP:   Ing. Martin Reves
 *
 *  Bakalarant:       Adrián Pekár
 *
 *  Zdrojove texty:
 *  Subor: ACPServer.java
 */
package sk.tuke.cnl.bm.JXColl.export;

import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
import sk.tuke.cnl.bm.JXColl.*;
import java.net.*;
import java.io.*;
import org.apache.log4j.Logger;
import java.util.*;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;

public class ACPServer extends Thread {

    private static Logger log = Logger.getLogger(ACPServer.class.getName());
    private static List<ACPIPFIXWorker> threadList = new ArrayList<ACPIPFIXWorker>(IJXConstants.NUMBER_OF_ACP_THREADS);
    ThreadGroup acpWorkers = new ThreadGroup("ACP Worker thread group");
    private boolean alive = true;
    private ServerSocket server;
    protected boolean end;

    /**
     * Vytvorí ACP vlákno, ktoré bude čakať na TCP pripojenie cez port
     *
     * @param port na ktorom sa očakáva pripojenie
     * @throws IOException v prípade ked port je obsadený (alebo v prípade inej sieťovej chyby)
     */
    public ACPServer(int port) throws IOException {
        super("ACP Service");
        this.server = new ServerSocket(port);
        log.info("ACP server online on: " + port);
    }

    /**
     * Ďaľší konštruktor (ACPServer(int port))
     *
     * @param port Integer port
     * @throws IOException  v prípade ked port je obsadený (alebo v prípade inej sieťovej chyby)
     */
    public ACPServer(Integer port) throws IOException {
        this(port.intValue());
    }

    /**
     * Metóda pre čisté pozastavenie vlákna
     */
    public void die() {
        ACPIPFIXWorker worker;
        alive = false;
        for (int i = 0; i < IJXConstants.NUMBER_OF_ACP_THREADS; i++) {
            worker = (ACPIPFIXWorker) threadList.get(i);
            log.debug(i);
            worker.die();
        }
        try {
            server.close();
        } catch (IOException ex) {
//            log.error(ex);
        }
//        this.interrupt();
        this.stop();
    }

    @Override
    public void interrupt() {
       // super.interrupt();
        die();
    }
    
    

    /*public static void processData(InetAddress ipmb, NF9Template dtemplate) {
    DCWorker worker;
    
    //        for (int i = 0; i < IJXConstants.NUMTHREADS; i++) {
    //                    worker = (DCWorker) threadList.get(i);
    //                   if (worker.active) log.info(worker.getName()+ " " +worker.sfilter.toString());
    //                }
    
    for (int i = 0; i < IJXConstants.NUMTHREADS; i++) {
    worker = (DCWorker) threadList.get(i);
    if (worker!=null && worker.active) {
    worker.processData(new ExportData(dtemplate,ipmb));
    }
    }
    
    }
     */
    /**
     * Predá Ip adresu meracieho bodu, IPFIX šablónu a údaje pre jednotlivé vlákna
     *
     * @param ipmb IP adresa meracieho bodu
     * @param ipfixTemplate IPFIX šablóna
     * @param ipfixData IPFIX údaj
     */
    public static void processIPFIXData(InetSocketAddress ipmb, IPFIXTemplateRecord ipfixTemplate, IPFIXDataRecord ipfixData) {
        //throw new UnsupportedOperationException("Not yet implemented");
        ACPIPFIXWorker worker;
        //log.debug("TEMPLATE, DATA received, processing 5x");
        for (int i = 0; i < IJXConstants.NUMBER_OF_ACP_THREADS; i++) {
            worker = (ACPIPFIXWorker) threadList.get(i);
            if (worker != null && worker.isActive()) {
                //if (worker.filteraccepted == true && worker.templateaccepted == true){
                //if ( worker.ispaused == false){
                worker.dispatchIPFIXRecord((Inet4Address) ipmb.getAddress(), ipfixTemplate, ipfixData);
                //} else log.debug("Data sending Paused!");
                //}else log.debug("Collector not connected / Filter or Template not accepted!");
            }//else log.debug("No worker or inactive!");
        }
        //log.debug("TEMPLATE, DATA received, processed 5x");
    }

    /**
     * Vytvorí Workers a caká na pripojenie
     */
    public void run() {
        ACPIPFIXWorker worker;
        for (int i = 0; i < IJXConstants.NUMBER_OF_ACP_THREADS; i++) {
            worker = new ACPIPFIXWorker(acpWorkers, i);
            worker.start();
            threadList.add(worker);
            //log.debug("New DCW thread added to position: "+threadList.indexOf(t));
        }
        log.debug("Spawned " + IJXConstants.NUMBER_OF_ACP_THREADS + " workers."); //dcworkers.activeCount()
        while (alive) {
            try {
                Socket request = server.accept();
                ACPIPFIXWorker.processRequest(request);

                /**recheck a respawn workerov ak sa nieco stane ;) */
//                       if (dcworkers.activeCount() < IJXConstants.NUMTHREADS ) {
//                           log.error("Detected dead DC workers, respawning");
//                           int i;
//                           for ( i = 0; i < IJXConstants.NUMTHREADS; i++) {
//                                       worker = (DCWorker) threadList.get(i);
//                               if (!worker.isAlive()) { threadList.remove(i);worker=null;break; }
//                           }
//                           worker = new DCWorker(dcworkers,i);
//                           /**@todo bug ak v threadliste je mrtve vlakno*/
//                           worker.start();
//                           //threadList.add(i,worker);
//                           threadList.add(worker);
//                       }
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
