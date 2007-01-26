/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.dictionary;
/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 * @since 1.1
 */
public class DictionaryDelegate implements Dictionary {
	private Dictionary target;

	public DictionaryDelegate() {
	}

	public DictionaryDelegate(Dictionary target) {
		this.target = target;
	}

	public Dictionary getTarget() {
		return target;
	}

	public void setTarget(Dictionary target) {
		this.target = target;
	}

	public String get(int index) {
		return target.get(index);
	}

	public Hit search(CharSequence input, int offset, int count) {
		return target.search(input, offset, count);
	}

	public int size() {
		return target.size();
	}

}
