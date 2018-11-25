/*
 * Copyright (C) 2010 Lubos Kosco, Adrian Pekar, Tomas Verescak, Marek Marcin
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

import sk.tuke.cnl.bm.JXColl.input.UDPProcessor;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.net.SocketTimeoutException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.FixedWindowRollingPolicy;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.SizeBasedTriggeringPolicy;
import sk.tuke.cnl.bm.JXColl.export.*;
import sk.tuke.cnl.bm.JXColl.OWD.OWDFlushCacheABThread;
import sk.tuke.cnl.bm.JXColl.OWD.Synchronization;
import sk.tuke.cnl.bm.JXColl.input.SCTPServer;
import sk.tuke.cnl.bm.JXColl.input.TCPServer;
import sk.tuke.cnl.bm.JXColl.input.UDPServer;

/**
 * This is the main class, it instantiates all classes & threads needed
 */
public class JXColl {

    private static Logger log = Logger.getLogger(JXColl.class.getName());
    /** Config Object reference */
    public static Config cfg;
    /** Object thread for ACPServer */
    public static ACPServer acpserver = null;
    /** Object thread for PacketListener */
//    protected static Receiver receiver = null;
    private static UDPServer udpServer = null;
    private static TCPServer tcpServer = null;
    private static SCTPServer sctpServer = null;
    /** Object thread for NetXMLParser */
    private static UDPProcessor udpProcessor = null;
    /** JXColl Version */
    private static final String VERSION = "4.0.1";
    private static final long MAX_LOGFILE_SIZE = 100000000; // in bytes
    protected static Synchronization synch = null;
    protected static OWDFlushCacheABThread owd = null;

    /**
     * The main method of whole collector, responsible for reading config file &
     * starting threads
     * @param args
     *            String[] - parameters of command line
     */
    public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException {

        ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%-5p [%t] %c{1} %d{ABSOLUTE} - %m%n")); //detailed info
//        ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%-5p %c{1} %d{ABSOLUTE} - %m%n")); //detailed info
        BasicConfigurator.configure(consoleAppender);

        if (args.length < 1) {
            System.err.println("Please provide a config file (e.g. java -jar jxcoll.jar jxcoll_config.xml)");
            // System.exit(1); //if no config file is given, try to find the default one
        }

        String configFileArg = null;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("--logtofile")) {
                //output to logfile and keep default config file (keep null)
                loggingToFile();
            } else {
                // set config file from argument
                configFileArg = args[0];
            }
        }

        log.info("Starting JXColl v" + VERSION);

        // logging to file?
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("--logtofile")) {
                loggingToFile();

            } else {
                log.warn("Second argument is wrong! Logs are output to console");
            }
        }



