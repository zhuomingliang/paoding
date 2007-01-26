/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding.cjk;

import com.sohospace.dictionary.BinaryDictionary;
import com.sohospace.dictionary.Dictionary;
import com.sohospace.dictionary.HashBinaryDictionary;

/**
 * 中文字典缓存根据地,为{@link CJKKnife}所用。<br>
 * 从本对象可以获取中文需要的相关字典。包括词汇表、姓氏表、计量单位表、忽略的词或单字等。
 * <p>
 * 使用{@link CJKDictionaryFactory}需要设置一个非空的{@link #wordsLoader}。
 * <p>
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @see CJKKnife
 * 
 * @since 1.0
 */
public class CJKDictionaryFactory {

	// -------------------------------------------------

	/**
	 * 用于从目录或数据库中获取词语
	 */
	private WordsLoader wordsLoader;

	// -------------------------------------------------

	/**
	 * 词汇表字典
	 */
	private Dictionary vocabulary;

	/**
	 * 姓氏字典
	 * 
	 */
	private Dictionary confucianFamilyNames;

	/**
	 * 忽略的单字
	 */
	private Dictionary xchars;

	/**
	 * 忽略的词语
	 * 
	 */
	private Dictionary xwords;

	/**
	 * 计量单位
	 */
	private Dictionary units;

	// -------------------------------------------------

	public CJKDictionaryFactory() {
	}

	public CJKDictionaryFactory(WordsLoader wordsLoader) {
		this.wordsLoader = wordsLoader;
	}

	// -------------------------------------------------

	public WordsLoader getWordsLoader() {
		return wordsLoader;
	}

	public void setWordsLoader(WordsLoader wordsLoader) {
		this.wordsLoader = wordsLoader;
	}

	// -------------------------------------------------
	/**
	 * 词汇表字典
	 * 
	 * @return
	 */
	public Dictionary getVocabulary() {
		if (vocabulary == null) {
			synchronized (this) {
				if (vocabulary == null) {
					//大概有5639个字有词语，故取0x2fff=x^13>8000>8000*0.75=6000>5639
					vocabulary = new HashBinaryDictionary(wordsLoader
							.loadCJKVocabulary().toArray(new String[0]), 0x2fff, 0.75f);
				}
			}
		}
		return vocabulary;
	}

	/**
	 * 姓氏字典
	 * 
	 * @return
	 */
	public Dictionary getConfucianFamilyNames() {
		if (confucianFamilyNames == null) {
			synchronized (this) {
				if (confucianFamilyNames == null) {
					confucianFamilyNames = new BinaryDictionary(wordsLoader
							.loadCJKConfucianFamilyNames().toArray(
									new String[0]));
				}
			}
		}
		return confucianFamilyNames;
	}

	/**
	 * 忽略的词语
	 * 
	 * @return
	 */
	public Dictionary getXchars() {
		if (xchars == null) {
			synchronized (this) {
				if (xchars == null) {
					xchars = new HashBinaryDictionary(wordsLoader.loadCJKXchars()
							.toArray(new String[0]), 256, 0.75f);
				}
			}
		}
		return xchars;
	}

	/**
	 * 忽略的单字
	 * 
	 * @return
	 */
	public Dictionary getXwords() {
		if (xwords == null) {
			synchronized (this) {
				if (xwords == null) {
					xwords = new BinaryDictionary(wordsLoader.loadCJKXwords()
							.toArray(new String[0]));
				}
			}
		}
		return xwords;
	}

	/**
	 * 计量单位
	 * 
	 * @return
	 */
	public Dictionary getUnits() {
		if (units == null) {
			synchronized (this) {
				if (units == null) {
					units = new HashBinaryDictionary(wordsLoader.loadCJKUnit()
							.toArray(new String[0]), 1024, 0.75f);
				}
			}
		}
		return units;
	}

}
