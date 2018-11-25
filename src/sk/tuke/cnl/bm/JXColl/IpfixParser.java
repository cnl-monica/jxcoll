/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm.JXColl;

import sk.tuke.cnl.bm.DataFormatException;
import java.awt.image.DataBuffer;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import sk.tuke.cnl.bm.JXColl.IPFIX.FieldSpecifier;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXDataRecord;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXMessage;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXSet;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXTemplateRecord;
import org.apache.log4j.Logger;
import sk.tuke.cnl.bm.JXColl.IPFIX.IPFIXOptionsTemplateRecord;
import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixUdpTemplateCache;
import sk.tuke.cnl.bm.JXColl.IPFIX.IpfixSingleSessionTemplateCache;
import sk.tuke.cnl.bm.TemplateException;

/**
 * This class is used to parse IPFIX message and create objects of received data, that can be easily interpreted.
 * @author Tomas Verescak
 */
public class IpfixParser {

    private static Logger log = Logger.getLogger(IpfixParser.class.getName());
    private final int TEMPLATE_SET = 2, OPTIONS_TEMPLATE_SET = 3;
    private IpfixUdpTemplateCache udpTemplateCache;
    private IpfixSingleSessionTemplateCache singleSessionTemplateCache;
    private TransportProtocol transportProtocol;
    private RecordDispatcher dispatcher = RecordDispatcher.getInstance();

    public enum TransportProtocol {

        UDP, TCP, SCTP;
    }

    /**
     * Creates IpfixParser for use with UDP transport protocol
     * @param templateCache 
     */
    public IpfixParser(IpfixUdpTemplateCache templateCache) {
        this.udpTemplateCache = templateCache;
        this.transportProtocol = TransportProtocol.UDP;
    }

    /**
     * Creates IpfixParser for use with TCP or SCTP transport protocol
     * @param templateCache
     * @param protocol 
     */
    public IpfixParser(IpfixSingleSessionTemplateCache templateCache, TransportProtocol protocol) {
        this.singleSessionTemplateCache = templateCache;
        this.transportProtocol = protocol;
    }

    /**
     * Parses IPFIX message contained in buffer. Calls method parseIpfixSet() while
     * there are some remaining bytes in buffer.
     * @param buffer input buffer
     * @param ipfixDevice IP address and port of exporter
     * @param time time when this message was received
     * @return IPFIXMessage parsed message
     * @throws DataFormatException
     * @throws TemplateException 
     */
    public IPFIXMessage parseIpfixMessage(ByteBuffer buffer, InetSocketAddress ipfixDevice, long time) throws DataFormatException, TemplateException, UnsupportedEncodingException {
        //data obalime do buffra pre lepsiu pracu s nimi
//        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        //@TODO: check ci je velkost spravy taka ako by mala byt
        if (buffer.remaining() != Support.unsignShort(buffer.getShort(2))) {
            throw new DataFormatException("Message length (" + buffer.remaining() + ") is not as stated in header (" + Support.unsignShort(buffer.getShort(2)) + ")");
        }

//        log.debug(String.format("capacity: %d, limit: %d, position: %d", dataBuffer.capacity(), dataBuffer.limit(), dataBuffer.position()));
        // vytvorime objekt IPFIX spravy
        IPFIXMessage message = new IPFIXMessage();
        //nastavime cas prijatia spravy
        message.setReceiveTime(time);
        // z hlavicky vyberieme data o IPFIX sprave
        message.setHeader(buffer);
        // vypiseme ich na obrazovku
        printIpfixMessageHeader(message);

        // dlzka spravy vratane hlavicky a set-ov
        int messageLength = message.getLength();

        // prehladame vsetky IPFIX Sety
        while (buffer.hasRemaining()) {
            parseIpfixSet(message, buffer, ipfixDevice);
        }

        return message;
    }

