package sk.tuke.cnl.bm.JXColl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Vlakno sluzi zatial len na Debugovacie ucely, neskor sa planuje pouzit na
 * korektne vypnutie JXColl.
 * 
 * @author Tomas Verescak
 */
public class Debug extends Thread {
	private static Logger log = Logger.getLogger(Debug.class.getName());
	private volatile String key;

	public Debug() {
		super("Debug");
		setlogl("DEBUG");

	}

	public void setlogl(String level) {
		this.log.setLevel(org.apache.log4j.Level.toLevel(level));
	}

	/**
	 * Method running all the time.
	 */
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean beginning = true;
		while (!interrupted()) {
			log.debug("som na zaciatku");
			key = null;
			// na zaciatku cakaj, mozno je zla cesta k ipfixFields.xml
			
			try {
				wait();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			if (beginning) {
				try {
					Thread.sleep(2000);
					beginning = false;
				} catch (InterruptedException e1) {	
					break;
				}
			}
			try {
				key = in.readLine();
			} catch (IOException e) {
				if (interrupted()) {
					interrupt();
				} else {
					e.printStackTrace();
				}
			}
			if (key == null) {
				continue;
			}

			if (key.equals("m") || key.equals("M")) {
				long heapSize = Runtime.getRuntime().totalMemory()
						/ (1024 * 1024);
				long heapMaxSize = Runtime.getRuntime().maxMemory()
						/ (1024 * 1024);
				long heapFreeSize = Runtime.getRuntime().freeMemory()
						/ (1024 * 1024);
				long heapUsedSize = heapSize - heapFreeSize;

				log.debug("HEAP--> TOTAL: " + heapSize + " MB | MAX: "
						+ heapMaxSize + " MB | FREE: " + heapFreeSize
						+ " MB | USED: " + heapUsedSize + " MB");

			}
			if (key.equals("q") || key.equals("Q")) {
				log.debug("Packets in cache: " + PacketCache.getSize());

			}
			if (key.equals("e") || key.equals("E")) {
				log.info("Shutting down Threads!");
				JXColl.stopJXColl();
				interrupt();
				// log.debug("stop");
				// interrupt();
			}

		}
		// log.info("Debug died!");

	}
}
