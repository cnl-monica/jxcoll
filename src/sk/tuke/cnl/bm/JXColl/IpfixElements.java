/* Copyright (C) 2010 Tomas Verescak
 *
 * This file is part of JXColl.
 *
 * JXColl is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JXColl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JXColl; If not, see <http://www.gnu.org/licenses/>.
 *
 * Fakulta Elektrotechniky a informatiky
 * Technicka univerzita v Kosiciach
 *
 * Zhromazdovaci proces nastroja BasicMeter
 * Bakalarska praca
 *
 * Veduci BP/DP: Ing. Juraj Giertl, PhD.
 * Konzultant BP/DP: Ing. Martin Reves
 *
 * Bakalarant/Diplomant: Tomas Verescak
 *
 * Zdrojove texty:
 * Subor: IpfixFields.java
 */
package sk.tuke.cnl.bm.JXColl;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;

/**
 * Class provides functions for easy retrieval of useful data about IPFIX information elements,
 * Class parses XML document (usually named ipfixFields.xml) into memory and saves data about particular
 * IPFIX elements into HashMap collection with mapping of ID of information element onto object of type
 * IpfixFieldAttributes, which contains details as name, datatype, group and ID.
 * @author Tomas Verescak
 */
public final class IpfixElements {

    /** Stores IPFIX fields with their IDs as keys */
    private HashMap<FieldKey, IpfixField> fieldsByID = new HashMap<>(initialMapCapacity);
    /** Stores IPFIX fields with their names as keys */
    private HashMap<String, IpfixField> fieldsByName = new HashMap<>(initialMapCapacity);
    private static Logger log = Logger.getLogger(IpfixElements.class.getName());
    private static final int initialMapCapacity = 350;
    private static IpfixElements instance = new IpfixElements();
    private static boolean fileloaded;

    /**
     * Serves for other threads which may want to make sure that XML document was loaded successfully.
     * @return true, if file was loaded successfully, false otherwise.
     */
    public static boolean isFileLoaded() {
        return fileloaded;
    }

    /**
     * Gets an instance of this class. Only one instance is permitted (singleton).
     * @return instance
     */
    public static IpfixElements getInstance() {
        return instance;
    }

    /**
     * Constructor cannot be instantiated directly by constructor - new IpfixElements().
     * Method getInstance() must be used instead.
     */
    private IpfixElements() {
        try {

            File file = new File(Config.XMLFile);
            if (file.exists()) {
                // loads xml document into memory
                log.info("Generating in-memory XML document from: " + Config.XMLFile);
                Document doc = parseXML(file);
                // retrieves data from XML document and saves it into HashMap
                retrieveDataFromXML(doc);
                fileloaded = true;

//                printAllFields();
//                printBeemSupportedFields();

            } else {
                log.fatal("XML file \"" + Config.XMLFile + "\" was not found!");
                fileloaded = false;
            }

        } catch (FileNotFoundException fnf) {
            log.fatal("File \"" + Config.XMLFile + "\" was not found!");
            fileloaded = false;
//            JXColl.stop();
        } catch (Exception e) {
            log.fatal(e);
//            e.printStackTrace();
            fileloaded = false;
//            JXColl.stop();
        }


    }

    /**
     * Gets name of information element which corresponds to given ID.
     * @param elementId information element ID.
     * @return String information element name.
     */
    public String getElementName(int elementId, long enterpriseNumber) {
        return fieldsByID.get(new FieldKey(elementId, enterpriseNumber)).getName();
    }

//    /**
//     * Gets IPFIX field's ID which name is passed to the method.
//     * @param elementName
//     * @return int ID of given IPFIX element, value of -1 when not found
//     */
//    public int getElementID(String elementName) {
//       Collection<IpfixField> values = fieldsByID.values();
//        // search over all fields that ipfixFields.xml contains
//        for (IpfixField field : values) {
//            if(field.getName().equals(elementName)) {
//                return field.elementID;
//            }
//        }
//        return -1;
//    }
    /**
     * Gets IPFIX field's ID which name is passed to the method.
     * @param elementName
     * @return int ID of given IPFIX element, value of -1 when not found
     */
    public int getElementID(String elementName) {
        return fieldsByName.get(elementName).getElementID();
    }

