package net.paoding.analysis.dictionary.support.detection;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */
public class Detection implements Runnable {

	private Log log = LogFactory.getLog(this.getClass());

	private DifferenceListener listener;

	private File home;

	private FileFilter filter;

	private long interval;

	private Snapshot lastSnapshot;
	
	private Thread thread;

	private boolean alive = true;

	public void setListener(DifferenceListener listener) {
		this.listener = listener;
	}

	public Detection() {
	}

	/**
	 * 检查间隔
	 * 
	 * @param interval
	 */
	public void setInterval(int interval) {
		this.interval = interval * 1000;
	}

	public void setHome(File home) {
		this.home = home;
	}

	public void setHome(String home) {
		this.home = new File(home);
	}

	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}
	
	public Snapshot flash(){
		return Snapshot.flash(home, filter);
	}

	public void start(boolean daemon) {
		if (lastSnapshot == null) {
			lastSnapshot = flash();
		}
		thread = new Thread(this);
		thread.setDaemon(daemon);
		thread.start();
	}
	
	
	public Snapshot getLastSnapshot() {
		return lastSnapshot;
	}
	
	public void setLastSnapshot(Snapshot last) {
		this.lastSnapshot = last;
	}

	public void run() {
		if (interval <= 0)
			throw new IllegalArgumentException(
					"should set a interval(>0) for the detection.");
		while (alive) {
			sleep();
			forceDetecting();
		}
	}

	public void forceDetecting() {
		Snapshot current = flash();
		Difference diff = current.diff(lastSnapshot);
		if (!diff.isEmpty()) {
			try {
				if (listener.on(diff)) {
					log.info("found differen for " + home);
					log.info(diff);
					lastSnapshot = current;
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	public void setStop() {
		alive = false;
		thread = null;
	}

	private void sleep() {
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Detection d = new Detection();
		d.setInterval(1);
		d.setHome(new File("dic"));
		d.setFilter(new ExtensionFileFilter(".dic"));
		d.setListener(new DifferenceListener() {
			public boolean on(Difference diff) {
				System.out.println(diff);
				return true;
			}

		});
		d.start(false);
	}

}
