/* 
 * Copyright (C) 2011 Tomas Verescak
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

import sk.tuke.cnl.bm.DataException;
//import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.activation.UnsupportedDataTypeException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.postgresql.util.Base64;

/**
 * Class for decoding IPFIX abstract data types to String representation
 * appropriate for DB storage (PostgreSQL)
 *
 * @author Tomas Verescak
 */
public class IpfixDecoder {

    private static Logger log = Logger.getLogger(IpfixDecoder.class);
    /**
     * All hexadecimal characters
     */
    private static final String HEXES = "0123456789ABCDEF";

    /**
     * This is utility class. Cannot be instantiated.
     */
    private IpfixDecoder() {
    }

    /**
     * Decodes data type contained in ByteBuffer argument into string form,
     * according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    public static Object decode(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {

        if (type.contains("unsigned")) { // musi byt prve testovane
            return decodeUnsignedIntegralType(type, buffer);

        } else if (type.contains("signed")) {
            return decodeSignedIntegralType(type, buffer);

        } else if (type.contains("float")) {
            return decodeFloatType(type, buffer);

        } else if (type.contains("Address")) {
            return decodeAddressType(type, buffer);

        } else if (type.matches("boolean")) {
            return decodeBooleanType(buffer);

        } else if (type.matches("string")) {
            return decodeStringType(buffer);

        } else if (type.matches("octetArray")) {
            return decodeOctetArrayType(buffer);

        } else if (type.contains("dateTime")) {
            return decodeDateTimeType(type, buffer);

        } else {
            throw new UnsupportedDataTypeException();
        }
    }

    /**
     * Decodes timestamp oriented data types contained in ByteBuffer argument
     * into string form, according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    private static Object decodeDateTimeType(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {
        // BigInteger big = null;
        switch (type) {
            /*
             The data type dateTimeseconds represents a time value in units of
             seconds normalized to the GMT timezone.  It MUST be encoded in a
             32-bit integer containing the number of seconds since 0000 UTC Jan 1,
             1970.  The 32-bit integer allows the time encoding up to 136 years.
             */
            case "dateTimeSeconds":
                if (buffer.capacity() > 4) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 4 bytes long! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() < 4) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 4, false);
                    return new Long(Support.unsignInt(fullCapBuffer.getInt()));
                }
                return new Long(Support.unsignInt(buffer.getInt()));

            /*   
             The data type dateTimeMilliseconds represents a time value in units
             of milliseconds normalized to the GMT timezone.  It MUST be encoded
             in a 64-bit integer containing the number of milliseconds since 0000
             UTC Jan 1, 1970.
             */
            case "dateTimeMilliseconds":
                if (buffer.capacity() > 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 8 bytes long! (" + buffer.capacity() + ")");
                }
//                if (buffer.capacity() < 8) {
//                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 8, false);
//                    // surovo nastavime ze je to kladne cislo, a teda dostaneme unsigned 64 cislo
//                    BigInteger unsignedNumber = new BigInteger(1, fullCapBuffer.array());
//                    return unsignedNumber;
//                }
//                big = new BigInteger(1, buffer.array()); // prvy parameter urcuje znamienko cisla, teda ze je to kladne a berie sa ako unsigned
//                return big;
                return new Long(buffer.getLong());


            /*   
             The data type dateTimeMicroseconds represents a time value in units
             of microseconds normalized to the GMT timezone.  It MUST be encoded
             in a 64-bit integer, according to the NTP format given in [RFC1305].
             */
            case "dateTimeMicroseconds":
                if (buffer.capacity() != 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 8 bytes long! (" + buffer.capacity() + ")");
                }
                return new Long(buffer.getLong()); // pretoze je to Timestamp, tak to ulozime do DB ako long, a az analyzer to interpretuje ako Timestamp