    /**
     * Parses IPFIX set contained in buffer. While there are remaining bytes left in set, 
     * calls methods for parsing particular records.
     * 
     * @param message message object
     * @param buffer input buffer
     * @param ipmb IP address and port of exporter
     * @throws IPFIXTemplateException When trying to add existing template or withdraw nonexistent one.
     * @throws DataFormatException When data was corrupted.
     */
    private void parseIpfixSet(IPFIXMessage message, ByteBuffer buffer, InetSocketAddress ipfixDevice) throws DataFormatException, TemplateException, UnsupportedEncodingException {

        int positionBeginning = buffer.position();

        // ziskame si zakladne info o sete
        int setID = Support.unsignShort(buffer.getShort()); // ci je to Template, Options Template alebo Data Set
        int setLength = Support.unsignShort(buffer.getShort()); // vratane hlavicky set-u, vsetkych recordov a pripadneho paddingu

        log.debug("dataBuffer.remaining() = " + buffer.remaining());
        if (setLength > buffer.remaining() + 4) {
            throw new DataFormatException("Set states that it is longer than remaining data part is!");
        }

        // vytvorime objekt IPFIX set-u
        IPFIXSet ipfixSet = new IPFIXSet(setID, setLength);
        ipfixSet.setOffsets(buffer.position() - 4);
        log.debug(String.format("Start offset: %d, End Offset: %d", ipfixSet.getStartOffset(), ipfixSet.getEndOffset()));
        // vypiseme na obrazovku hlavicku
        printIpfixSetHeader(ipfixSet);


        //@TODO: jeden SET moze mat zaznamy len svojho typu, teda while presunut do jednotlivych vetiev
        //musime prejst vsetky zaznamy setu - predtym sa predpokladalo ze existuje len jeden zaznam na set


        // zistime druh IPFIX Set-u
        set:
        switch (ipfixSet.getSetID()) {

            /*******************************************
             *              TEMPLATE SET               *
             *******************************************/
            case TEMPLATE_SET:
                //preskumame vsetky template recordy v ramci setu
                while (buffer.position() - positionBeginning < setLength) {
                    IPFIXTemplateRecord template = parseIpfixTemplateRecord(message, ipfixSet, buffer);
                    // celu sablonu pridame do ipfix set-u
                    ipfixSet.addTemplateRecord(template);
                    log.debug(template);




                    // ak sme nahodou na konci, zistime ci nejde o PADDING
                    // field specifiery su bud 32 alebo 64 bitove. Zarovnanie moze byt max 4 bajty a kazdy musi byt nulovy.
                    int paddingLength = setLength - (buffer.position() - positionBeginning);
//                    log.debug("PaddingLength = " + paddingLength);
//                    log.debug("Set Length = " + setLength);
//                    log.debug("dataBuffer.position() = " + dataBuffer.position());
//                    log.debug("position.beginning() = " + positionBeginning);

                    if (paddingLength != 0) {
                        if (paddingLength > 0 && paddingLength < 4) {
                            log.warn("Padding in template is less than 4 bytes, data may be corrupt!");
                            throw new DataFormatException("Padding in template is not 4 bytes (" + paddingLength + "), data may be corrupt!");

                        }
                        if (paddingLength == 4) {// moze ist o prazdnu sablonu !!! template withdrawal, treba otestovat

                            // ak by bola velkost najmensieho zaznamu 4 (templatewithdrawal message), tak nemozme to uvazovat ako padding
                            if (ipfixSet.getSmallestTemplateRecordSize() != paddingLength) {
                                //nevyhod vynimku, ide o template withdrawal message neni to padding
                                // ide o padding a teda posunieme sa dalej o 4 bajty
                                //dataBuffer.position(dataBuffer.position() + 4);
                                int paddingData = buffer.getInt(); // zaroven tymto sa posunieme o 4 bajty
                                log.debug("Padding used in template record!");
                                if (paddingData != 0) {
                                    log.warn("Padding at the end of Template Set is not composed of NULL bytes!" + Integer.toHexString(paddingData));
                                }
                            }
                        }
//                        else {
//                            // ak je padding vacsi ako 4 bajty
//                            log.warn("Padding in template is more than 4 bytes, data may be corrupt!");
//                            throw new DataFormatException("Padding in template is more than 4 bytes (" + paddingLength + "), data may be corrupt!");
//                        }
                    }//ak je padding
                }//while

                // pre kazdu prijatu sablonu, pridame ju do cache
                for (IPFIXTemplateRecord template : ipfixSet.templateRecords) {

                    protocol:
                    switch (transportProtocol) {
                        case UDP:
                            // uchovame sablonu v template cache / nahradime ju
                            if (template.getTemplateID() <= 255) {
                                throw new TemplateException("Template #" + template.getTemplateID() + " is not valid template identifier!");
                            }
                            udpTemplateCache.addTemplate(template, ipfixDevice, message.getObservationDomainID());
//                    log.debug("Velkost cache po vlozeni: " + ipfixTemplateCache.getTemplates(ipmb, ipfixMessage.getObservationDomainID()).getTemplates().size());
                            break protocol;

                        case TCP:
                        case SCTP:
                            // ak sa jedna o Template Withdrawal Message
                            if (template.getFieldCount() == 0) {

                                if (template.getTemplateID() == 2) {
                                    // All Data Template Withdrawal Message
                                    singleSessionTemplateCache.removeAll(message.getObservationDomainID());
                                    log.info("All templates within observation domain " + message.getObservationDomainID() + " were withdrawn!");
                                } else {
                                    // Template Withdrawal Message
                                    if (!singleSessionTemplateCache.contains(template.getTemplateID(), message.getObservationDomainID())) {
                                        throw new TemplateException("Attempt to withdraw Template #" + template.getTemplateID() + ", which does not exist in cache!");
                                    }
                                    singleSessionTemplateCache.remove(template.getTemplateID(), message.getObservationDomainID());
                                    log.info("Template ID " + template.getTemplateID() + " within observation domain " + message.getObservationDomainID() + " was withdrawn!");
                                }
                            } else {
                                if (template.getTemplateID() <= 255) {
                                    throw new TemplateException("Template #" + template.getTemplateID() + " is not valid template identifier!");
                                }
                                // inak pridaj sablonu do cache
                                if (singleSessionTemplateCache.contains(template.getTemplateID(), message.getObservationDomainID())) {
                                    throw new TemplateException("Template #" + template.getTemplateID() + " already exist in cache!");
                                }
                                singleSessionTemplateCache.addTemplate(template, message.getObservationDomainID());
                                log.info("Template with ID " + template.getTemplateID() + " was added to template cache!");
                            }
                            break protocol;
                    }//swtich protocol
                }

                break set;


            /*******************************************
             *          OPTIONS TEMPLATE SET           *
             *******************************************/
            case OPTIONS_TEMPLATE_SET:
                while (buffer.position() - positionBeginning < setLength) {

                    IPFIXOptionsTemplateRecord optionsTemplate = parseIpfixOptionsTemplateRecord(message, ipfixSet, buffer);
                    //                // celu options sablonu pridame do ipfix set-u
                    ipfixSet.addOptionsTemplateRecord(optionsTemplate);
                    //                //@TODO: uchovat options template record do cache?


                    log.debug("Smallest Options Template Length = " + ipfixSet.getSmallestOptionsTemplateRecordSize());
                    // ak sme nahodou na konci, zistime ci nejde o padding
                    // Zarovnanie moze byt max 2 bajty a kazdy musi byt nulovy.
                    int paddingLength = setLength - (buffer.position() - positionBeginning);
                    if (paddingLength != 0) {

                        if (paddingLength == 2) {
                            short paddingData = buffer.getShort();  // tymto sa aj posunieme dalej, teda preskocime padding
                            if (paddingData != 0x0000) {
                                log.warn("Padding (2 bytes) at the end of Template Set is not composed of NULL bytes!" + Integer.toHexString(paddingData));
                            }
                        }
                        if (paddingLength == 4) {
                            // ak by bola velkost najmensieho zaznamu 4 (templatewithdrawal message), tak nemozme to uvazovat ako padding
                            log.debug("Smallest Options Template Length = " + ipfixSet.getSmallestOptionsTemplateRecordSize());
                            if (ipfixSet.getSmallestOptionsTemplateRecordSize() != paddingLength) {
                                //nevyhod vynimku, ide o template withdrawal message neni to padding
                                // ide o padding a teda posunieme sa dalej o 4 bajty
                                //dataBuffer.position(dataBuffer.position() + 4);
                                int paddingData = buffer.getInt(); // zaroven tymto sa posunieme o 4 bajty
                                log.debug("Padding used in template record!");
                                if (paddingData != 0) {
                                    log.warn("Padding at the end of Template Set is not composed of NULL bytes!" + Integer.toHexString(paddingData));
                                }
                            }

                            short paddingData = buffer.getShort();  // tymto sa aj posunieme dalej, teda preskocime padding
                            if (paddingData != 0x0000) {
                                log.warn("Padding (2 bytes) at the end of Template Set is not composed of NULL bytes!" + Integer.toHexString(paddingData));
                            }
                        }
                        if (paddingLength == 6) {
                            byte[] padding = new byte[6];
                            buffer.get(padding, 0, 6);   // tymto sa aj posunieme dalej, teda preskocime padding
                            boolean nuly = true;
                            for (int i = 0; i < padding.length; i++) {
                                if (padding[i] != 0x00) {
                                    nuly = false;
                                    break;
                                }
                            }
                            if (!nuly) {
                                log.warn("Padding (2 bytes) at the end of Template Set is not composed of NULL bytes!" + Arrays.toString(padding));
                            }
                        } else {
                            log.error("Padding is not 2 nor 6 bytes, but " + paddingLength + " long!");
                            throw new DataFormatException("Padding is not 2 nor 6 bytes, but " + paddingLength + " long!");
                        }
                    }
                }//while

                for (IPFIXOptionsTemplateRecord optionsTemplate : ipfixSet.optionsTemplateRecords) {
                    protocol:
                    switch (transportProtocol) {
                        case UDP:
                            // uchovame sablonu v template cache / nahradime ju
                            udpTemplateCache.addTemplate(optionsTemplate, ipfixDevice, message.getObservationDomainID());
                            //                    log.debug("Velkost cache po vlozeni: " + ipfixTemplateCache.getTemplates(ipmb, ipfixMessage.getObservationDomainID()).getTemplates().size());
                            break protocol;

                        case TCP:
                        case SCTP:
                            // ak sa jedna o Template Withdrawal Message
                            if (optionsTemplate.getFieldCount() == 0) {

                                if (optionsTemplate.getTemplateID() == 3) {
                                    // All Options Template Withdrawal Message
                                    singleSessionTemplateCache.removeAll(message.getObservationDomainID());
                                    log.info("All Option Templates within observation domain " + message.getObservationDomainID() + " were withdrawn!");
                                } else {
                                    // Template Withdrawal Message                                                      test presunuty do Cache
//                                    if (!singleSessionTemplateCache.contains(optionsTemplate.getTemplateID(), message.getObservationDomainID())) {
//                                        throw new TemplateException("Attempt to withdraw Options Template #" + optionsTemplate.getTemplateID() + ", which does not exist in cache!");
//                                    }
                                    singleSessionTemplateCache.remove(optionsTemplate.getTemplateID(), message.getObservationDomainID());
                                    log.info("Option Template ID " + optionsTemplate.getTemplateID() + " within observation domain " + message.getObservationDomainID() + " was withdrawn!");
                                }
                            } else {
                                // inak pridaj sablonu do cache, pripadnu vynimku odchyti TCPProcessor alebo SCTPProcessor
                                singleSessionTemplateCache.addTemplate(optionsTemplate, message.getObservationDomainID());
                                log.info("Option Template with ID " + optionsTemplate.getTemplateID() + " was added to template cache!");
                            }
                            break protocol;
                    }//swtich protocol
                }

                break set;

            default:
                // od 4 do 255 je rezervovane pre buduce pouzitie
                if (setID == 0 || setID == 1 || (setID >= 4 && setID <= 255)) {
                    // discard this set 4 to 255, new position is incremented by set length minus set header length
                    log.error("SetID " + ipfixSet.getSetID() + " is not used for historical reasons or reserved for future use. Discarding...");
                    int newPosition = buffer.position() + ipfixSet.getLength() - ipfixSet.getHeaderSize(); // 4 is length of set header
                    buffer.position(newPosition);
                    // vacsie ako 255 su datove recordy

                } else {

                    /*******************************************
                     *                DATA SET                 *
                     *******************************************/
                    // tu spracovavame DATA SET - setID > 255
                    int positionBefore = buffer.position() - ipfixSet.getHeaderSize();
                    while (buffer.position() - positionBefore < ipfixSet.getLength()) {
                        //                        log.debug("position before parsing DATA Record: " + dataBuffer.position());
                        IPFIXDataRecord ipfixData = parseIpfixDataRecord(message, ipfixSet, buffer, ipfixDevice);
                        //                        log.debug("position after parsing DATA Record:" + dataBuffer.position());

                        if (ipfixData != null) {
                            ipfixSet.addDataRecord(ipfixData); // vlozime data do Set-u
                        }
                    }

                    IPFIXTemplateRecord ipfixTemplate = null;


                    // ziskame si sablonu prisluchajucu k datam
                    switch (transportProtocol) {
                        case UDP:
                            ipfixTemplate = udpTemplateCache.getByID(ipfixSet.getSetID(), ipfixDevice, message.getObservationDomainID());
                            break;

                        case TCP:
                        case SCTP:
                            ipfixTemplate = singleSessionTemplateCache.get(ipfixSet.getSetID(), message.getObservationDomainID());
                            break;
                    }
                    if (ipfixTemplate != null) {
                        if (ipfixTemplate instanceof IPFIXOptionsTemplateRecord) {
                            log.debug("Data for Options Template!");
                        }

                        for (IPFIXDataRecord dataRecord : ipfixSet.dataRecords) {
                            // posleme sablonu a data dispecerovi na export
                            dispatcher.dispatchIPFIXRecord(ipfixTemplate, dataRecord, (InetSocketAddress) ipfixDevice);
                            // datove zaznamy su paddovane pomocou informacneho elementu paddingOctets... treba ho ignorovat
                            //@TODO: ignorovat padding octets pocas dekodovania - done
                        }
                    } else {
                        // ked nemame sablonu
                        log.error(String.format("Template #%d for this data set was not found! Skipping data records in this set!",
                                ipfixSet.getSetID(), ipfixSet.dataRecords.size()));
                    }

                }
        }//switch


        log.debug(String.format("Current position in Set: %d", buffer.position() - positionBeginning));




        log.debug(String.format("End of set was reached, position: %d", buffer.position()));
    }

