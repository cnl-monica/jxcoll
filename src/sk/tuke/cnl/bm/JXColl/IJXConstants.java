package sk.tuke.cnl.bm.JXColl;

/* 
 * Copyright (C) 2010 Lubos Kosco
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
/**
 * Interface with intern variables. Most of them influence performance of whole
 * function of program.
 */
public interface IJXConstants {


    /* ---------------------- ACP connection ---------------------- */

    
    /**
     * The number of threads spawned for ACP connection handling
     * (max separate parallel DC connections handled at one time).
     * Default value = 5.
     */
    public static final int NUMBER_OF_ACP_THREADS = 5;
    /**
     * ACPIPFIXWorker thread out cache size (default value = 25).
     */
    public static final int THREAD_CACHE_SIZE = 25;
    /** 
     * If keep-alive mechanism of the connection between analyzer & JXColl
     * should be enabled
     */
    public static final boolean SET_ACP_KEEPALIVE = true;
    /** 
     * Time interval in ms the ACPIPFIXWorker periodically sleeps while waiting
     * for incoming data (minimal response of flow sent by ACP).
     * Recommended value is 5 ms. Anything lower than 3 or greater than 10 
     * could result in poor performance or heavy active waiting using CPU.
     */
    public static final int SEND_FLOW_FREQUENCY = 3;


    /* ---------------------- DB connection ---------------------- */


    /** 
     * PostgreSQL driver name
     */
    public static final String DB_DRIVER_POSTGRESQL = "org.postgresql.Driver";
    /** 
     * URL prefix when accessing PosgreSQL database
     */
    public static final String DB_URL_PREFIX_POSTGRESQL = "jdbc:postgresql://";
    /** 
     * MySQL driver name - not used in present.
     */
    public static final String DB_DRIVER_MYSQL = "org.gjt.mm.mysql.Driver";
    /** 
     * URL prefix when accessing MySQL database - not used in present.
     */
    public static final String DB_URL_PREFIX_MYSQL = "jdbc:mysql://";


    /* ---------------------- Incoming packet cache ---------------------- */


    /** 
     * Maximum number of packets stored in cache
     */
    public static final int INPUT_QUEUE_SIZE = 25;
    /** 
     * Maximum size of buffer for single packet data
     */
    public static final int INPUT_BUFFER_SIZE = 65540;
    // stale aspon o 4 byte vacsie pls ako max, kedze seeking po nf9 flowe,
    // kt. nepoznam je rieseny prave cez to, kedze sa nepredava plna velkost dat


    /* ---------------------- NetFlow 9 / IPFIX ---------------------- */


    /** 
     * Template timeout in seconds - time after template is considered
     * expired and should not be used for decoding flows
     */
    public static final int TEMPLATE_TIMOUT = 10 * 60;
    /**
     * Size of output cache.
     */
    public static final int OUTPUT_CACHE_SIZE = 100;
}
