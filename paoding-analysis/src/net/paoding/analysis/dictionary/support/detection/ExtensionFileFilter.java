package net.paoding.analysis.dictionary.support.detection;

import java.io.File;
import java.io.FileFilter;
/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */
public class ExtensionFileFilter implements FileFilter {
	private String end;

	public ExtensionFileFilter() {
	}

	public ExtensionFileFilter(String end) {
		this.end = end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getEnd() {
		return end;
	}

	public boolean accept(File pathname) {
		return pathname.isDirectory() || pathname.getName().endsWith(end);
	}

}