//        new TimeTest().testik();

        // catching ctrl+c shortcut
        Thread shutdownHook = new Thread("ShutDown Hook") { // by Tomas Verescak

            @Override
            public void run() {
                JXColl.stopJXColl();
                log.info("(: byebye :)");
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);


        // load data from config file
        Config.loadData(configFileArg);

        log.info("Setting log level: " + Config.logl);
        Logger.getRootLogger().setLevel(Level.toLevel(Config.logl));

        if (!IpfixElements.isFileLoaded()) { //  by Tomas Verescak
            log.fatal("JXColl could not start because of an error while processing XML file!");
            return;
        }

        log.info("Providing PacketCache for " + IJXConstants.INPUT_QUEUE_SIZE + " packets.");


        //---------------------- Synchronization --------------------------

        if (Config.makeSync) {
            try {

                log.info("Starting Synchronization.");
                synch = new Synchronization(Config.lsyncport);
                synch.start();
            } catch (IOException ex) {
                if (ex instanceof BindException) {
                    log.fatal("Listen sync. port " + Config.lsyncport + " is already used, another instance of JXColl might be running!");
                } else {
                    log.fatal(ex);
                }
                log.info("Shutting down JXColl");
                // bypasses successive calls and invokes shutdownHook;
                return;
            }
        }

        // input , collect flows
        try {

            // run desired receiver threads
            if (Config.receiveUDP) {
                udpServer = new UDPServer(Config.lport);
                udpServer.start();
            }
            if (Config.receiveTCP) {
                tcpServer = new TCPServer(Config.lport);
                tcpServer.start();
            }
            if (Config.receiveSCTP) {
                sctpServer = new SCTPServer(Config.lport);
                sctpServer.start();
            }


        } catch (IOException e) {
            if (e instanceof BindException) {
                log.fatal("Listen port " + Config.lport + " is already used, another instance of JXColl might be running!");
            } else if (e instanceof SocketTimeoutException) {
                log.fatal("Waiting for TCP connection establishement (" + Config.DEFAULT_TCP_CONNECTION_TIMEOUT + " seconds) timed out. Shutting down JXColl.");
            } else {
                log.fatal(e);
            }
            log.info("Shutting down JXColl");
            // bypasses successive calls and invokes shutdownHook;
            return;
        }

        // output, ACP Connect
//		if (Config.doACPTransfer) {
//			try {
//				acpserver = new ACPServer(Config.acpport);
//				acpserver.start();
//			} catch (IOException e) {
//				log.error("ACP server could not start because of an "
//						+ e.getClass() + " : " + e);
//				stop();
//			}
//		}


        // parser, preparing & calling output ; ALWAYS CALL AT THE END OF MAIN
        // BECAUSE OF PRECEEDING CALLS TO EXPORT MODULES !!!!


        udpProcessor = new UDPProcessor();
        udpProcessor.start();

        //ak neni co robit tak skonc
        if (!Config.doPGexport && !Config.doACPTransfer && !Config.doPGAccExport) {
            log.info("There is nothing to do! All export methods are turned off!");
            stopJXColl();
        }

        //---------------------- OWD --------------------------
        if (Config.measureOwd) {
            log.info("Starting OWD FlushCache Thread.");
            owd = new OWDFlushCacheABThread();
            owd.start();
        }

        log.info("Press Ctrl + c to kill me!");


    }

    /**
     * Function stops a thread and waits for it to finish
     * @see http://www.javaspecialists.co.za/archive/Issue056.html
     */
    public static void interruptThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            log.info(String.format("Shutting down %s Thread...", thread.getName()));
            thread.interrupt();

            log.debug(String.format("Waiting for %s thread to die!", thread.getName()));
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            log.info(String.format("...%s thread died!", thread.getName()));
        }
    }

    /**
     * Function stops all threads and exits the program
     * @see http://www.javaspecialists.co.za/archive/Issue056.html
     */
    public static void stopJXColl() {
        log.info("Received request to kill JXColl, starting cleaning process ...");

        interruptThread(udpServer);
        interruptThread(tcpServer);
        interruptThread(sctpServer);

        // cakame kym sa nevyprazdni vstupna cache
        while (PacketCache.getSize() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        
        interruptThread(udpProcessor);
        interruptThread(acpserver);
        RecordDispatcher.getInstance().closeDBConnection();
        interruptThread(owd);
        interruptThread(synch);

        //kedze sme vsetko nase povypinali spravne, ale ked je nejaka ina zavada.. vypneme nasilu lebo nase uz nic neni
        Runtime.getRuntime().halt(0);
    }

    private static void loggingToFile() {
        try {
            // make file in the proper directory
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String timestamp = formatter.format(new Date(System.currentTimeMillis()));
            String path = "/var/log/jxcoll/" + timestamp + "/jxcoll.log";
            log.info("Creating log file in: " + path);
            File logFile = new File(path);
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
            // create file appender
            RollingFileAppender logFileAppender = new RollingFileAppender();
            logFileAppender.setTriggeringPolicy(new SizeBasedTriggeringPolicy(MAX_LOGFILE_SIZE)); //100 MB = 100*2^20
            logFileAppender.getTriggeringPolicy().activateOptions();
            logFileAppender.setLayout(new PatternLayout("[%-5p][%t][%c{2}] %m%n"));
            logFileAppender.setFile(logFile.getPath());
            // set what should happen - gzip maximum of 10 files
            FixedWindowRollingPolicy rPolicy = new FixedWindowRollingPolicy();
            rPolicy.setMaxIndex(10);
            rPolicy.setFileNamePattern(path + ".%i.gz");
            logFileAppender.setRollingPolicy(rPolicy);
            // activate all the settings
            logFileAppender.activateOptions();
            log.info("Rest of messages will be in log file!");
            // set logging to console
            BasicConfigurator.resetConfiguration();
            BasicConfigurator.configure(logFileAppender);

        } catch (IOException ex) {
            log.fatal("Could not create log file: " + ex.getMessage());
            log.info("Do you have privileges to create logfile here?");
            System.exit(1);
        }
    }
}