    /**
     * Parses IPFIX template record contained in the buffer. Retrieves all field specifiers
     * 
     * @param message message object
     * @param set IPFIX set this record belongs to
     * @param buffer input buffer
     * @return IPFIXTemplateRecord parsed template record
     * @throws DataFormatException When there is no space left in the buffer to retrieve all field specifiers
     */
    private IPFIXTemplateRecord parseIpfixTemplateRecord(IPFIXMessage message, IPFIXSet set, ByteBuffer buffer) throws DataFormatException {
        //@TODO: overit spravnost spracovania Template Record-u
        int positionBeginning = buffer.position();
        int setLength = set.getLength();

        int templateID = Support.unsignShort(buffer.getShort());
        int fieldCount = Support.unsignShort(buffer.getShort());

        // vytvorime novy zaznam sablony
        IPFIXTemplateRecord ipfixTemplate = new IPFIXTemplateRecord(templateID, fieldCount);
        ipfixTemplate.setLastReceived(message.getReceiveTime());
        // vypiseme info o hlavicke na obrazovku
        printIpfixTemplateRecordHeader(ipfixTemplate);

        //        log.debug("ipfixset.getEndOffset() = " + ipfixSet.getEndOffset());
        //iteracia cez vsetky field specifiery
        for (int i = 0; i < fieldCount; i++) {
            // vytvorime prazdny field specifier

            int remainingBytesInSet = set.getEndOffset() - buffer.position();
            //            log.debug("remainingBytesInSet = " + remainingBytesInSet);
            if (remainingBytesInSet < 4) {
                throw new DataFormatException("Template Set is not long enough to hold all field specifiers!");
            }

            FieldSpecifier fieldSpecifier = new FieldSpecifier();
            // nastavime fieldSpecifier podla dat v buffri (aktualizuje sa aj pozicia)
            fieldSpecifier.setFieldSpecifier(buffer); // pozicia sa posunie bud o 4 alebo 8 bajtov (ak je to enterprise IE)
            if (fieldSpecifier.isEnterpriseBit() && buffer.position() > set.getEndOffset()) {
                throw new DataFormatException("TemplateRecord spans beyond the template set!");
            }
            // @TODO: OSETRIT PADDING !!!!!!!!!!!!!! http://www.ietf.org/rfc/rfc5101.txt EDIT: Vramci sablony netreba, lebo je vzdy zarovnana na 32 bitov
            // a 32 bitov je postacujucich
            //            log.debug("Position after specifier read: " + dataBuffer.position());

            //            log.debug("Field specifier - Enterprise bit: " + fieldSpecifier.isEnterpriseBit());
            //            log.debug("Field specifier - Information element ID: " + fieldSpecifier.getElementID());
            //            log.debug("Field specifier - Length: " + fieldSpecifier.getFieldLength());
            //            log.debug("Field specifier - Enterprise number: " + fieldSpecifier.getEnterpriseNumber());

            // do zaznamu sablony pridame field specifier
            ipfixTemplate.addField(fieldSpecifier);
        }

        // vratime vysledok volajucej metode
        return ipfixTemplate;
    }

