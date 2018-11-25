/*  Copyright (C) 2013 MONICA Research Group / TUKE 
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
 *  Subor: ACPIPFIXWorker.java
 */
package sk.tuke.cnl.bm.JXColl.export;

import java.io.*;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.log4j.Logger;
//import sk.tuke.cnl.bm.ACPIPFIXTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.IJXConstants;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
import sk.tuke.cnl.bm.JXColl.IpfixElements;
import sk.tuke.cnl.bm.JXColl.RecordDispatcher;
import sk.tuke.cnl.bm.JXColl.Support;
import sk.tuke.cnl.bm.SimpleFilter;

/**
 * Trieda, ktorá umožňuje filtrovanie údajov, ich nasledné posielanie protokolom ACP, a komunikaciu Analyzerom.
 * @author Adrián Pekár
 */
public class ACPIPFIXWorker extends Thread {

    private static Logger log = Logger.getLogger(ACPIPFIXWorker.class.getName());
    private static List<Socket> pool = new LinkedList<Socket>();
    //FIFO pre údaje, ktoré budú poslané cez ACP
    private ArrayBlockingQueue<String> outCache = new ArrayBlockingQueue<String>(IJXConstants.THREAD_CACHE_SIZE);
//    Thread parser;
    private int fFilter = 0;
    private SimpleFilter sFilter;
    private boolean alive = true;
    private boolean active = false;
    private boolean isPaused = false;
    private boolean templateAccepted = false;
    private boolean filterAccepted = false;
    //private boolean transferTypeAccepted = false;
    //private int transferType;
    private String sendData;
    private int[] analyzerTemplate = null;
    /** XML dokument opisujúci všetky informačné elementy IPFIX */
//    private Document ieXml = null;
//    private Element rootElement = null;
    private boolean isOutcacheFilled = false;
    private OutputStream raw;
    private InputStream inByte;
    private DataOutputStream outData;
    private DataInputStream inpData;
    private ObjectInputStream inFilter;
    private Writer out;
    private Reader in;
    private BufferedReader inString;
    private IpfixElements elementsInfo = IpfixElements.getInstance();
    private ObjectOutputStream outObject; 
    /**
     * Null konštruktor pre tento Thread
     */
    public ACPIPFIXWorker() {
        this(null, 0);
    }

    /**
     * Konštruktor, nastaví meno a id pre tento worker.
     *
     * @param group ThreadGroup ku ktorému worker patrí
     * @param i int id no.
     */
    public ACPIPFIXWorker(ThreadGroup group, int i) {
        super(group, null, "ACP Thread " + i);
    }

    /**
     * Metóda pre čisté zastavenie Threadu
     */
    public void die() {
        this.alive = false;
//        this.parser.interrupt();
        this.interrupt();
        this.stop();
    }

    /**
     * Spracuje spojenie z pool
     * @param request Socket na ktorom prišlo spojenie
     */
    public static void processRequest(Socket request) {
        synchronized (pool) {
            pool.add(pool.size(), request);
            pool.notifyAll();
        }
    }

    /**
     * Metóda, ktorá slúži na overenie autentifikovaného spojenia.
     * @param connection spojenie, cez ktoré sa očakávajú autentifikačné údaje
     * @param login prijatý login
     * @param passwd prijaté heslo
     * @return true - ak sa došlo k správnej autentifikácie, false - ak analyzer poslal nesprávne autentifikačné údaje.
     * @throws java.io.IOException
     */
    public boolean ACPauthentication(Socket connection, String login, String passwd) throws IOException {
        boolean variable = false;
        if ((Config.acppassword.equals(passwd)) && (Config.acplogin.equals(login))) {
            log.warn("Auth OK. DC granted to: " + connection.getInetAddress().getHostName());
            outData.writeInt(1);
            outData.flush();
            variable = true;
            return variable;
        } else {
            log.warn("Auth Failed !!! Tried from: " + connection.getInetAddress().getHostName());
            outData.writeInt(0);
            outData.flush();
            connection.close();
            return variable;
        }

    }//ACPauthentication()