//                return Long.toString(new TimeStamp(buffer.getLong()).getTime()); // ntp timestamp

            /*   
             The data type of dateTimeNanoseconds represents a time value in units
             of nanoseconds normalized to the GMT time zone.  It MUST be encoded
             in a 64-bit integer, according to the NTP format given in [RFC1305].
             */
            case "dateTimeNanoseconds":
                if (buffer.capacity() != 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 8 bytes long! (" + buffer.capacity() + ")");
                }
                return new Long(buffer.getLong()); // pretoze je to Timestamp, tak to ulozime do DB ako long, a az analyzer to interpretuje ako Timestamp
            //return Long.toString(new TimeStamp(buffer.getLong()).getTime()); // ntp timestamp

            default:
                throw new UnsupportedDataTypeException();
        }
    }

    /**
     * Decodes variable sized data types string and octetArray as string encoded
     * in UTF-8 that are contained in ByteBuffer argument into string form,
     * according to IPFIX protocol specification, RFC 5101
     *
     * @param buffer Data to be decoded
     * @return String representation of the data given
     */
    private static Object decodeStringType(ByteBuffer buffer) {
        return new String(buffer.array(), Charset.forName("UTF-8")).trim();
    }

    /**
     * Decodes variable sized data types string and octetArray as string encoded
     * in UTF-8 that are contained in ByteBuffer argument into string form,
     * according to IPFIX protocol specification, RFC 5101
     *
     * @param buffer Data to be decoded
     * @return String representation of the data given
     */
    private static Object decodeOctetArrayType(ByteBuffer buffer) {
        byte[] b = buffer.array();
        return b;
    }

    /**
     * Decodes boolean data type contained in ByteBuffer argument into string
     * form, according to IPFIX protocol specification, RFC 5101
     *
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     */
    private static Object decodeBooleanType(ByteBuffer buffer) throws DataException {
        if (buffer.capacity() != 1) {
            throw new DataException("Cannot decode type boolean: buffer is not 1 bytes long! (" + buffer.capacity() + ")");
        }

        //http://www.ietf.org/mail-archive/web/ipfix/current/msg02673.html true=1, false=2
        // ale RFC5101 nic take nespomina, uvazuje sa true=1, false=0
        byte b = buffer.get();
        switch (b) {
            case 0:
                return Boolean.FALSE;
            case 1:
                return Boolean.TRUE;
            default:
                throw new DataException("The boolean dataType should be only true(1) or false(0), but is: " + b);
        }
    }

    /**
     * Decodes address oriented data types contained in ByteBuffer argument into
     * string form, according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    private static Object decodeAddressType(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {
        switch (type) {
            case "ipv4Address":
                if (buffer.capacity() != 4) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 4 bytes long! (" + buffer.capacity() + ")");
                }
                return buffer.array();
            //return buffer;

            case "ipv6Address":
                if (buffer.capacity() != 16) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 16 bytes long! (" + buffer.capacity() + ")");
                }
                return buffer.array();

            case "macAddress":
                if (buffer.capacity() != 6) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 6 bytes long! (" + buffer.capacity() + ")");
                }
                return buffer.array();

            default:
                throw new UnsupportedDataTypeException("Datatype " + type + " unsupported!");
        }
    }

    /**
     * Decodes floating point data types contained in ByteBuffer argument into
     * string form, according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    private static Object decodeFloatType(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {
        switch (type) {
            case "float32":
                if (buffer.capacity() != 4) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 4 bytes long! (" + buffer.capacity() + ")");
                }
                return new Float(buffer.getFloat());

            case "float64":
                if (buffer.capacity() == 4) {
                    return new Float(buffer.getFloat());
                }
                if (buffer.capacity() != 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is not 8 bytes long! (" + buffer.capacity() + ")");
                }
                return new Double(buffer.getDouble());

            default:
                throw new UnsupportedDataTypeException();
        }
    }

    /**
     * Decodes signed integral data types contained in ByteBuffer argument into
     * string form, according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    private static Object decodeSignedIntegralType(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {
        switch (type) {
            case "signed8":
                if (buffer.capacity() > 1) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 1 byte! (" + buffer.capacity() + ")");
                }
                return new Byte(buffer.get());

            case "signed16":
                if (buffer.capacity() > 2) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 2 bytes! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() == 1) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 2, true);
                    return new Short(fullCapBuffer.getShort());
                }
                return new Short(buffer.getShort());

            case "signed32":
                if (buffer.capacity() > 4) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 4 bytes! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() < 4) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 4, true);
                    return new Integer(fullCapBuffer.getInt());
                }
                return new Integer(buffer.getInt());

            case "signed64":
                if (buffer.capacity() > 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 8 bytes! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() < 8) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 8, true);
                    return new Short(fullCapBuffer.getShort());
                }
                return new Long(buffer.getLong());

            default:
                throw new UnsupportedDataTypeException();
        }
    }

    /**
     * Decodes unsigned integral data types contained in ByteBuffer argument
     * into string form, according to IPFIX protocol specification, RFC 5101
     *
     * @param type IPFIX abstract datatype name
     * @param buffer Data to be decoded
     * @return String representation of the data given
     * @throws BufferUnderflowException when number of bytes given in data
     * buffer is not correct as in RFC 5101
     * @throws UnsupportedDataTypeException when type is something that is not
     * implemented / defined in RFCs
     */
    private static Object decodeUnsignedIntegralType(String type, ByteBuffer buffer) throws DataException, UnsupportedDataTypeException {

        switch (type) {

            case "unsigned8":
                if (buffer.capacity() > 1) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 1 byte! (" + buffer.capacity() + ")");
                }
                return new Short(Support.unsignByte(buffer.get()));

            case "unsigned16":
                if (buffer.capacity() > 2) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 2 bytes! (" + buffer.capacity() + ")");
                }

                if (buffer.capacity() == 1) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 2, false);
                    return new Integer(Support.unsignShort(fullCapBuffer.getShort()));
                }
                // pri normalnej velkosti
                return new Integer(Support.unsignShort(buffer.getShort()));

            case "unsigned32":
                if (buffer.capacity() > 4) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 4 bytes! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() < 4) {
                    //ziskame buffer s plnou kapacitou a vypiseme cislo
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 4, false);
                    return new Long(Support.unsignInt(fullCapBuffer.getInt()));
                }
                //pri normalnej velkosti
                return new Long(Support.unsignInt(buffer.getInt()));

            case "unsigned64":
                if (buffer.capacity() > 8) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 8 bytes! (" + buffer.capacity() + ")");
                }
                if (buffer.capacity() < 8) {
                    ByteBuffer fullCapBuffer = handleReducedSizeEncoding(buffer.array(), 8, false);
                    // surovo nastavime ze je to kladne cislo, a teda dostaneme unsigned 64 cislo
                    //            BigInteger unsignedNumber = new BigInteger(1, fullCapBuffer.array());
                    return new Long(Support.unsignLong(fullCapBuffer.getLong()));
                }

                return new Long(Support.unsignLong(buffer.getLong()));

            case "unsigned128": // NON standard datatype - NOT defined by IETF IPFIX WG
                if (buffer.capacity() > 16) {
                    throw new DataException("Cannot decode type " + type + ": buffer is bigger than 16 bytes! (" + buffer.capacity() + ")");
                }
                byte[] b = buffer.array();
                return b;

            default:
                throw new UnsupportedDataTypeException();
        }
    }

    /**
     * Creates buffer with length corresponding to particular data type maximum
     * length as specified in IPFIX Information Model [RFC5102]. If type is
     * signed and MSB is set, leading bytes are filled with ones. Otherwise and
     * when dealing with unsigned types, leading bytes are filled with zeroes.
     *
     * @param input Input data byte array containing data with reduced size.
     * @param arraySize Output array size, corresponds with IPFIX Information
     * Model specification.
     * @param isSigned Tells if the input should be handled as signed type or
     * not.
     * @return ByteBuffer Buffer containing the full version of type to be
     * decoded
     */
    private static ByteBuffer handleReducedSizeEncoding(byte[] input, int arraySize, boolean isSigned) {
        log.debug("Reduced size encoding is in use!");
        byte[] fullCapacityArray = new byte[arraySize];
        // zistime ci je nastaveny MSB bit - ci je znamienkove cislo zaporne
        boolean isMSBSet = ((0x80 & input[0]) == 0x80) ? true : false;

        int smallIndex = input.length - 1; // index maleho buffra
        // premiestnime vsetky bajty zprava do pola s celkovou kapacitou, bigIndex - index velkeho buffra
        for (int bigIndex = arraySize - 1; bigIndex >= 0; bigIndex--) {
            if (smallIndex < 0) {
                // ak nam dojdu zdrojove data, tak zlava paddujeme 0 alebo 1 ak je zaporne
                fullCapacityArray[bigIndex] = (isSigned && isMSBSet) ? (byte) 0xFF : (byte) 0x00;
            } else {
                fullCapacityArray[bigIndex] = input[smallIndex];
            }
            smallIndex -= 1;
        }

        return ByteBuffer.wrap(fullCapacityArray);
    }

    /**
     * Converts byte array into hexadecimal string
     *
     * @param data data to be converted
     * @return String representation of byte array as hexadecimal string
     */
    public static String getHex(byte[] data) {

        if (data == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * data.length);
        for (final byte b : data) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Converts bytes in byte array into string representation of MAC address.
     * The form is xx:xx:xx:xx:xx:xx.
     *
     * @param data mac address as bytes
     * @return String representation of mac address
     * @throws BufferUnderflowException when the number of bytes in array is not
     * equal to 6.
     */
    public static String parseMacAddress(byte[] data) throws DataException {
        if (data == null) {
            return null;
        }

        if (data.length != 6) {
            // ak nesedi dlzka, oznamme to vyssie
            throw new DataException("Length of mac Address is not 6 bytes (" + data.length + ")");
        }

        final StringBuilder hex = new StringBuilder(20); // treba nam 17+1, vytvorime pre 20

        for (final byte octet : data) {
            hex.append(HEXES.charAt((octet & 0xF0) >> 4)); // high word
            hex.append(HEXES.charAt((octet & 0x0F))); // low word
            hex.append(':');
        }
        // odstranime poslednu dvojbodku
        hex.deleteCharAt(hex.length() - 1);
        return hex.toString();
    }
}