    /**
     * Parses IPFIX options template record contained in the buffer. Retrieves all field specifiers.
     * 
     * @param message message object
     * @param set IPFIX set this record belongs to
     * @param buffer input buffer
     * @return IPFIXOptionsTemplateRecord parsed options template record
     * @throws DataFormatException When there is no space left in the buffer to retrieve all field specifiers
     */
    private IPFIXOptionsTemplateRecord parseIpfixOptionsTemplateRecord(IPFIXMessage message, IPFIXSet set, ByteBuffer buffer) throws DataFormatException {
        //@TODO: overit spravnost spracovania Options Template Record-u

        boolean isTemplateWithdrawal = false;
        int positionBeginning = buffer.position();
        int setLength = set.getLength();
        int scopeFieldCount = 0;

        int templateID = Support.unsignShort(buffer.getShort());
        int fieldCount = Support.unsignShort(buffer.getShort());
        if (fieldCount == 0) {
            isTemplateWithdrawal = true;
        } else {
            ///nacitame z buffra len ak to nie je template withdrawal
            scopeFieldCount = Support.unsignShort(buffer.getShort());

        }

        if (!isTemplateWithdrawal && scopeFieldCount == 0) {
            // chyba, nesmie to byt 0
            throw new DataFormatException("Options Template has field count set to 0!");
        }

        // vytvorime novy zaznam options sablony
        IPFIXOptionsTemplateRecord optionsTemplate = new IPFIXOptionsTemplateRecord(templateID, fieldCount, scopeFieldCount);
        optionsTemplate.setLastReceived(message.getReceiveTime());
        // vypiseme info o hlavicke na obrazovku
        printIpfixOptionsTemplateRecordHeader(optionsTemplate);


        //        log.debug("ipfixset.getEndOffset() = " + ipfixSet.getEndOffset());
        int remainingBytesInSet = set.getEndOffset() - buffer.position();


        // prejdeme cez vsetky fieldy (scope aj normal)
        for (int i = 0; i < optionsTemplate.getFieldCount(); i++) {

            //            log.debug("remainingBytesInSet = " + remainingBytesInSet);
            //            log.debug("databuffer.position() = " + dataBuffer.position());
            if (remainingBytesInSet < 4) {
                throw new DataFormatException("Template Set is not long enough to hold all field specifiers!");
            }
            // vytvorime prazdny field specifier
            FieldSpecifier fieldSpecifier = new FieldSpecifier();
            // nastavime fieldSpecifier podla dat v buffri (aktualizuje sa aj pozicia)
            fieldSpecifier.setFieldSpecifier(buffer);
            // do zaznamu options sablony pridame field specifier

            // je to scope field za predpokladu ze scope fieldy su na zaciatku sablony - SU!!!
            //@TODO: overit, ci scope fieldy su vzdy na zaciatku
            boolean isScope = i < optionsTemplate.getScopeFieldCount();
            fieldSpecifier.setScope(isScope);
            optionsTemplate.addField(fieldSpecifier);

        }
//        if (optionsTemplate.getScopeFieldCount() == 0) {
//            throw new DataFormatException("Scope field count is zero!");
//        }

        // vratime options sablonu volajucej metode
        return optionsTemplate;
    }

