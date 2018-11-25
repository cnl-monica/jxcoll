/* 
 * Copyright (C) 2010 Lubos Kosco, Michal Kascak
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
package sk.tuke.cnl.bm.JXColl.export;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import sk.tuke.cnl.bm.JXColl.IJXConstants;
import sk.tuke.cnl.bm.JXColl.Config;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * <p>
 * Title: JXColl</p>
 *
 * <p>
 * Description: Java XML Collector for network protocols</p>
 *
 * <p>
 * Copyright: Copyright (c) 2005</p>
 *
 * <p>
 * Company: TUKE, FEI, CNL</p>
 * Implementation of access to MONGO database
 *
 * @author Lubos Kosco, Marek Marcin
 * @version 0.2
 */
public class MongoClient extends DBExport {

    private static Logger log = Logger.getLogger(DBExport.class.getName());
    private DB db;
    private int rowcount;

    /**
     * MongoClient default constructor
     */
    public MongoClient() {
        super(/*IJXConstants.DB_DRIVER_POSTGRESQL*/);
        try {
            this.dbConnect();
        } catch (Exception ex) {
            log.error(ex);
 //           JXColl.stop(); 
        }

    }

    /**
     * connect to database
     */
    public void dbConnect() {
        String dbLoginHostField = Config.dbHost;
        String dbLoginPortField = Config.dbPort;
        String dbLoginNameField = Config.dbName;
        String dbLoginUsernameField = Config.dbLogin;
        String dbLoginPasswordField = Config.dbPassword;
        super.connect(dbLoginHostField, dbLoginPortField, dbLoginNameField, dbLoginUsernameField, dbLoginPasswordField);
        db = getDb();
    }

    /**
     * insert data into database
     *
     * @param tabname String name of table
     * @param s String values for a row
     */
    public void insertdata(String tabname, String json) {
        ResultSet rs;

        /**
         * @todo kontrola na to, ci je spojenie platne, ak nemame spojenie,
         * pokus sa o znovupripojenie
         */
        //st=super.query();
        //String s = "" ;
        if (isConnected()) {
            DBCollection collection = db.getCollection(tabname);
            DBObject dbObject = (DBObject) JSON.parse(json);
            collection.insert(dbObject);
        }
    }

    //Added by Michal Kascak
    //Rewrite to MongoDB by Marek Marcin
    public void insertData(String tabname, String[] columns, Object[] values) {
        if (columns.length != values.length) {
            log.error("Corrupted insert statement (columns and values mismatch)");
            return;
        }
        DBCollection collection = db.getCollection(tabname);                    //tabulka v MongoDB
        BasicDBObject dbObject = new BasicDBObject();

        for (int i = 0; i < columns.length; i++) {
            //log.debug("MongoClient117_APPEND>>  c: " + columns[i] + "   v: " + values[i]);
            dbObject.append(columns[i], values[i]);
        }

        //System.out.println(sql.toString());
        if (!isConnected()) {
            log.info("Non-existing DB connection while exporting. Connecting...");
            dbConnect();
        }
        WriteResult result = collection.insert(dbObject);                       //samotny INSERT do databazy
        if (result.getError() == null) {
            //log.info(columns.length + " IPFIX fields was stored in table: " + tabname);
        } else {
            log.error("Data was NOT stored in table: " + tabname + ". WriteResult: " + result);
        }
    }

//    /*
//     * TODO Prerobit na MONGO
//     */
//    public long getCurrentSequenceNumber(String sequence) {
//        long currValue = 0;
//        if (isConnected()) {
//            ResultSet rs = st.executeQuery("Select currval('" + sequence + "')");
//            if (rs.next()) {
//                currValue = rs.getLong(1);
//            }
//        }
//        return currValue;
//    }
//
//    /*
//     * TODO prerobit na MONGO
//     */
//    public long getNextSequenceNumber(String sequence) {
//        long nextValue = 0;
//        if (isConnected()) {
//            ResultSet rs = st.executeQuery("Select nextval('" + sequence + "')");
//            if (rs.next()) {
//                nextValue = rs.getLong(1);
//            }
//        }
//        return nextValue;
//    }
    /**
     * If sequence no exist, the new sequence is created Take care, Sequences
     * are in collection "counters"
     *
     * @param seq_name The name of Sequence
     * @return next value of the Sequence
     */
    public double getNextSequenceNumber(String seq_name) {
        String sequence_collection = "counters"; // the name of the sequence collection
        String sequence_field = "seq"; // the name of the field which holds the sequence

        DBCollection seq = db.getCollection(sequence_collection); // get the collection (this will create it if needed)

        // this object represents your "query", its analogous to a WHERE clause in SQL
        DBObject query = new BasicDBObject("_id", seq_name); // where _id = the input sequence name

        // this object represents the "update" or the SET blah=blah in SQL
        DBObject update = new BasicDBObject("$inc", new BasicDBObject(sequence_field, 1)); // the $inc here is a mongodb command for increment

        // Atomically updates the sequence field and returns the value for you
        DBObject res = seq.findAndModify(query, new BasicDBObject(), new BasicDBObject(), false, update, true, true);

        return Double.parseDouble(res.get(sequence_field).toString());
    }

    /**
     * The sequence has to be created earlier. If the sequence not exist, error
     * occurs Take care, Sequences are in collection "counters"
     *
     * @param seq_name the name of Sequence
     * @return current value of the Sequence
     */
    public double getCurrentSequenceNumber(String seq_name) {
        String sequence_collection = "counters"; // the name of the sequence collection
        String sequence_field = "seq"; // the name of the field which holds the sequence

        DBCollection seq = db.getCollection(sequence_collection); // get the collection (this will create it if needed)

        // this object represents your "query", its analogous to a WHERE clause in SQL
        DBObject query = new BasicDBObject("_id", seq_name); // where _id = the input sequence name

        DBObject res = seq.findOne(query);
        log.debug("MONGO SEQUENCE RESULT 1: " + res);

        if (res == null) {
            log.debug("MONGO SEQUENCE RESULT 2:"+seq.insert(new BasicDBObject("_id", seq_name).append(sequence_field, 0)));
            return 0d;
        }

        return Double.parseDouble(res.get(sequence_field).toString());
    }

}