    /**
     * Metóda, ktorá slúži na prijímanie a následné reagovanie správ od analyzera.
     * @throws java.io.IOException
     */
    public void ACPCommunication(int messageCode) throws IOException {
        int analyzerTemplSize = 0;
        log.debug("ACP: Incoming MSG type: " + messageCode);
        switch (messageCode) {
            case 0:
                // precitame prve cislo, je to velkost sablony - pocet elementov
                analyzerTemplSize = inpData.readInt();

                //sablona prijata z analyzera
                analyzerTemplate = new int[analyzerTemplSize];
                for (int index = 0; index < analyzerTemplSize; index++) {
                    // precitame ID jednotlivych elementov ktore analyzer potrebuje
                    analyzerTemplate[index] = inpData.readInt();
                    log.debug("Template element (ID): " + analyzerTemplate[index]);
                }


                int templateElementsSupported = 0;
                for (int i = 0; i < analyzerTemplate.length; i++) {
               
              if(elementsInfo.exists(analyzerTemplate[i], 26235L)){
                 if(elementsInfo.isBeemSupported(analyzerTemplate[i], 26235L)){
                    templateElementsSupported++;}
              } else if (elementsInfo.isBeemSupported(analyzerTemplate[i], 0L)){
                        templateElementsSupported++;
                    }
                }

                // ak kolektor podporuje vsetky elementy, ktore analyzer potrebuje
                if (analyzerTemplSize == templateElementsSupported) {
                    log.debug("Template accepted!");
                    templateAccepted = true;
                    outData.writeInt(1);
                    outData.writeInt(1);
                    outData.flush();
                } else {
                    log.debug("Template rejected!");
                    outData.writeInt(1);
                    outData.writeInt(0);
                    outData.flush();
                }
                
                break;

            case 1:

                boolean accept = true;
                try {
                    Object tempFilter = inFilter.readObject();
                    if (tempFilter instanceof SimpleFilter) {
                        sFilter = (SimpleFilter) tempFilter;
                    } else {
                        accept = false;
                    }
                } catch (ClassNotFoundException ex2) {
                    log.error(ex2);
                    accept = false;
                }


                log.debug("FilterFlag came: " + sFilter.getFlag() + " In text(): " + sFilter.toString());
                if (accept) {
                    filterAccepted = true;
                    this.fFilter = sFilter.getFlag();
                    log.debug("I've set the filter: " + this.fFilter);
                    outData.writeInt(1);
                    outData.writeInt(11);
                    outData.flush();
                } else {
                    log.warn("The filter that came is bad");
                    outData.writeInt(1);
                    outData.writeInt(10);
                    outData.flush();
                }
                break;
            case 2:
                if (templateAccepted == true) {
                    isPaused = true;
                    log.debug("Data transfer paused.");
                    outData.writeInt(1);
                    outData.writeInt(21);
                    outData.flush();
                    while (!outCache.isEmpty()) {
                        log.debug("Emptying outcache after pause accept.");
                        try {
                            outCache.take();
                        } catch (InterruptedException ex) {
                            log.debug(ex.getMessage());
                        }
                    }
                    isOutcacheFilled = false;
                } else {
                    log.debug("Data transfer pause request rejected.");//este sa nezacal prenos udajov
                    outData.writeInt(1);
                    outData.writeInt(20);
                    outData.flush();
                }
                break;
            case 3:
                if (isPaused == false) {
                    log.debug("Data transfer unpause request rejected.");//este sa nezacal prenos udajov
                    outData.writeInt(1);
                    outData.writeInt(30);
                    outData.flush();
                } else {
                    //transferState = true;
                    log.debug("Data transfer unpaused.");
                    outData.writeInt(1);
                    outData.writeInt(31);
                    outData.flush();
                    isPaused = false;
                }

                break;
            default:
                log.error("Unknown message type :" + messageCode);
        }
    }//ACPCommunication()

