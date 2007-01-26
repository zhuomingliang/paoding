/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding.cjk;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.sohospace.dictionary.support.filewords.FileWordsReader;
import com.sohospace.dictionary.support.merging.Merger;
/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 */
public class FileWordsLoader implements WordsLoader {
	public static void main(String[] args) throws IOException {
		int size = new FileWordsLoader().loadCJKVocabulary().size();
		System.out.println(size);
	}
	private String path = "dic/CJK";
	
	public FileWordsLoader() {
	}
	
	public FileWordsLoader(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public LinkedList<String> loadCJKVocabulary() {
		Map<String, LinkedList<String>> ejk;
		try {
			ejk = FileWordsReader.readWords(path);
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
		//
		String baseName = "base";
		LinkedList<String> base = ejk.get(baseName);
		ejk.remove(baseName);
		//
		String divAsianChinaName = "division.asian.china";
		LinkedList<String> divAsianChina = ejk.get(divAsianChinaName);
		if (divAsianChina == null) {
			divAsianChina = new LinkedList<String>();
		}
		else {
			ejk.remove(divAsianChinaName);
		}
		//
		Iterator<String> iter = ejk.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if (name.startsWith("x")) {
				continue;
			}
			Merger.merge(divAsianChina, ejk.get(name));
		}
		Merger.merge(divAsianChina, loadDirectory(path + "/locale"));
		Merger.merge(divAsianChina, loadDirectory(path + "/division"));
		Merger.merge(base, divAsianChina);
		Merger.remove(base, ejk.get("x干扰词"));
		return base;
	}

	protected LinkedList<String> loadDirectory(String dir)  {
		Map<String, LinkedList<String>> ls;
		try {
			ls = FileWordsReader.readWords(dir);
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
		if (ls.size() > 0) {
			Iterator<LinkedList<String>> iter = ls.values().iterator();
			LinkedList<String> a = iter.next();
			while (iter.hasNext()) {
				Merger.merge(a, iter.next());
			}
			return a;
		}
		else {
			return new LinkedList<String>();
		}
	}
	
	public LinkedList<String> loadCJKConfucianFamilyNames()  {
		try {
			return FileWordsReader.readWords(
					path + "/x姓氏.dic").values()
					.iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> loadCJKXwords() {
		try {
			return FileWordsReader.readWords(path + "/x干扰词.dic").values()
					.iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> loadCJKXchars()  {
		try {
			return FileWordsReader.readWords(path + "/x干扰字.dic")
					.values().iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> loadCJKUnit()  {
		try {
			return FileWordsReader.readWords(path + "/x计量单位.dic")
					.values().iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}
	
	//-------------------------------------
	
	protected RuntimeException toRuntimeException(IOException e) {
		return new RuntimeException(e);
	}
	

}
