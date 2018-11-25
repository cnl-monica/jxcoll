/* 
 * Copyright (C) 2012 Lubos Kosco, Michal Kascak, Tomas Verescak
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

import sk.tuke.cnl.bm.TemplateException;
import java.util.HashMap;

/**
 * Cache na ukladanie sablon IPFIX spravy pre pouzite s protokolmi vytvarajucimi spojenie.
 * @author Michal Kascak, Tomas Verescak
 */
public class IpfixSingleSessionTemplateCache {

    /** Vytvara novu instanciu triedy */

    private HashMap<Long, TemplateHolder> observationDomains = new HashMap<>();

    /**
     * Prida zaznam sablony do cache podla exportera, ktory je identifikovany IP adresou a jeho identifikatorom
     * @param template Zaznam sablony
     * @param odid observation domain id
     * @param timeReceived Cas prijatia sablony
     * @throws IPFIXTemplateException Je vyhodena, ak je pokus o pridanie uz existujucej sablony.
     */
    public void addTemplate(IPFIXTemplateRecord template, long odid) throws TemplateException {
        TemplateHolder templates;
        // ak evidujeme pozorovaciu domenu
        if (observationDomains.containsKey(odid)) {
            templates = (TemplateHolder) observationDomains.get(odid);
            if (templates.contains(template.getTemplateID())) {

                throw new TemplateException("Template" + template.getTemplateID() + " already in cache!");
                // vyhodit vynimku, aby sa zavrela asociacia
                // v pripade TCP a SCTP ak prijmeme taku istu sablonu pred vymazanim.. povazuje sa to za utok
//                templates.addTemplate(template); // proste nahradime sucasnu verziu (UDP)
                // v pripade TCP a SCTP musime este odsledovat, ci nahodou neni priata ina sablona - v tom pripade hodit vynimku ze uz tam je
                // to preto, lebo tam je session, a povazuje sa to za utok, vtedy treba spojenie zrusit
            } else {
                templates.addTemplate(template); // ak tam este neni, tak ju tam supneme
            }

        } //nova pozorovacia domena
        else {
            templates = new TemplateHolder();            // vytvorime hashmapu pre pozorovaciu domenu - moze posielat viac sablon
            templates.addTemplate(template);             // vlozime tomuto exporteru prijatu sablonu
            observationDomains.put(odid, templates);     // vlozime kluc tejto pozorovacej domeny do zoznamu exporterov
        }
    }

    /**
     * Vrati vsetky sablony v cache pre dany exporter
     *
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param exporterSrcUdpPort zdrojovy port exportera
     * @param odid Observation Domain ID
     * @return TemplateHolder objekt obsahujuci sablony v cache pre dany exporter
     */
    public TemplateHolder getTemplates(long odid) {
        return (TemplateHolder) observationDomains.get(odid);
    }

    /**
     * Zisti ci sa sablona identifikovana svojim ID nachadza v cache pre dany exporter. (UDP)
     * @param templateId identifikator sablony
     * @param ipfixDevice IP adresa a zdrojovy port exportera
     * @param odid Observation Domain ID
     * @return true, ak sa sablona nachadza v cache, false opacne
     */
    public boolean contains(int templateId, long odid) {
        if (observationDomains.containsKey(odid)) {
            TemplateHolder templates = (TemplateHolder) observationDomains.get(odid);
            return templates.contains(templateId);
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
    public IPFIXTemplateRecord get(int templID, long odid) {
        TemplateHolder templates = (TemplateHolder) observationDomains.get(odid);
        if (templates == null) {
            return null;
        }
        return (IPFIXTemplateRecord) templates.get(templID);
    }

    /**
     * Zmaze vsetky sablony z danej pozorovacej domeny. Tato metoda by mala
     * byt zavolana v pripade prijatia All Data Template Records Withdrawal Message.
     * @param odid Cislo pozorovacej domeny
     * @return 
     */
    public void removeAll(long odid) {
        TemplateHolder templates = observationDomains.get(odid);

        HashMap<Integer, IPFIXTemplateRecord> map = templates.getTemplates();
        for (IPFIXTemplateRecord template : map.values()) {
            templates.remove(template.getTemplateID());
        }
        // mozno staci len toto
        observationDomains.remove(odid);
    }

    /**
     * Zmaze sablonu danu cislom sablony a cislom pozorovacej domeny do ktorej patri.
     * @param templID Cislo sablony
     * @param odid Cislo pozorovacej domeny
     */
    public void remove(int templID, long odid) throws TemplateException {
        if (contains(templID, odid)) {
            TemplateHolder templates = observationDomains.get(odid);
            templates.remove(templID);
        } else {
            throw new TemplateException("Attempt to withdraw Options Template #" + templID + ", OD: " + odid + ", which does not exist in cache!");
        }
    }
}