    /**
     * Parses IPFIX data record contained in the buffer. Retrieves all field specifiers.
     * 
     * @param message message object
     * @param set IPFIX set this record belongs to
     * @param buffer input buffer
     * @param ipfixDevice IP address and port of exporter
     * @return IPFIXDataRecord parsed data record
     * @throws DataFormatException 
     */
    private IPFIXDataRecord parseIpfixDataRecord(IPFIXMessage message, IPFIXSet ipfixSet, ByteBuffer dataBuffer, InetSocketAddress ipfixDevice) throws DataFormatException {
        //@TODO: overit spravnost spracovania Data Record-u
        log.debug("****** DATA Record ******");
        //        log.debug("Data record recognized ...");
        IPFIXTemplateRecord ipfixTemplate = null;
        IPFIXDataRecord ipfixData = null;

        switch (transportProtocol) {
            case UDP:

                if (udpTemplateCache.checkForPresence(ipfixSet.getSetID(), ipfixDevice, message.getObservationDomainID())) {
                    // ziskame si sablonu prisluchajucu k tomuto data recordu
                    ipfixTemplate = udpTemplateCache.getByID(ipfixSet.getSetID(), ipfixDevice, message.getObservationDomainID());
//                    log.debug("Referenced template ID: " + ipfixTemplate.getTemplateID());
                    // vytvorime data record na zaklade sablony
                    ipfixData = new IPFIXDataRecord(ipfixTemplate.getTemplateID());
                } else {
                    // ak nemame sablonu pre tento data record
                    log.error(String.format("Template with ID %s was not found for this data record!", ipfixSet.getSetID()));
                    int newPosition = dataBuffer.position() + ipfixSet.getLength() - ipfixSet.getHeaderSize(); // 4 is length of set header
                    dataBuffer.position(newPosition);
                    // nema zmysel dalej pokracovat
                    return null;
                }

                break;

            case TCP:
            case SCTP:

                if (singleSessionTemplateCache.contains(ipfixSet.getSetID(), message.getObservationDomainID())) {
                    // ziskame si sablonu prisluchajucu k tomuto data recordu
                    ipfixTemplate = singleSessionTemplateCache.get(ipfixSet.getSetID(), message.getObservationDomainID());
                    //                    log.debug("TemplateID: " + ipfixTemplate.getTemplateID());
                    ipfixData = new IPFIXDataRecord(ipfixTemplate.getTemplateID());
                } else {
                    // ak nemame sablonu pre tento data record - v ramci jedneho setu mozu byt datove recordy len jednej sablony
                    log.error(String.format("Template with ID %s was not found for this data record! Skipping this Data Set!", ipfixSet.getSetID()));
                    //                    int newPosition = dataBuffer.position() + ipfixSet.getLength() - ipfixSet.getHeaderSize(); // 4 is length of set header
                    int newPosition = ipfixSet.getEndOffset();
                    dataBuffer.position(newPosition);
                    // nema zmysel dalej pokracovat
                    return null;
                }
                break;
        }



        // ziskavajme data recordy, kym nie sme na konci Data Recordu, 4B ma hlavicka Set-u
        //        int positionBefore = dataBuffer.position();


        // ak je to fixed length Data Record
        //        int dataRecordLength = ipfixTemplate.getDataRecordLength();
        //        int processedBytes = 0;
        //        if (!ipfixTemplate.isOfVariableLength()) {
        //        }
        // while(dataBuffer.position() - positionBefore < ) {}
        //            while (dataBuffer.position() < positionBefore + ipfixSet.getLength() - ipfixSet.getHeaderSize()) {



        // prejdeme vsetky polozky sablony
        for (FieldSpecifier fieldSpecifier : ipfixTemplate.getFields()) {
            // skutocna velkost dat
            int dataLength = 0;

            // pre fixne IE ziskame velkost zo sablony
            dataLength = fieldSpecifier.getFieldLength();

            int remainingBytesInSet = ipfixSet.getEndOffset() - dataBuffer.position();

            // osetrenie pre variable length typy: octetArray, string
            if (fieldSpecifier.getFieldLength() == 65535) {  // RFC 5101 chapter 7
                // ide o variabilnu dlzku, dlzka je zakodovana v prvom / alebo dalsich bajtoch dat
                if (remainingBytesInSet == 0) {
                    throw new DataFormatException("Field data is longer than remaining bytes in Data Set!");
                }
                short length = Support.unsignByte(dataBuffer.get());

                // ak je dlzka mensia ako 255, dalsich <dlzka> bajtov su data
                // ak je dlzka 255, dlzka je zakodovana v dalsich 2 bajtoch (0-65535)
                // dalej mozme pokracovat vo vybere dat o velkosti: dataLength
                if (length < 255) {
                    dataLength = length;
                } else {
                    if (remainingBytesInSet < 2) {
                        throw new DataFormatException("Field data is longer than remaining bytes in Data Set!");
                    }
                    dataLength = Support.unsignShort(dataBuffer.getShort());

                }
//                dataLength = (length < 255) ? length : Support.unsignShort(dataBuffer.getShort());
            }

//            log.debug("data Length: " + dataLength);

            // osetrenie ak by sme chceli precitat data, ktore uz nepatria do sady.
            remainingBytesInSet = ipfixSet.getEndOffset() - dataBuffer.position();
//            log.debug("remainingBytesInSet = " + remainingBytesInSet);
//            log.debug("ipfixset.getEndOffset() = " + ipfixSet.getEndOffset());
            if (remainingBytesInSet < dataLength) {
                throw new DataFormatException("Field data is longer than remaining bytes in Data Set!");
            }

            // vytvorime pole o skutocnej velkosti dat
            byte[] value = new byte[dataLength];
            //log.debug(String.format("DataRecord before get - capacity: %d, limit: %d, position: %d", dataBuffer.capacity(), dataBuffer.limit(), dataBuffer.position()));

            // nacitame data z buffra do pomocneho pola
            for (int i = 0; i < dataLength; i++) {
                value[i] = dataBuffer.get();
            }

            // vlozime data do Data Recordu
            ipfixData.addFieldValue(value);



            //                log.debug("Data record processed!");
            //@TODO: increment sequence number in cache
        } // foreach

        // ak mame co do cinenia s paddingom
        // datove zaznamy fixnej velkosti
        if (!ipfixTemplate.isOfVariableLength()) {
            log.debug("data record length = " + ipfixTemplate.getDataRecordLength());
            int paddingSize = (ipfixSet.getLength() - ipfixSet.getHeaderSize()) % ipfixTemplate.getDataRecordLength();
            log.debug("padding size = " + paddingSize);


            int remainingBytesInSet = ipfixSet.getEndOffset() - dataBuffer.position();
            // ak je do konca sady presne tolko bajtov ako je vypocitany padding, tak dojdeme na koniec
            // tym padom v metode parseIPFIXSet v casti datoveho zaznamu bude signalizovane ze sme vsetko spravne sparsovali
            if (paddingSize == remainingBytesInSet) {
                dataBuffer.position(dataBuffer.position() + remainingBytesInSet);
            }
            // ak je fyzicky menej bajtov ako sa uvadza, tak dojde k chybe pri parsovani a reportuje sa poskodeny paket
        } else {
            log.debug("Variable sized data record!");
            log.debug("data record length = " + ipfixTemplate.getDataRecordLength());
            int smallestDataRecord = ipfixSet.getSmallestDataRecordSize();
            int remainingBytesInSet = ipfixSet.getEndOffset() - dataBuffer.position();
            // ak je pocet zvysnych bajtov mensi ako najmensi datovy zaznam, moze ist o padding
            // ale budeme to za padding povazovat, len vtedy
            if (remainingBytesInSet < smallestDataRecord && remainingBytesInSet < 8) {
                dataBuffer.position(dataBuffer.position() + remainingBytesInSet);
            }
        }


        // tu sme na konci datoveho zaznamu.. mozme skusit otestovat ci nie sme na konci a ci je tam nejaky padding

//        log.debug("Fields in data record: " + ipfixData.count());


        //            }//while
        //        }//if
        return ipfixData;
    }

