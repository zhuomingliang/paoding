/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.dictionary;

import static com.sohospace.dictionary.Hit.UNDEFINED;

import com.sohospace.dictionary.support.Utils;

/**
 * Dictionary的二叉查找实现。
 * <p>
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.0
 * 
 */
public class BinaryDictionary implements Dictionary {

	// -------------------------------------------------

	private String[] ascWords;
	
	private final int start;
	private final int end;
	private final int count; 

	// -------------------------------------------------

	/**
	 * 以一组升序排列的词语构造二叉查找字典
	 * <p>
	 * 
	 * @param ascWords
	 *            升序排列词语
	 */
	public BinaryDictionary(String[] ascWords) {
		this(ascWords, 0, ascWords.length);
	}
	
	public BinaryDictionary(String[] ascWords, int start, int end) {
		this.ascWords = ascWords;
		this.start = start;
		this.end = end;
		this.count = end - start;
	}

	// -------------------------------------------------
	
	public String get(int index) {
		return ascWords[start + index];
	}

	public int size() {
		return count;
	}

	public Hit search(CharSequence input, int begin, int count) {
		int left = this.start;
		int right = this.end - 1;
		int pointer = 0;
		String word = null;
		int relation;
		//
		while (left <= right) {
			pointer = (left + right) >> 1;
			word = ascWords[pointer];
			relation = Utils.compare(input, begin, count, word);
			if (relation == 0) {
				// System.out.println(new String(input,begin, count)+"***" +
				// word);
				int nextWordIndex = pointer + 1;
				if (nextWordIndex >= ascWords.length) {
					return new Hit(pointer, word, null);
				} else {
					return new Hit(pointer, word, ascWords[nextWordIndex]);
				}
			}
			if (relation < 0)
				right = pointer - 1;
			else
				left = pointer + 1;
		}
		//
		if (left >= ascWords.length) {
			return UNDEFINED;
		}
		//
		boolean asPrex = true;
		String nextWord = ascWords[left];
		// System.out.println(text);
		if (nextWord.length() < count) {
			asPrex = false;
		}
		for (int i = begin, j = 0; asPrex && j < count; i++, j++) {
			if (input.charAt(i) != nextWord.charAt(j)) {
				asPrex = false;
			}
		}
		return asPrex ? new Hit(Hit.UNCLOSED_INDEX, null, nextWord) : UNDEFINED;
	}

}
