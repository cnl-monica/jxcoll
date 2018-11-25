/* Copyright (C) 2013 MONICA Research Group / TUKE 
 * 2010 Lubos Kosco, Michal Kascak,  Adrian Pekar, Tomas Verescak, Matúš Husovský
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
 *
 */ 
package sk.tuke.cnl.bm.JXColl;

import java.io.IOException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Class responsible for loading and holding the configuration file attributes.
 * The class also contains default values for the praticular attribues.
 */
public abstract class Config {

    private static Logger log = Logger.getLogger(Config.class.getName());
    public static int DEFAULT_TCP_CONNECTION_TIMEOUT = 10;
    private static final String DEFAULT_confFile = "/etc/jxcoll/jxcoll_config.xml";
    private static final String DEFAULT_XMLFile = "/etc/jxcoll/ipfixFields.xml";
    private static int DEFAULT_IPFIX_TEMPLATE_TIMEOUT = 300;
    private static int DEFAULT_TEMPLATE_ID_OWD_START_OBSERVATION_POINT = 256;
    private static int DEFAULT_OWD_START_OBSERVATION_DOMAIN_ID = 0;
    private static int DEFAULT_TEMPLATE_ID_OWD_END_OBSERVATION_POINT = 257;
    private static int DEFAULT_OWD_END_OBSERVATION_DOMAIN_ID = 0;
    private static int DEFAULT_PASSIVE_TIMEOUT = 5000;
    private static int DEFAULT_ACTIVE_TIMEOUT = 10000;
    private static int DEFAULT_LPORT = 4739;
    private static int DEFAULT_ACPPORT = 2138;
    private static int DB_DEFAULT_PORT = 27017;
    private static int DEFAULT_ACC_REC_EXPORT_INTERVAL = 60;
    private static int DEFAULT_COLLECTOR_ID = 1;
    private static int DEFAULT_SYNC_PORT = 5544;
    public static String logl;
    public static String XMLFile;
    public static Integer IPFIX_TEMPLATE_TIMEOUT;    
    public static Integer lport;
    public static Integer lsyncport;
    public static String lprotocol;
    public static Integer maxConnections;
    public static int DEFAULT_MAX_CONNECTIONS = 10;
    public static boolean makeSync = false;
    public static boolean measureOwd = false;
    public static Long owdStartObsPoID;
    public static String owdStartHost;
    public static Integer owdStartObsDomID;
    public static Integer owdStartTempIDObsPoint;
    public static Long owdEndObsPoID;
    public static String owdEndHost;
    public static Integer owdEndObsDomID;
    public static Integer owdEndTempIDObsPoint;
    public static Integer owdPassiveTimeout;
    public static Integer owdActiveTimeout;
    public static boolean doACPTransfer = false;
    public static Integer acpport;
    public static String acplogin;
    public static String acppassword;
    public static boolean doPGexport = true;
    public static String dbHost;
    public static String dbPort;
    public static String dbName;
    public static String dbLogin;
    public static String dbPassword;
    public static boolean doPGAccExport = false;
    public static int accRecExportInterval;
    public static int collectorID;
    public static boolean receiveUDP = false;
    public static boolean receiveTCP = false;
    public static boolean receiveSCTP = false;