    /**
     * Metóda, ktorá umožňuje fizykú komunikáciu protokolom ACP.
     * @param connection spojenie medzi kolektorom a príslušným analyzerom.
     * @throws java.io.IOException
     */
    private void makeconnection(Socket connection) throws IOException {
        raw = new BufferedOutputStream(connection.getOutputStream());
        outData = new DataOutputStream(raw);
        out = new OutputStreamWriter(raw);
        inByte = connection.getInputStream();
        in = new InputStreamReader(new BufferedInputStream(inByte), "ASCII");
        inpData = new DataInputStream(inByte);
        inString = new BufferedReader(new InputStreamReader(inByte));
        inFilter = new ObjectInputStream(inByte);
        outObject = new ObjectOutputStream(connection.getOutputStream());
    }//makeconnection()

    /**
     * Metóda, v ktorej prebieha príprava exportovaných údajov podľa prijatej šablóny
     * @param ipmb IP adresa meracieho bodu
     * @param template  IPFIX šablóna
     * @param data IPFIX údaje
     */
    private void ACPExport(Inet4Address ipmb, IPFIXTemplateRecord template, IPFIXDataRecord data) {

        if (elementsInfo == null) {
            elementsInfo = IpfixElements.getInstance();
        }

        JSONObject object= new JSONObject();
        JSONArray array= new JSONArray();
        Hashtable IPFIXData = RecordDispatcher.getInstance().getData();
        if (filterAccepted) {
            if (ACPFilter(ipmb, template, data)) {//kontrola filtra
              
                for (int i = 0; i < analyzerTemplate.length; i++) {
                    array.put(IPFIXData.get(analyzerTemplate[i]));
                }
                
                try {
                    object.put("IPFIXData", array);
                    outCache.put(object.toString());
                    } catch (JSONException|InterruptedException ex) {
                        ex.printStackTrace();
                    }
                
                isOutcacheFilled = true;
                log.debug("IS OUTCACHE FILLED? : " + isOutcacheFilled);
            } else {
                log.debug("No Filter match");
                isOutcacheFilled = false;
            }
        } else {
            for (int i = 0; i < analyzerTemplate.length; i++) {
                 array.put(IPFIXData.get(analyzerTemplate[i]));
            }
            try {
                    object.put("IPFIXData", array);
                    outCache.put(object.toString());
                    } catch (JSONException|InterruptedException ex) {
                        ex.printStackTrace();
                    }
            isOutcacheFilled = true;
            log.debug("IS OUTCACHE FILLED? : " + isOutcacheFilled);
        }
    }//dcExport()

    /** 
     * Metóda, ktorá filtruje exportované údaje.
     * @param ipmb IP adresa meracieho bodu
     * @param template  IPFIX šablóna
     * @param data IPFIX údaje
     * @return true - ak údaje vyhovujú prijatému filtru, false = ak údaje nevyhovujú prijatúmu filtru.
     */
    private boolean ACPFilter(Inet4Address ipmb, IPFIXTemplateRecord template, IPFIXDataRecord data) {
        //boolean processflow = true;
        //int filter_priznak = 63;
        byte tempipmb[] = new byte[16];
        tempipmb = ipmb.getAddress();     //indata.addr.getAddress();
        short elid;
        //filter
        if (!(fFilter == 0) && sFilter != null) {
            log.debug("Comparing data with Filter");
            //if ((filter_priznak & ffilter) == ffilter) {

            //try
            try {

                if (!sFilter.mpMatches(tempipmb)) {
                    log.debug("No ipmb matches!");
                    return false;
                }

                if (template.getFieldByElementID(elementsInfo.getElementID("sourceIPv4Address")) != null) {
                    elid = 8;
                    //log.debug(sfilter.getSrcAddrs().length);
                    //log.debug(data.getFieldValue(template.getFieldSpecifierPosition(elid)).length);
                    if (!sFilter.srcIPMatches(data.getFieldValue(template.getFieldSpecifierPosition(elid)))) {
                        log.debug("No srcIP matches!");
                        return false;
                    }
                }

                if (template.getFieldByElementID(elementsInfo.getElementID("destinationIPv4Address")) != null) {
                    elid = 12;
                    if (!sFilter.dstIPMatches(data.getFieldValue(template.getFieldSpecifierPosition(elid)))) {
                        log.debug("No dstIP matches!");
                        return false;
                    }
                }

                if (template.getFieldByElementID(elementsInfo.getElementID("destinationIPv4Address")) != null) {
                    elid = 7;
                    if (!sFilter.srcPortMatches(stringToInt(decodeIpfixType("unsigned16", ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elid))))))) {
                        log.debug("No srcPort matches!");
                        return false;
                    }
                }

