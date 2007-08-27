package net.paoding.analysis.dictionary.support.detection;
/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */

public class Node {

	String path;

	boolean isFile;

	public Node() {
	}

	public Node(String path, boolean isFile) {
		this.path = path;
		this.isFile = isFile;
	}

	/**
	 * 返回结点路径
	 * <p>
	 * 如果该结点为根，则返回根的绝对路径<br>
	 * 如果该结点为根下的目录或文件，则返回其相对与根的路径<br>
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 该结点当时的属性：是否为文件
	 * 
	 * @return
	 */
	public boolean isFile() {
		return isFile;
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Node other = (Node) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

}
