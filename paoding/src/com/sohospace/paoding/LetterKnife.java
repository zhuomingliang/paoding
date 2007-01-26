/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 */
public class LetterKnife extends CharKnife {

	public static final String[] DEFAULT_NOISE = { "and", "are", "as", "at",
			"be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
			"not", "of", "on", "or", "such", "that", "the", "their", "then",
			"there", "these", "they", "this", "to", "was", "will", "with",
			"www" };

	public LetterKnife() {
		super(DEFAULT_NOISE);
	}

	public LetterKnife(String[] noiseWords) {
		super(noiseWords);
	}

	public boolean assignable(CharSequence beaf, int index) {
		return CharSet.isLetter(beaf.charAt(index));
	}
	
	@Override
	protected boolean isTokenChar(CharSequence beaf, int history, int index) {
		char ch = beaf.charAt(index);
		return CharSet.isLetter(ch) || (ch >='0' && ch <='9') || ch == '-';
	}

	@Override
	protected void collect(Collector collector, CharSequence beaf, int offset,
			int end, String word) {
		if (word.length() > 1) {
			super.collect(collector, beaf, offset, end, word);
		}
	}

}
