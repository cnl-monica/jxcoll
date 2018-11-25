/*
 * Copyright (C) 2010 Lubos Kosco, Marek Marcin
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

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.sql.*;

import org.apache.log4j.*;
import sk.tuke.cnl.bm.JXColl.Config;
import sk.tuke.cnl.bm.JXColl.JXColl;

/**
 * Abstract database access layer
 */
public abstract class DBExport {

    private static Logger log = Logger.getLogger(DBExport.class.getName());
    //private Connection db;
    private MongoClient mongo;
    private DB db;
    private String dbString;
    private volatile boolean connected = false;
    public int verboseLevel = 0;

    /**
     * Set level of logging for this class
     *
     * @param level String Log Level
     */
    public void setlogl(String level) {
        log.setLevel(org.apache.log4j.Level.toLevel(level));
    }

    /**
     * construct a DBExport for a database accessed by the driver
     *
     * @param driver String database driver (URL)
     */
    public DBExport(/*String driver*/) {
///PRE MONGO TO NETREBA
//        try {
//            Class.forName(driver);
//        } catch (ClassNotFoundException e) {
//            log.error(e.getMessage());
//        } //try-catch

    } //DB()

    /**
     * Check whether the connection to database exists
     *
     * @return boolean true if yes, false if no
     */
    public boolean isConnected() {
        /**
         * @todo lepsiu kontrolu a schopnost znovu pripojenia ak stratime
         * connection
         */
        try {
            log.info("NAMES: "+mongo.getDatabaseNames());
            
//AUTH->ON            if (db.isAuthenticated()) {
            connected = true;           
            return connected;
//AUTH->ON            }
        } catch (java.lang.NullPointerException | MongoException npex) {
            log.fatal("Check if is DB connected failed: " + npex);
            connected = false;
            return connected;
        }
    }

    /**
     * Connection to database.
     * @param host String host
     * @param port String  port
     * @param name String database name
     * @param username String username
     * @param password String password
     */
    public void connect(String host, String port, String name, String username, String password) {

        try {
//            dbString = username + "@" + url;
            mongo = new MongoClient(host, Integer.parseInt(port));
            // GET DB
            db = mongo.getDB(name);
            
            log.info("NAMES: "+mongo.getDatabaseNames());
            
            // AUTHENTICATION
//AUTH->ON            boolean auth = db.authenticate(username, password.toCharArray());
            log.info("Connecting to MongoDB " + host + ":" + name);
//AUTH->ON            if (Boolean.TRUE.equals(auth)) {
            log.info("Connection to DB successfully established.");
            connected = true;
//AUTH->ON            }
        } catch (UnknownHostException | MongoException ex) {
            log.fatal(ex + " Probably caused by failed database connection. Check database settings and connection.");
            java.util.logging.Logger.getLogger(DBExport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex  + " Probably caused by failed database connection. Check database settings and connection.");
            connected = false;
            Config.doPGexport = false;
        }
    }

    /**
     * Close the connection to the database.
     */
    public void disconnect() {
        mongo.close();
        connected = false;
        log.debug("Disconnected from MongoDB " +db.getMongo().getAddress().getHost()+":"+ db.getName());
    }

    /**
     * Database getter
     * @return the db
     */
    public DB getDb() {
        return db;
    }

// PRE MONGO TO NETREBA    
//    /**
//     * create a query for querying the database
//     *
//     * @return Statement
//     */
//    public Statement query() {
//
//        if (isConnected()) {
//            try {
//                Statement st = db.createStatement();
//                return st;
//            } catch (SQLException e) {
//                log.error(e.getMessage());
//                return null;
//            } //try-catch
//        }
//        return null;
//    } //query()
//    public void export() {
//        throw new UnsupportedOperationException();
//    }
//
//    public void init() {
//        throw new UnsupportedOperationException();
//    }
}