    /**
     * Reads values from xml file, if filename is empty falls back to default /etc/jxcoll/jxcoll_config.xml
     *
     * @param confFile String filename and path to the configuration xml file
     */
    public static void loadData(String confFile) {

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;

        if (confFile == null) {
            log.warn("No configuration file was given.");
            log.info("Loading configuration file from it's default location: " + DEFAULT_confFile);
            confFile = DEFAULT_confFile;
        }

        try {
            //get the factory
            dbf = DocumentBuilderFactory.newInstance();
            //Using factory get an instance of document builder
            db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            doc = db.parse(confFile);
            //get attributes form configuration xml file
            FillData(doc);
        } catch (XPathExpressionException ex) {
            java.util.logging.Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            log.fatal("Could not load configuration file:  " + confFile + "  !\n" + ex);
            System.exit(1);
        } catch (IOException ex) {
            log.fatal("Could not load configuration file:  " + confFile + "  !\n" + ex);
            System.exit(1);
        } catch (ParserConfigurationException ex) {
            java.util.logging.Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//Config

//        /**
//         * Default config, loads ./JXColl.conf if no file is given
//         *
//         */
//    public Config() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
//        this("");
//    }
    /**
     * Parses the xml configuration file and reads the particular attributes out of it
     *
     * @param doc Document DOM representation of the configuration xml file
     */
    private static void FillData(Document doc) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //creating an XPathFactory
        XPathFactory factory = XPathFactory.newInstance();
        //using this factory to create an XPath object:
        XPath xpath = factory.newXPath();
        XPathExpression expr;

//GLOBAL
        //LOGLEVEL
        //compiling the XPath expression
        expr = xpath.compile("//global/logLevel");
        //evaluating the XPath expression to get the result
        logl = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toUpperCase();
        if (logl == null || !logl.matches("ALL|DEBUG|INFO|WARN|TRACE|ERROR|FATAL|OFF")) {
            log.warn("Could not recognize value for log level (falling back to default DEBUG) !");
            logl = "ALL";
        }

        //ipfixFields.xml name and path
        expr = xpath.compile("//global/ipfixFieldsXML");
        XMLFile = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim();
        if (XMLFile == null || XMLFile.equals("")) {
            log.warn("Config file does not contain path to XML file (falling back to default \"" + DEFAULT_XMLFile + "\")");
            XMLFile = DEFAULT_XMLFile;
        }

        //IPFIX Template Timeout
        expr = xpath.compile("//global/ipfixTemplateTimeout");
        try {
            IPFIX_TEMPLATE_TIMEOUT = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for IPFIX template timeout (falling back to default 5 min) : " + nfe);
            IPFIX_TEMPLATE_TIMEOUT = new Integer(DEFAULT_IPFIX_TEMPLATE_TIMEOUT);
        }
        
        //LISTENING PORT
        expr = xpath.compile("//global/listenPort");
        try { 
            lport = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
            if (lport.intValue() < 0 || lport.intValue() > 65535) {
                log.warn("Value for Listener port is out of bounds (falling back to default 4739)");
                lport = new Integer(DEFAULT_LPORT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for Listener port (falling back to default 4739) : " + nfe);
            lport = new Integer(DEFAULT_LPORT);
        }

//        //LISTENING PROTOCOL
//        expr = xpath.compile("//global/listenProtocol");
//        lprotocol = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toUpperCase();
//        if (!lprotocol.matches("TCP|UDP|SCTP")) {
//            log.warn("Listener protocol value not recognize (falling back to default UDP)");
//            lprotocol = "UDP";
//        }

        //LISTENING ON UDP
        expr = xpath.compile("//global/receiveUDP");
        String tmpUDPsupport = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toUpperCase();
        if (!tmpUDPsupport.matches("YES|NO")) {
            log.warn("UDP support could not be determined from config file (UDP is not used, valid options are YES or NO)");
            tmpUDPsupport = "no";
        }
        receiveUDP = (tmpUDPsupport.equalsIgnoreCase("yes")) ? true : false;
        
        //LISTENING ON TCP
        expr = xpath.compile("//global/receiveTCP");
        String tmpTCPsupport = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toUpperCase();
        if (!tmpTCPsupport.matches("YES|NO")) {
            log.warn("TCP support could not be determined from config file (TCP is not used, valid options are YES or NO)");
            tmpTCPsupport = "no";
        }
        receiveTCP = (tmpTCPsupport.equalsIgnoreCase("yes")) ? true : false;
        
        //@TODO: maxConnections separately on TCP and SCTP
        
        //LISTENING ON SCTP
        expr = xpath.compile("//global/receiveSCTP");
        String tmpSCTPsupport = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toUpperCase();
        if (!tmpSCTPsupport.matches("YES|NO")) {
            log.warn("SCTP support could not be determined from config file (SCTP is not used, valid options are YES or NO)");
            tmpSCTPsupport = "no";
        }
        receiveSCTP = (tmpSCTPsupport.equalsIgnoreCase("yes")) ? true : false;

        //MAXIMUM CONNECTIONS
        expr = xpath.compile("//global/maxConnections");
        try {
            maxConnections = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
            if (maxConnections.intValue() < 0 || maxConnections.intValue() > 10) {
                log.warn("Value for maxConnection is out of bounds (falling back to default 10)");
                maxConnections = new Integer(DEFAULT_MAX_CONNECTIONS);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for Listener port (falling back to default 4739) : " + nfe);
            maxConnections = new Integer(DEFAULT_MAX_CONNECTIONS);
        }
//GLOBAL

//SYNC
        //Make Synchronization
        expr = xpath.compile("//sync/makeSync");
        String tmpMakeSync = (expr.evaluate(doc, XPathConstants.STRING)).toString();
        if (!(tmpMakeSync.equals("yes") || tmpMakeSync.equals("no"))) {
            log.warn("Could not parse value for Synchronziation status (falling back to default no)");
            tmpMakeSync = "no";
        }
        makeSync = (tmpMakeSync.equals("yes")) ? true : false;


        //LISTENING PORT FOR SYNCHRONIZATION PACKETS
        expr = xpath.compile("//sync/listenSynchPort");
        try {
            lsyncport = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
            if (lsyncport.intValue() < 0 || lsyncport.intValue() > 65535) {
                log.warn("Value for Synchronization Listener port is out of bounds (falling back to default 5544)");
                lsyncport = new Integer(DEFAULT_SYNC_PORT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for Synchronization Listener port (falling back to default 5544) : " + nfe);
            lsyncport = new Integer(DEFAULT_SYNC_PORT);
        }
//SYNC

//OWD
        //MEASURE OWD
        expr = xpath.compile("//owd/measureOwd");
        String tmpMeasureOWD = (expr.evaluate(doc, XPathConstants.STRING)).toString();
        if (!(tmpMeasureOWD.equals("yes") || tmpMeasureOWD.equals("no"))) {
            log.warn("Could not parse value for OWD measurement status (falling back to default no)");
            tmpMeasureOWD = "no";
        }
        if (tmpMeasureOWD.equals("yes")) {
            measureOwd = true;
        } else {
            measureOwd = false;
        }

        //OWDSTART       
        //OWDSTARTOBSERVATIONPOINTTEMPLATEID
        expr = xpath.compile("//owd/owdStart/owdStart_ObservationPointTemplateID");
        try {
            owdStartTempIDObsPoint = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
            if (owdStartTempIDObsPoint.intValue() < 0 || owdStartTempIDObsPoint.intValue() > 65535) {
                log.warn("Value for Template ID of OWD Start ObservationPointID is out of bounds (falling back to default 256)");
                owdStartTempIDObsPoint = new Integer(DEFAULT_TEMPLATE_ID_OWD_START_OBSERVATION_POINT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value  Template ID of OWD Start ObservationPointID (falling back to default 256) : " + nfe);
            owdStartTempIDObsPoint = new Integer(DEFAULT_TEMPLATE_ID_OWD_START_OBSERVATION_POINT);
        }

        //OWDSTARTOBSERVATIONDOMAINID
        expr = xpath.compile("//owd/owdStart/owdStart_ObservationDomainID");
        try {
            owdStartObsDomID = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
            if (owdStartObsDomID.intValue() < 0 || owdStartObsDomID.intValue() > 65535) {
                log.warn("Value for OWD Start ObservationPointID is out of bounds (falling back to default 0)");
                owdStartObsDomID = new Integer(DEFAULT_OWD_START_OBSERVATION_DOMAIN_ID);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for OWD Start ObservationPointID (falling back to default 0) : " + nfe);
            owdStartObsDomID = new Integer(DEFAULT_OWD_START_OBSERVATION_DOMAIN_ID);
        }

        //OWDSTARTHOST
        expr = xpath.compile("//owd/owdStart/owdStart_Host");
        owdStartHost = (expr.evaluate(doc, XPathConstants.STRING)).toString();
        if (!(validateIp(owdStartHost) || validateHost(owdStartHost))) {
            log.warn("Wrong IP address or host name (falling back to default 127.0.0.1)");
            owdStartHost = "127.0.0.1";
        }

        //OWDSTARTOBSERVATIONPOINTID
        expr = xpath.compile("//owd/owdStart/owdStart_ObservationPointID");
        String tmpOwdStartObsPoID = expr.evaluate(doc, XPathConstants.STRING).toString();
        if (Long.parseLong(tmpOwdStartObsPoID) < 0 || Long.parseLong(tmpOwdStartObsPoID) > 4294967295L) {
            log.warn("Value for OWD START ObservationPointID is out of bounds! (turning off OWD measurement)");
            measureOwd = false;
        } else {
            owdStartObsPoID = Long.parseLong(tmpOwdStartObsPoID);
        }
        //OWDSTART

        //OWDEND
        //OWDENDOBSERVATIONPOINTTEMPLATEID
        expr = xpath.compile("//owd/owdEnd/owdEnd_ObservationPointTemplateID");
        try {
            owdEndTempIDObsPoint = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
            if (owdEndTempIDObsPoint.intValue() < 0 || owdEndTempIDObsPoint.intValue() > 65535) {
                log.warn("Value for Template ID of OWD End ObservationPointID is out of bounds (falling back to default 257)");
                owdEndTempIDObsPoint = new Integer(DEFAULT_TEMPLATE_ID_OWD_END_OBSERVATION_POINT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value  Template ID of OWD End ObservationPointID (falling back to default 257) : " + nfe);
            owdEndTempIDObsPoint = new Integer(DEFAULT_TEMPLATE_ID_OWD_END_OBSERVATION_POINT);
        }

        //OWDENDOBSERVATIONDOMAINID
        expr = xpath.compile("//owd/owdEnd/owdEnd_ObservationDomainID");
        try {
            owdEndObsDomID = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
            if (owdEndObsDomID.intValue() < 0 || owdEndObsDomID.intValue() > 65535) {
                log.warn("Value for OWD Start ObservationPointID is out of bounds (falling back to default 0)");
                owdEndObsDomID = new Integer(DEFAULT_OWD_END_OBSERVATION_DOMAIN_ID);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for OWD Start ObservationPointID (falling back to default 0) : " + nfe);
            owdEndObsDomID = new Integer(DEFAULT_OWD_END_OBSERVATION_DOMAIN_ID);
        }

        //OWDENDHOST
        expr = xpath.compile("//owd/owdEnd/owdEnd_Host");
        owdEndHost = (expr.evaluate(doc, XPathConstants.STRING)).toString();
        if (!(validateIp(owdEndHost) || validateHost(owdEndHost))) {
            log.warn("Wrong IP address or host name (falling back to default 127.0.0.1)");
            owdEndHost = "127.0.0.1";
        }

        //OWDENDOBSERVATIONPOINTID
        expr = xpath.compile("//owd/owdEnd/owdEnd_ObservationPointID");
        String tmpOwdEndObsPoID = expr.evaluate(doc, XPathConstants.STRING).toString();
        if (Long.parseLong(tmpOwdEndObsPoID) < 0 || Long.parseLong(tmpOwdEndObsPoID) > 4294967295L) {
            log.warn("Value for OWD END ObservationPointID is out of bounds! (turning off OWD measurement)");
            measureOwd = false;
        } else {
            owdEndObsPoID = Long.parseLong(tmpOwdEndObsPoID);
        }
        //OWDEND

        //OWD PASSIVE TIMEOUT
        expr = xpath.compile("//owd/passiveTimeout");
        try {
            owdPassiveTimeout = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for passiveTimeout (falling back to default 5 seconds) : " + nfe);
            owdPassiveTimeout = new Integer(DEFAULT_PASSIVE_TIMEOUT);
        }

        //OWD ACTIVE TIMEOUT
        expr = xpath.compile("//owd/activeTimeout");
        try {
            owdActiveTimeout = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for activeTimeout (falling back to default 10 seconds) : " + nfe);
            owdActiveTimeout = new Integer(DEFAULT_ACTIVE_TIMEOUT);
        }
//OWD

//ACP
        //ACP Transfer State
        expr = xpath.compile("//acp/acpTransfer");
        String tmpDoACPTransfer = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toLowerCase();
        if (!tmpDoACPTransfer.matches("yes|no")) {
            log.warn("Could not parse value for ACP transfer (falling back to default no)");
            tmpDoACPTransfer = "no";
        }

        doACPTransfer = (tmpDoACPTransfer.equals("yes")) ? true : false;

        //ACP Port
        expr = xpath.compile("//acp/acpPort");
        try {
            acpport = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
            if (acpport.intValue() < 0 || acpport.intValue() > 65535) {
                log.warn("Value for ACP port is out of bounds (falling back to default 2138)");
                acpport = new Integer(DEFAULT_ACPPORT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for ACP port (falling back to default 2138) : " + nfe);
            acpport = new Integer(DEFAULT_ACPPORT);
        }

        //ACP Login
        expr = xpath.compile("//acp/acpLogin");
        acplogin = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim();
        try {
            acplogin = getMd5Digest(acplogin);
        } catch (NoSuchAlgorithmException nsae) {
            log.error("Error while encrypting ACP login: " + nsae);
        }

        //ACP Password
        expr = xpath.compile("//acp/acpPassword");
        acppassword = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim();
        try {
            acppassword = getMd5Digest(acppassword);
        } catch (NoSuchAlgorithmException nsae) {
            log.error("Error while encrypting ACP password: " + nsae);
        }
//ACP

// DATABASE
        //DB Export State
        expr = xpath.compile("//database/dbExport");
        String tmpDoPgExp = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toLowerCase();
        if (!tmpDoPgExp.matches("yes|no")) {
            log.warn("Could not parse value for database export (falling back to default yes)");
            tmpDoPgExp = "yes";
        }

        doPGexport = (tmpDoPgExp.equals("yes")) ? true : false;

        //DB Host
        expr = xpath.compile("//database/dbHost");
        dbHost = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim();
        if (!(validateIp(dbHost) || validateHost(dbHost))) {
            log.warn("Wrong IP address or host name (falling back to default 127.0.0.1)");
            dbHost = "127.0.0.1";
        }

        //DB Port
        expr = xpath.compile("//database/dbPort");
        int tmppgdbPort;
        try {
            tmppgdbPort = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString().trim());
            if (tmppgdbPort < 0 || tmppgdbPort > 65535) {
                log.warn("Value for database port is out of bounds (falling back to default 27017)");
                tmppgdbPort = new Integer(DB_DEFAULT_PORT);
            }
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for database port (falling back to default 27017) : " + nfe);
            tmppgdbPort = new Integer(DB_DEFAULT_PORT);
        }
        dbPort = Integer.toString(tmppgdbPort);

        //DB Name
        expr = xpath.compile("//database/dbName");
        dbName = (expr.evaluate(doc, XPathConstants.STRING)).toString();

        //DB Login
        expr = xpath.compile("//database/dbLogin");
        dbLogin = (expr.evaluate(doc, XPathConstants.STRING)).toString();

        //DB Password
        expr = xpath.compile("//database/dbPassword");
        dbPassword = (expr.evaluate(doc, XPathConstants.STRING)).toString();
//DATABASE

//ACCOUNTING
        //ACCOUNTING Export State
        expr = xpath.compile("//accounting/accExport");
        String tmpAccExp = (expr.evaluate(doc, XPathConstants.STRING)).toString().trim().toLowerCase();
        if (!tmpAccExp.matches("yes|no")) {
            log.warn("Could not parse value for Accounting database export (falling back to default no)");
            tmpAccExp = "no";
        }

        doPGAccExport = (tmpAccExp.equals("yes")) ? true : false;

        //ACCOUNTING Record Export Interval
        expr = xpath.compile("//accounting/accRecordExportInterval");
        try {
            accRecExportInterval = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for Accounting Record Export Interval (falling back to default 60 s) : " + nfe);
            accRecExportInterval = new Integer(DEFAULT_ACC_REC_EXPORT_INTERVAL);
        }

        //ACCOUNTING collectorID
        expr = xpath.compile("//accounting/collectorID");
        try {
            collectorID = new Integer(expr.evaluate(doc, XPathConstants.STRING).toString());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse value for Database Collector ID (falling back to default 1) : " + nfe);
            collectorID = new Integer(DEFAULT_COLLECTOR_ID);
        }
//ACCOUNTING
    }//FillData

    /**
     * Method for getting the hash representation of the given login information
     * @param input input string, that has to be encrypted.
     * @return output string in MD5 prepresentation.
     * @throws java.security.NoSuchAlgorithmException indicates that a requested algorithm could not be found
     */
    public static String getMd5Digest(String input) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }//try-catch
    }//getMd5Digest

    /**
     * Method for validating IP addresses
     * @param input input string, that has to be checked.
     * @return output true/false.
     */
    public static boolean validateIp(String ipAddress) {
//        there could be a better solution
//        http://www.tek-tips.com/viewthread.cfm?qid=1379040&page=5
        final Pattern IP_PATTERN = Pattern.compile(""
                + "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        //final Pattern IP_PATTERN = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
        return IP_PATTERN.matcher(ipAddress).matches();
    }
    //Variables for host name validation
    static String oneAlpha = "(.)*((\\p{Alpha})|[-])(.)*";
    static String domainIdentifier = "((\\p{Alnum})([-]|(\\p{Alnum}))*(\\p{Alnum}))|(\\p{Alnum})";
    static final String domainNameRule = "(" + domainIdentifier + ")((\\.)(" + domainIdentifier + "))*";

    /**
     * Method for validating host name
     * @param input input string, that has to be checked.
     * @return output true/false.
     */
    public static boolean validateHost(String host) {
        if ((host == null) || (host.length() > 63)) {
            return false;
        }
        return host.matches(domainNameRule) && host.matches(oneAlpha);
    }
}
