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

	private Snapshot last;

	public void setListener(DifferenceListener listener) {
		this.listener = listener;
	}
	
	public Detection() {
	}
	
	/**
	 * 检查间隔
	 * @param interval 
	 */
	public void setInterval(int interval) {
		this.interval = interval * 1000;
	}

	public void setHome(File home) {
		this.home = home;
	}

	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}

	public void start(boolean daemon) {
		last = Snapshot.flash(home, filter);
		Thread t = new Thread(this);
		t.setDaemon(daemon);
		t.start();
	}

	public void run() {
		if (interval <= 0) 
			throw new IllegalArgumentException("should set a interval(>0) for the detection.");
		while (true) {
			sleep();
			Snapshot current = Snapshot.flash(home, filter);
			Difference diff = current.diff(last);
			if (!diff.isEmpty()) {
				try {
					if (listener.on(diff)) {
						log.info("detected differen in " + home);
						log.info(diff);
						last = current;
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
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
