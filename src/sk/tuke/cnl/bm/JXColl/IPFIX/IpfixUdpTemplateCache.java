/* 
 * Copyright (C) 2010 Lubos Kosco, Michal Kascak, Tomas Verescak
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
package sk.tuke.cnl.bm.JXColl.IPFIX;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.Config;

/**
 * Cache na ukladanie sablon IPFIX spravy.
 * @author Michal Kascak, Tomas Verescak
 */
public class IpfixUdpTemplateCache {

    private Map<ExporterKey, TemplateHolder> exporters;
    private static IpfixUdpTemplateCache instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        if (Config.receiveUDP) {
            instance = new IpfixUdpTemplateCache();
        }
    }

    /**
     * Zrusi vykonavanie cistiaceho vlakna
     */
    public final void cancelCleaningTask() {
        scheduler.shutdownNow();
    }

    /**
     * Nastavi vykonavanie cistiaceho vlakna kazdu minutu
     */
//    public final void setUpCleaningTaskOld() {
//        final Runnable cleaner = new Runnable() {
//
//            public void run() {
//
//                Logger log = Logger.getLogger("Template cleaner");
////                log.debug("Cleaner thread started!");
//
//                for (ExporterKey key : exporters.keySet()) {
//                    TemplateHolder templates = exporters.get(key);
//                    for (IPFIXTemplateRecord template : templates.getTemplates().values()) {
//                        if (!template.isValid()) {
//                            templates.remove(template.getTemplateID());
//                            log.info(String.format("Template #%d is no longer valid, removed from cache!", template.getTemplateID()));
//                            // a navyse checkni vstupnu cache
//                            if (PacketCache.isExporterPresent(key) && templates.isEmpty()) { // ak exporter je pritomny, nesmie byt pre exporter ziadna sablona
//                                try {
//                                    // vymazeme cache ak je prazdna
//                                    if (PacketCache.removeCacheIfEmpty(key) == true) {
//                                        // vymazeme tiez vlakno pre tento exporter nech nezavadzia
//                                        UDPServerOld.removeProcessor(key);
//                                        log.info(String.format("Cache and thread for exporter %s:%d (odid: %d) is empty, removed from multicache!", key.getIpfixDevice(), key.getExporterSrcUdpPort(), key.getObservationDomainId()));
//                                    }
//                                } catch (JXCollException ex) {
//                                    // ked je cache vymazana medzi tym
//                                    log.info(ex.getMessage());
//                                }
//                            } // ak je pritomny dany exporter
//                            else {
//                                log.debug(String.format("Cache for exporter %s:%d (odid: %d) was already removed!", key.getIpfixDevice(), key.getExporterSrcUdpPort(), key.getObservationDomainId()));
//                            }
//                        } //if - ak nie je platna sablona
//                    } // for - vsetky sablony daneho exportera
//                }// for - vsetky exportery
//            }
//        };
//        // nastavme nech sa to spusta kazdu minutu
//        scheduler.scheduleAtFixedRate(cleaner, 60, 60, TimeUnit.SECONDS);
//
//    }

    public final void setUpCleaningTask() {
        final Runnable cleaner = new Runnable() {

            public void run() {

                Logger log = Logger.getLogger("Cache cleaner");
//                log.debug("Cleaner thread started!");

                for (ExporterKey key : exporters.keySet()) {
                    TemplateHolder templates = exporters.get(key);
                    for (IPFIXTemplateRecord template : templates.getTemplates().values()) {
                        if (!template.isValid()) {
                            templates.remove(template.getTemplateID());

                            log.info(String.format("Template #%d, OD: %d from  %s:%d is no longer valid, removed from cache!",
                                    template.getTemplateID(), key.getObservationDomainId(), key.getIpfixDevice(), key.getExporterSrcUdpPort()));
                            if (templates.isEmpty()) {
                                exporters.remove(key);
                            }
//                             a navyse checkni vstupnu cache
//                            if (PacketCache.isExporterPresent(key) && templates.isEmpty()) { // ak exporter je pritomny, nesmie byt pre exporter ziadna sablona
//                                try {
//                                    // vymazeme cache ak je prazdna
//                                    if (PacketCache.removeCacheIfEmpty(key) == true) {
//                                        // vymazeme tiez vlakno pre tento exporter nech nezavadzia
//                                        UDPServer.removeProcessor(key);
//                                        log.info(String.format("Cache and thread for exporter %s:%d (odid: %d) is empty, removed from multicache!", key.getIpfixDevice(), key.getExporterSrcUdpPort(), key.getObservationDomainId()));
//                                    }
//                                } catch (JXCollException ex) {
//                                    // ked je cache vymazana medzi tym
//                                    log.info(ex.getMessage());
//                                }
//                            } // ak je pritomny dany exporter
//                            else {
//                                log.debug(String.format("Cache for exporter %s:%d (odid: %d) was already removed!", key.getIpfixDevice(), key.getExporterSrcUdpPort(), key.getObservationDomainId()));
//                            }
                        } //if - ak nie je platna sablona
                    } // for - vsetky sablony daneho exportera
                }// for - vsetky exportery
            }
        };
        // nastavme nech sa to spusta kazdu minutu
        scheduler.scheduleAtFixedRate(cleaner, 600, 600, TimeUnit.SECONDS);

    }

    /** Vytvara novu instanciu triedy */
    private IpfixUdpTemplateCache() {
        exporters = Collections.synchronizedMap(new HashMap<ExporterKey, TemplateHolder>());
        //odstartujeme cistiace vlakno
        setUpCleaningTask();
    }

    /**
     * Ziska instanciu tejto triedy.
     * @return 
     */
    public static IpfixUdpTemplateCache getInstance() {
        if (instance == null) {
            return new IpfixUdpTemplateCache();
        }
        return instance;
    }

    /**
     * Prida / nahradi zaznam sablony do cache podla exportera, ktory je identifikovany IP adresou a jeho identifikatorom
     *
     * @param template Objekt šablóny.
     * @param ipfixDevice IP adresa exportéra
     * @param exporterUdpSrcPort zdrojový UDP port exportéra
     * @param odid  identifikátor pozorovacej domény
     * @param timeReceived čas doručenia šablóny
     */
    public synchronized void addTemplate(IPFIXTemplateRecord template, InetAddress ipfixDevice, int exporterUdpSrcPort, long odid) {
        ExporterKey exporterKey = new ExporterKey(ipfixDevice, exporterUdpSrcPort, odid);
        TemplateHolder templates;
        // ak evidujeme exporter
        if (exporters.containsKey(exporterKey)) {
            templates = (TemplateHolder) exporters.get(exporterKey);
            if (templates.contains(template.getTemplateID())) {
                templates.addTemplate(template); // proste nahradime sucasnu verziu (UDP)
                // v pripade TCP a SCTP musime este odsledovat, ci nahodou neni priata ina sablona - v tom pripade hodit vynimku ze uz tam je
                // to preto, lebo tam je session, a povazuje sa to za utok, vtedy treba spojenie zrusit
            } else {
                templates.addTemplate(template); // ak tam este neni, tak ju tam supneme
            }

        } //neznamy exporter
        else {
            templates = new TemplateHolder();            // vytvorime hashmapu pre ten exporter - moze posielat viac sablon
            templates.addTemplate(template);             // vlozime tomuto exporteru prijatu sablonu
            exporters.put(exporterKey, templates);       // vlozime kluc tohto exportera do zoznamu exporterov

        }
    }

    /**
     * Do cache vlozi novu sablonu.
     * @param template Objekt sablony.
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param odid  identifikator pozorovacej domeny
     * @param timeReceived cas dorucenia sablony
     */
    public synchronized void addTemplate(IPFIXTemplateRecord template, InetSocketAddress ipfixDevice, long odid) {
        addTemplate(template, ipfixDevice.getAddress(), ipfixDevice.getPort(), odid);
    }

    /**
     * Vrati vsetky sablony v cache pre dany exporter
     *
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param exporterSrcUdpPort zdrojovy port exportera
     * @param odid Observation Domain ID
     * @return TemplateHolder objekt obsahujuci sablony v cache pre dany exporter
     */
    public synchronized TemplateHolder getTemplates(InetAddress exporterAddress, int udpSourcePort, long odid) {
        ExporterKey exporterKey = new ExporterKey(exporterAddress, udpSourcePort, odid);
        return (TemplateHolder) exporters.get(exporterKey);
    }

    /**
     * Zisti ci sa sablona identifikovana svojim ID nachadza v cache pre dany exporter. (UDP)
     * @param templateId identifikator sablony
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param odid Observation Domain ID
     * @return true, ak sa sablona nachadza v cache, false opacne
     */
    public synchronized boolean contains(int templateId, InetSocketAddress ipfixDevice, long odid) {
        ExporterKey exporterKey = new ExporterKey(ipfixDevice.getAddress(), ipfixDevice.getPort(), odid);
        if (exporters.containsKey(exporterKey)) {
            TemplateHolder templates = (TemplateHolder) exporters.get(exporterKey);
            return templates.contains(templateId);
        }
        return false;
    }

    /**
     * Zisti ci je dana sablona pritomna, ak je a sablona vyprsala, tak ju vymaze a vrati false. Pouzitelne len pre UDP.
     * @param templateId  identifikator sablony
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param odid Observation Domain ID
     * @return true, ak sa sablona nachadza v cache, false opacne. False aj vtedy ak sablona vyprsala.
     */
    public synchronized boolean checkForPresence(int templateId, InetSocketAddress ipfixDevice, long odid) {
        ExporterKey exporterKey = new ExporterKey(ipfixDevice.getAddress(), ipfixDevice.getPort(), odid);
        if (exporters.containsKey(exporterKey)) {
            TemplateHolder templates = (TemplateHolder) exporters.get(exporterKey);
            if (templates.contains(templateId)) {
                IPFIXTemplateRecord template = templates.get(templateId);

                if (template != null && template.isValid()) {
                    return true;
                } else {
                    //nemusime vymazat.. o vymazanie sa stara Cleaner thread
//                    templates.remove(templateId);
//                    if (templates.isEmpty()) {
//                        exporters.remove(exporterKey);
//                    }
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Vrati sablonu v cache podla jej ID a dany exporter (UDP)
     * @param templID identifikator sablony
     * @param ip IP adresa exportera
     * @param odid identifikator exportera
     * @return Zaznam sablony, null ak sa taka sablona v cache nenachadza
     */
    public synchronized IPFIXTemplateRecord getByID(int templID, InetSocketAddress ip, long odid) {
        ExporterKey exporterKey = new ExporterKey(ip.getAddress(), ip.getPort(), odid);
        TemplateHolder templates = (TemplateHolder) exporters.get(exporterKey);
        if (templates == null) {
            return null;
        }
        return (IPFIXTemplateRecord) templates.get(templID);
    }
//    /**
//     * Ziska vsetky kluc
//     * @param odid
//     * @return 
//     */
//    public List<TemplateHolder> getAllByOdid(long odid) {
//        Set<ExporterKey> allKeys = exporters.keySet();
//        
//        List<ExporterKey> selectedKeys = new ArrayList<>();
//        
//        for (ExporterKey exporterKey : allKeys) {
//            // vyberieme len kluce, ktore maju danu observation domain ID
//            if (exporterKey.getObservationDomainId() == odid) {
//                selectedKeys.add(exporterKey);
//            }
//        }
//        
//        List<TemplateHolder> templates = new ArrayList<>();
//        for (ExporterKey exporterKey : selectedKeys) {
//            templates.
//        }
//    }
}