    /**
     * 
     * @param message 
     */
    private void printIpfixMessageHeader(IPFIXMessage message) {
        log.debug("****** IPFIX MESSAGE HEADER ******");
        log.debug("Version number: " + message.getVersionNumber());
        log.debug("Message Length: " + message.getLength() + " bytes");
        log.debug("Export time: " + Support.SecToTimeOfDay(message.getExportTime()));
        log.debug("Export Time normal: " + message.getExportTime());
        log.debug("Sequence number: " + message.getSequenceNumber());
        log.debug("Observation domain ID: " + message.getObservationDomainID());
    }

    /**
     * 
     * @param set 
     */
    private void printIpfixSetHeader(IPFIXSet set) {
        log.debug("****** IPFIX SET HEADER ******");
        String setInfo = null;
        if (set.getSetID() == 2) {
            setInfo = "(TEMPLATE SET)";
        } else if (set.getSetID() == 3) {
            setInfo = "(OPTIONS TEMPLATE SET)";
        } else if (set.getSetID() > 255) {
            setInfo = "(DATA SET)";
        } else {
            setInfo = "(NOT USED)";
        }
        log.debug("Set ID: " + set.getSetID() + setInfo);
        log.debug("Set Length: " + set.getLength() + " bytes");
    }

    /**
     * 
     * @param template 
     */
    private void printIpfixTemplateRecordHeader(IPFIXTemplateRecord template) {
        log.debug("****** TEMPLATE RECORD HEADER ******");
        log.debug("Template ID: " + template.getTemplateID());
        log.debug("Field Count: " + template.getFieldCount());
    }

    /**
     * 
     * @param template 
     */
    private void printIpfixOptionsTemplateRecordHeader(IPFIXOptionsTemplateRecord template) {
        log.debug("****** OPTIONS TEMPLATE RECORD HEADER ******");
        log.debug("Options Template record ID " + template.getTemplateID() + " recognized ...");
        log.debug("Field Count: " + template.getFieldCount());
        log.debug("Scope Field Count: " + template.getScopeFieldCount());
    }
}
