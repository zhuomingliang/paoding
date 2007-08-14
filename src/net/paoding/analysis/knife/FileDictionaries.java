/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.analysis.knife;

import java.util.Arrays;

import net.paoding.analysis.dictionary.BinaryDictionary;
import net.paoding.analysis.dictionary.Dictionary;
import net.paoding.analysis.dictionary.HashBinaryDictionary;

/**
 * 中文字典缓存根据地,为{@link CJKKnife}所用。<br>
 * 从本对象可以获取中文需要的相关字典。包括词汇表、姓氏表、计量单位表、忽略的词或单字等。
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see CJKKnife
 * 
 * @since 1.0
 */
public class FileDictionaries implements Dictionaries {

	// -------------------------------------------------

	/**
	 * 用于从文件系统中获取词语
	 */
	private FileWordsLoader wordsLoader;

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
	private Dictionary noiseCharactors;

	/**
	 * 忽略的词语
	 * 
	 */
	private Dictionary noiseWords;

	/**
	 * 计量单位
	 */
	private Dictionary units;

	// -------------------------------------------------

	public FileDictionaries() {
	}

	public FileDictionaries(FileWordsLoader wordsLoader) {
		setWordsLoader(wordsLoader);
	}

	public void setWordsLoader(FileWordsLoader wordsLoader) {
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
					String[] words = wordsLoader.getVocabulary().toArray(
							new String[0]);
					Arrays.sort(words);
					// 大概有5639个字有词语，故取0x2fff=x^13>8000>8000*0.75=6000>5639
					vocabulary = new HashBinaryDictionary(words, 0x2fff, 0.75f);
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
					String[] words = wordsLoader.getConfucianFamilyNames()
							.toArray(new String[0]);
					Arrays.sort(words);
					confucianFamilyNames = new BinaryDictionary(words);
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
	public Dictionary getNoiseCharactors() {
		if (noiseCharactors == null) {
			synchronized (this) {
				if (noiseCharactors == null) {
					String[] words = wordsLoader.getNoiseCharactors().toArray(
							new String[0]);
					Arrays.sort(words);
					noiseCharactors = new HashBinaryDictionary(words, 256,
							0.75f);
				}
			}
		}
		return noiseCharactors;
	}

	/**
	 * 忽略的单字
	 * 
	 * @return
	 */
	public Dictionary getNoiseWords() {
		if (noiseWords == null) {
			synchronized (this) {
				if (noiseWords == null) {
					String[] words = wordsLoader.getNoiseWords().toArray(
							new String[0]);
					Arrays.sort(words);
					noiseWords = new BinaryDictionary(words);
				}
			}
		}
		return noiseWords;
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
					String[] words = wordsLoader.getUnits().toArray(
							new String[0]);
					Arrays.sort(words);
					units = new HashBinaryDictionary(words, 1024, 0.75f);
				}
			}
		}
		return units;
	}
}
