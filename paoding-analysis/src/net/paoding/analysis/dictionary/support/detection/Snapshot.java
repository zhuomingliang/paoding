package net.paoding.analysis.dictionary.support.detection;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */
public class Snapshot {

	// 此次快照版本，使用时间表示
	private long version;

	// 根地址，绝对地址，使用/作为目录分隔符
	private String root;

	// String为相对根的地址，使用/作为目录分隔符
	private Map<String, InnerNode> nodesMap = new HashMap<String, InnerNode>();

	//
	private InnerNode[] nodes;

	private Snapshot() {
	}

	public static Snapshot flash(File rootFile, FileFilter filter) {
		Snapshot snapshot = new Snapshot();
		snapshot.implFlash(rootFile, filter);
		return snapshot;
	}
	
	private void implFlash(File rootFile, FileFilter filter) {
		version = System.currentTimeMillis();
		root = rootFile.getAbsolutePath().replace('\\', '/');
		if (!rootFile.exists()) {
			// do nothing, maybe the file has been deleted
		} else {
			InnerNode rootNode = new InnerNode();
			rootNode.path = root;
			rootNode.isFile = rootFile.isFile();
			rootNode.lastModified = rootFile.lastModified();
			nodesMap.put(root, rootNode);
			if (rootFile.isDirectory()) {
				LinkedList<File> files = getPosterity(rootFile, filter);
				nodes = new InnerNode[files.size()];
				Iterator<File> iter = files.iterator();
				for (int i = 0; i < nodes.length; i++) {
					File f = iter.next();
					String path = f.getAbsolutePath().substring(
							this.root.length() + 1);
					path = path.replace('\\', '/');
					InnerNode node = new InnerNode();
					node.path = path;
					node.isFile = f.isFile();
					node.lastModified = f.lastModified();
					int index = path.lastIndexOf('/');
					node.parent = index == -1 ? root : path.substring(0, index);
					nodes[i] = node;
					nodesMap.put(path, node);
				}
			}
		}
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public Difference diff(Snapshot that) {
		Snapshot older = that;
		Snapshot yonger = this;
		if (that.version > this.version) {
			older = this;
			yonger = that;
		}
		Difference diff = new Difference();
		if (!yonger.root.equals(older.root)) {
			throw new IllegalArgumentException("the snaps should be same root");
		}
		for (InnerNode olderNode : older.nodes) {
			InnerNode yongerNode = yonger.nodesMap.get((String) olderNode.path);
			if (yongerNode == null) {
				diff.getDeleted().add(olderNode);
			} else if (yongerNode.lastModified != olderNode.lastModified) {
				diff.getModified().add(olderNode);
			}
		}

		for (InnerNode yongerNode : yonger.nodes) {
			InnerNode olderNode = older.nodesMap.get((String) yongerNode.path);
			if (olderNode == null) {
				diff.getNewcome().add(yongerNode);
			}
		}
		return diff;
	}

	public static void main(String[] args) throws InterruptedException {
		File f = new File("dic");
		Snapshot snapshot1 = Snapshot.flash(f, null);
		System.out.println("----");
		Thread.sleep(3000);
		System.out.println("----");
		Thread.sleep(3000);
		System.out.println("----");
		Snapshot snapshot2 = Snapshot.flash(f, null);
		Difference diff = snapshot2.diff(snapshot1);
		String deleted = Arrays.toString(diff.getDeleted().toArray(
				new Node[] {}));
		System.out.println("deleted: " + deleted);
		String modified = Arrays.toString(diff.getModified().toArray(
				new Node[] {}));
		System.out.println("modified: " + modified);
		String newcome = Arrays.toString(diff.getNewcome().toArray(
				new Node[] {}));
		System.out.println("newcome: " + newcome);
	}

	// --------------------------------------------

	private LinkedList<File> getPosterity(File root, FileFilter filter) {
		ArrayList<File> dirs = new ArrayList<File>();
		LinkedList<File> files = new LinkedList<File>();
		dirs.add(root);
		int index = 0;
		while (index < dirs.size()) {
			File cur = dirs.get(index++);
			File[] children = cur.listFiles();
			for (File f : children) {
				if (filter == null || filter.accept(f)) {
					if (f.isDirectory()) {
						dirs.add(f);
					} else {
						files.add(f);
					}
				}
			}
		}
		return files;
	}

	class InnerNode extends Node {
		String parent;
		long lastModified;
	}

}