                if (template.getFieldByElementID(elementsInfo.getElementID("destinationTransportPort")) != null) {
                    elid = 11;
                    if (!sFilter.dstPortMatches(stringToInt(decodeIpfixType("unsigned16", ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elid))))))) {
                        log.debug("No dstPort matches!");
                        return false;
                    }
                }

                if (template.getFieldByElementID(elementsInfo.getElementID("protocolIdentifier")) != null) {
                    elid = 4;
                    if (!sFilter.protocolMatches(stringToInt(decodeIpfixType("unsigned8", ByteBuffer.wrap(data.getFieldValue(template.getFieldSpecifierPosition(elid))))))) {
                        log.debug("No Porotocol matches!");
                        return false;
                    }
                }
                log.debug("Data OK");
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
            //try-catch
            //}//if
        }
        return true;
    }//dcFilter()

    /**
     * Metóda na prekonvertovanie Stringu na Int.
     * @param string String, ktorý sa má prekonvertovať na Int
     * @return prekonvertovaný String na Int
     */
    public int stringToInt(String string) {
        return Integer.parseInt(string);
    }

    /**
     * Dekóduje dátový typ informačného elementu špecifikovaného v IPFIX pre uloženie do databázy
     * @param type Typ informačného elementu podľa IPFIX
     * @param buffer Hodnota informačného elementu v ByteBuffri
     * @return Dekódovaná hodnota pre uloženie do databázy.
     * @throws java.net.UnknownHostException Ak hodnota v informačnom elemente typu IP addresa je neplatná
     */
    @Deprecated
    private String decodeIpfixType(String type, ByteBuffer buffer) throws UnknownHostException {
        String returnValue = null;
        if (type.equals("unsigned8")) {
//        	if (buffer.capacity() != 1) throw new BufferUnderflowException();
            returnValue = Short.toString(Support.unsignByte(buffer.get()));

        } else if (type.equals("unsigned16")) {
//        	if (buffer.capacity() != 2) throw new BufferUnderflowException();
            returnValue = Integer.toString(Support.unsignShort(buffer.getShort()));

        } else if (type.equals("unsigned32")) {
//        	if (buffer.capacity() != 4) throw new BufferUnderflowException();
            returnValue = Long.toString(Support.unsignInt(buffer.getInt()));

        } else if (type.equals("unsigned64")) {
//        	if (buffer.capacity() != 8) throw new BufferUnderflowException();
            BigInteger big = new BigInteger(Long.toBinaryString(buffer.getLong()), 2);
            returnValue = big.toString();

        } else if (type.equals("ipv4Address")) {
//        	if (buffer.capacity() != 4) throw new BufferUnderflowException();
            returnValue = Inet4Address.getByAddress(buffer.array()).getHostAddress();

        } else if (type.equals("ipv6Address")) {
//        	if (buffer.capacity() != 16) throw new BufferUnderflowException();
            returnValue = Inet6Address.getByAddress(buffer.array()).getHostAddress();

        } else if (type.equals("macAddress")) {
            // toto sa zatial asi neexportuje takze nevieme kolko presne by to malo byt, zatial davam velkost macAddr  = 6B
            // The type "macAddress" represents a string of 6 octets. (http://tools.ietf.org/html/rfc5102#section-3.1.12)
//        	if (buffer.capacity() != 6) throw new BufferUnderflowException();
            returnValue = new String(buffer.array());

        } else if (type.equals("string")) {
            returnValue = new String(buffer.array());

        } else if (type.equals("octetArray")) {
            returnValue = new String(buffer.array());

        } else if (type.equals("dateTimeSeconds")) {
//        	if (buffer.capacity() != 4) throw new BufferUnderflowException();
            returnValue = Long.toString(Support.unsignInt(buffer.getInt()));

        } else if (type.equals("dateTimeMilliseconds")) {
//        	if (buffer.capacity() != 8) throw new BufferUnderflowException();
            BigInteger big = new BigInteger(Long.toBinaryString(buffer.getLong()), 2);
            returnValue = big.toString();

        } else if (type.equals("dateTimeMicroseconds")) {
//        	if (buffer.capacity() != 8) throw new BufferUnderflowException();
            BigInteger big = new BigInteger(Long.toBinaryString(buffer.getLong()), 2);
            returnValue = big.toString();

        } else if (type.equals("dateTimeNanoseconds")) {
//        	if (buffer.capacity() != 8) throw new BufferUnderflowException();
            BigInteger big = new BigInteger(Long.toBinaryString(buffer.getLong()), 2);
            returnValue = big.toString();

        }
//        else {
//        	throw new UnsupportedDataTypeException();
//        }
        return returnValue;
    }//decodeIpfixType()


    public void dispatchIPFIXRecord(Inet4Address ipmb, IPFIXTemplateRecord template, IPFIXDataRecord data) {
        log.debug("DISPATCH IPFIX RECORD IS OUTCACHE FILLED? : " + isOutcacheFilled);
        while (isOutcacheFilled == true) {
            try {
                log.debug("Waiting for empty outcache ...");
                Thread.sleep(10);//TOTO JE POTREBNE NASTAVIT PODLA TRAFFIC
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
        log.debug("NOW DISPATCHING, IT TOOK A WHILE...");
        if (isOutcacheFilled == false) {
            if (templateAccepted == true) {
                if (isPaused == false) {
                    ACPExport(ipmb, template, data);
                } else {
                    log.debug("Data sending Paused!");
                }

            } else {
                log.debug("Template not accepted!");
            }
        } else {
            log.debug("NOT AN EMPTY OUTCACHE AFTER WAITING???....:O");//TU BY SA NEMAL ODSTAT NIKDY
        }
    }//dispatchIPFIXRecord()

    /**
     * hlavný loop pre ACP, kde sa kontroluje spojenie, prebieha príjem riadiacich správ a samotné posielanie dát vyhovujúce šablóne a filtračným kritériam.
     */
    @Override
    public void run() {
        Socket connection = new Socket();
        this.fFilter = 0;
        while (alive) {
            setActive(false); // pre istotu ak by nepresiel finally blok  ;)
            synchronized (pool) {
                while (pool.isEmpty() && alive) {
                    try {
                        pool.wait();
                    } catch (InterruptedException e) {
                        log.debug("Thread interrupted when waiting : " + e);
                    }
                }
                if (alive) {
                    connection = (Socket) pool.remove(0);
                }
            }
            if (alive) // processing connection
            {
                try {
                    makeconnection(connection);
                    try {
                        connection.setKeepAlive(IJXConstants.SET_ACP_KEEPALIVE);
                    } catch (SocketException ex) {
                        log.error(ex.getMessage());
                    }
                    //read
                    String login = inString.readLine();
                    String passwd = inString.readLine();
                    //log.debug(login + " ? " + Config.dclogin);
                    //log.debug(passwd + " ? " + Config.dcpassword);
                    if (ACPauthentication(connection, login, passwd)) {
                        setActive(true); // priznak aktivneho spojenia
                    }

                    while (connection.isConnected()) { // (connection.isConnected()) {
                        while (inpData.available() > 0) { //|| (inpdata.available()>0)
                            log.debug("Input data available!");
                            ACPCommunication(inpData.readInt());
                            try {
                                log.debug("Waiting for a while...");
                                Thread.sleep(IJXConstants.SEND_FLOW_FREQUENCY); // toto tu je crucial ... ako ale spravit zamok nad dvomi hodnotami ?
                                //presnejsie ako kontrolovat vstup aj vystup a potom blokovat nad obidvomi ????
                            } catch (InterruptedException ex1) {
                                log.error(ex1);
                            }
                        }

                        if (isOutcacheFilled) {

                            if (!outCache.isEmpty()) {
                                int temp = 55;
                                boolean temp2 = false;
                                    log.debug("#1# SENDING DATA TO ANALYZER");
                                    outData.writeInt(0);
                                    outData.flush();
                                        try {
                                            if (inpData.available() > 0) {
                                                log.debug("INP DAT AV ON THE BIG.");
                                                temp2 = true;
                                                temp = inpData.readInt();
                                                
                                            }
                                            sendData = outCache.take();
                                            log.debug("#1# SENDING DATA TO ANALYZER:" + new String(sendData));
                                            outObject.writeObject(sendData);
                                            outObject.flush();    
                                            int c = inpData.readInt();
                                            if (c == 5) {
                                                log.debug("2#2 data received on the other side" + c);
                                            } else {
                                                temp2 = true;
                                                temp = c;
                                                c = inpData.readInt();
                                                if (c == 5) {
                                                    log.debug("2#2 data received on the other side" + c);
                                                } else {
                                                    log.debug("Chyba kukni sem, kde sa vypisujem ;)");
                                                }
                                                }
                                        } catch (InterruptedException ex) {
                                            log.error(ex.getMessage());
                                        }
                                    
                                    log.debug("#1# DATA SENT!");
                                    isOutcacheFilled = false;
                                    if (temp2) {
                                        ACPCommunication(temp);
                                    }
                            }
                        }
                    }
                    log.debug("DOSTALI SME SA DO 1");
                    /*while (!outcache.isEmpty()) {
                    log.debug("Emptying outcache after not active detection.");
                    try {
                    outcache.take();
                    } catch (InterruptedException ex) {
                    log.debug(ex.getMessage());
                    }
                    }
                    active = false;
                    isOutcacheFilled = false;
                    templateaccepted = false;
                    filteraccepted = false;
                    transfertypeaccepted = false;
                    ispaused= false;
                     */
                } catch (SocketException se) {
                    log.warn("DC unexpected close from client: " + connection.getInetAddress().getHostName());
                    log.error(se.getMessage());
                } catch (IOException e) {
                    log.error("IO EXCEPTION :" + e.getMessage());
                } finally {
                    try {
                        log.debug("Closing connection in try-catch");
                        connection.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    log.debug("DOSTALI SME SA DO 2");
                    setActive(false);
                    while (!outCache.isEmpty()) {
                        log.debug("Emptying outcache after not active detection.");
                        try {
                            outCache.take();
                        } catch (InterruptedException ex) {
                            log.debug(ex.getMessage());
                        }
                    }
                    isOutcacheFilled = false;
                    templateAccepted = false;
                    //filteraccepted = false;
                    //transferTypeAccepted = false;
                    isPaused = false;
                }
            }

        } // end while
//  System.out.println("Tento bod by mal byt nedosazitelny kym sa nezavola DIE");

    } // end run

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
