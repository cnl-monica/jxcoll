/* 
 * Copyright (C) 2012  Tomas Verescak
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
import java.util.Objects;

/**
 *
 * @author Tomas Verescak
 */
public class ExporterKey {

    //17.3.2012: podla [RFC5101], <IPFIX Device, Exporter source UDP port, Observation Domain ID, Template ID, Template Definition, Last Received>
    private InetAddress ipfixDevice;            // ip adresa exportera
    private int exporterSrcUdpPort;             // zdrojovy port exportera
    private long observationDomainId;           // cislo pozorovacej domeny


    /**
     * TemplateInfo constructor. Uses exporter IP, src UDP port and observation domain ID as a key
     * @param ipfixDevice IP address of device exporter is running on
     * @param exporterSrcUdpPort UDP source port Template was received from
     * @param templateId ID of template 
     * @param observationDomainId Exporter observation domain ID (area where exporter operates)
     */
    public ExporterKey(InetAddress ipfixDevice, int exporterSrcUdpPort, long observationDomainId) {
        this.ipfixDevice = ipfixDevice;
        this.exporterSrcUdpPort = exporterSrcUdpPort;
        this.observationDomainId = observationDomainId;
    }

    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExporterKey other = (ExporterKey) o;
        if (!this.ipfixDevice.equals(other.getIpfixDevice())) {
            return false;
        }
        if (this.exporterSrcUdpPort != other.getExporterSrcUdpPort()) {
            return false;
        }
        if (this.observationDomainId != other.getObservationDomainId()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.ipfixDevice);
        hash = 83 * hash + this.exporterSrcUdpPort;
        hash = 83 * hash + (int) (this.observationDomainId ^ (this.observationDomainId >>> 32));
        return hash;
    }

    public InetAddress getIpfixDevice() {
        return ipfixDevice;
    }

    public int getExporterSrcUdpPort() {
        return exporterSrcUdpPort;
    }

    public long getObservationDomainId() {
        return observationDomainId;
    }

    @Override
    public String toString() {
        return String.format("Device: %s:%d, OD: %d", ipfixDevice, exporterSrcUdpPort, observationDomainId);
    }
    
    
}