    /**
     * Gets datatype of information element which corresponds to given ID.
     * @param elementId information element ID.
     * @return String information element datatype.
     */
    public String getElementDataType(int elementId, long enterpriseId) {
        return fieldsByID.get(new FieldKey(elementId, enterpriseId)).getDataType();
    }

    /**
     * Gets group information element belongs to, corresponding to given ID.
     * @param elementId information element ID.
     * @return String information element group.
     */
    public String getElementGroup(int elementId, long enterpriseNumber) {
        return fieldsByID.get(new FieldKey(elementId, enterpriseNumber)).getGroup();
    }

    /**
     * Determines if there is entry with given ID in XML document.
     * @param elementId information element ID.
     * @return true if it exists, false otherwise.
     */
    public boolean exists(int elementId, long enterpriseNumber) {
        return fieldsByID.containsKey(new FieldKey(elementId, enterpriseNumber));
    }

    public boolean isBeemSupported(int elementId, long enterpriseNumber) {
        return fieldsByID.get(new FieldKey(elementId, enterpriseNumber)).isBeemSupported();
    }

    /**
     * Prints information about fields currently supported by BEEM and ACP
     */
    public void printBeemSupportedFields() {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        Collection<IpfixField> fields = fieldsByID.values();
        for (IpfixField field : fields) {
            if (field.isBeemSupported() == true) {
                sb.append(field.getElementID() + " -> " + field.getName() + " (" + field.getDataType() + ")\n");
                if (++counter % 10 == 0) {
                    sb.append("\n");
                }
            }
        }

        log.debug("BEEM supported IPFIX elements (" + counter + "): \n" + sb.toString());
    }

    /**
     * Prints information about fields currently supported by BEEM and ACP
     */
    public void printAllFields() {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        Collection<IpfixField> fields = fieldsByID.values();
        for (IpfixField field : fields) {
            sb.append(field.getElementID() + " -> " + field.getName() + " (" + field.getDataType() + ")\n");
            if (++counter % 10 == 0) {
                sb.append("\n");
            }
        }

        log.debug("All IPFIX elements (" + counter + "): \n" + sb.toString());
    }

    /**
     * Data parsed by DOM + XPath is inserted into collection of type HashMap,
     * in order to have quicker access to particular information element
     * attributes. Key value of HashMap is Integer - IPFIX information element
     * identifier, value is object of type IpfixFieldAttributes. It contains
     * only information used by JXColl - name, group, datatype, elemetId.
     * @return HasMap object with loaded data about IPFIX information elements.
     */
    public void retrieveDataFromXML(Document doc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        NodeList nodeList = null;
        String name = null;
        String dataType = null;
        String group = null;
        long enterpriseNumber = 0;
        int elementID = 0;
        boolean beemSupported = false;

        // HashMap is faster than ConcurrentHashMap. So if you aren't accessing (writing) data from multiple threads, 
        // or if the Map's data doesn't change at all after initialization, then you would usually want to use HashMap.
//        HashMap<Integer, IpfixField> fields = new HashMap<Integer, IpfixField>(initialMapCapacity);

        nodeList = (NodeList) xpath.evaluate("//field", doc, XPathConstants.NODESET);

        // search every field
        for (int i = 0; i < nodeList.getLength(); i++) {
            // select element <field> with index i
            Node node = nodeList.item(i);
            // auxiliar object used for XPath results
            Node result = null;

            result = ((Node) xpath.evaluate("@name", node, XPathConstants.NODE));
            name = result.getNodeValue().trim();

            result = ((Node) xpath.evaluate("@dataType", node, XPathConstants.NODE));
            dataType = result.getNodeValue().trim();

            result = ((Node) xpath.evaluate("@group", node, XPathConstants.NODE));
            group = result.getNodeValue().trim();

            result = ((Node) xpath.evaluate("@elementId", node, XPathConstants.NODE));
            elementID = (int) Integer.parseInt(result.getNodeValue().trim());

            result = ((Node) xpath.evaluate("@beemSupported", node, XPathConstants.NODE));
            beemSupported = Boolean.parseBoolean(result.getNodeValue().trim());

            result = ((Node) xpath.evaluate("@enterpriseID", node, XPathConstants.NODE));
            enterpriseNumber = (result == null) ? 0 : Long.parseLong(result.getNodeValue().trim());

            //   log.debug("Enterprise number = " + enterpriseNumber);

            // create aux object and insert it into HashMap - if it does not exist yet
            IpfixField field = new IpfixField(name, dataType, group, elementID, beemSupported, enterpriseNumber);
            FieldKey key = new FieldKey(elementID, enterpriseNumber);
            if (!fieldsByID.containsKey(key)) {
                fieldsByID.put(key, field);
                fieldsByName.put(name, field);
            } else {
                log.warn("IPFIX info element with id=" + elementID + " already exists, check ipfixFields.xml for duplicate data.");
            }
        }

    }

