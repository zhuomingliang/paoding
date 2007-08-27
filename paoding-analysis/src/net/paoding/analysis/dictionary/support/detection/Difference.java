package net.paoding.analysis.dictionary.support.detection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */

public class Difference {

	/**
	 * 变更了的
	 * 
	 * @return
	 */
	private List<Node> modified = new LinkedList<Node>();

	/**
	 * 删除了的
	 * 
	 * @return
	 */
	private List<Node> deleted = new LinkedList<Node>();

	/**
	 * 新加的
	 * 
	 * @return
	 */
	private List<Node> newcome = new LinkedList<Node>();

	public List<Node> getModified() {
		return modified;
	}

	public void setModified(List<Node> modified) {
		this.modified = modified;
	}

	public List<Node> getDeleted() {
		return deleted;
	}

	public void setDeleted(List<Node> deleted) {
		this.deleted = deleted;
	}

	public List<Node> getNewcome() {
		return newcome;
	}

	public void setNewcome(List<Node> newcome) {
		this.newcome = newcome;
	}

	public boolean isEmpty() {
		return deleted.isEmpty() && modified.isEmpty() && newcome.isEmpty();
	}

	@Override
	public String toString() {
		String smodified = Arrays.toString(modified.toArray(new Node[] {}));
		String snewcome = Arrays.toString(newcome.toArray(new Node[] {}));
		String sdeleted = Arrays.toString(deleted.toArray(new Node[] {}));
		return "modified=" + smodified + ";newcome=" + snewcome + ";deleted=" + sdeleted;
	}

}