    /**
     * Parses given XML file and loads it into memory.
     * @param file file to be parsed
     * @return org.w3c.DOM.Document - in-memory representation of XML file
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public Document parseXML(File file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(file);

    }

    public class FieldKey {

        private int id;
        private long enterpriseId;

        public FieldKey(int id, long enterpriseId) {
            this.id = id;
            this.enterpriseId = enterpriseId;
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldKey other = (FieldKey) obj;
            if (this.id != other.id) {
                return false;
            }
            if (this.enterpriseId != other.enterpriseId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.id;
            hash = 47 * hash + (int) (this.enterpriseId ^ (this.enterpriseId >>> 32));
            return hash;
        }

        public int getId() {
            return id;
        }

        public long getEnterpriseId() {
            return enterpriseId;
        }
    }

    /**
     * Aux class which stores information about information element:
     * name, datatype, group and ID.
     */
    public class IpfixField implements Comparable<IpfixField> {

        private String name;
        private String dataType;
        private String group;
        private int elementID;
        private boolean beemSupported;
        private boolean isEnterprise;
        private long enterpriseNumber;

//        public IpfixField(String name, String dataType, String group, int elementID, boolean beemSupported) {
//            this.name = name;
//            this.dataType = dataType;
//            this.group = group;
//            this.elementID = elementID;
//            this.beemSupported = beemSupported;
//        }

        public IpfixField(String name, String dataType, String group, int elementID, boolean beemSupported, long enterpriseNumber) {
//            this(name, dataType, group, elementID, beemSupported);
            this.name = name;
            this.dataType = dataType;
            this.group = group;
            this.elementID = elementID;
            this.beemSupported = beemSupported;
            this.isEnterprise = (enterpriseNumber != 0) ? true : false;
            this.enterpriseNumber = enterpriseNumber;
        }

        /**
         * Gets information element datatype
         * @return String datatype
         */
        public String getDataType() {
            return dataType;
        }

        /**
         * Gets information element ID
         * @return int ID
         */
        public int getElementID() {
            return elementID;
        }

        /**
         * Gets information element group name
         * @return String group name
         */
        public String getGroup() {
            return group;
        }

        /**
         * Gets information element name
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Tells whether this element is supported by BEEM
         * @return the beemSupported
         */
        public boolean isBeemSupported() {
            return beemSupported;
        }

        public long getEnterpriseNumber() {
            return enterpriseNumber;
        }

        public void setEnterpriseNumber(long enterpriseNumber) {
            this.enterpriseNumber = enterpriseNumber;
        }

        public boolean isIsEnterprise() {
            return isEnterprise;
        }

        public void setIsEnterprise(boolean isEnterprise) {
            this.isEnterprise = isEnterprise;
        }

        public int compareTo(IpfixField o) {
            if (elementID > o.getElementID()) {
                return 1;
            } else if (elementID == o.getElementID()) {
                return 0;
            } else {
                return -1;
            }
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IpfixField other = (IpfixField) obj;
            if (this.elementID != other.elementID) {
                return false;
            }
            if (this.enterpriseNumber != other.enterpriseNumber) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.elementID;
            hash = 37 * hash + (int) (this.enterpriseNumber ^ (this.enterpriseNumber >>> 32));
            return hash;
        }
    }
}
