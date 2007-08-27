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

import static net.paoding.analysis.Constants.DIC_CHARSET;
import static net.paoding.analysis.Constants.DIC_CONFUCIAN_FAMILY_NAME;
import static net.paoding.analysis.Constants.DIC_HOME;
import static net.paoding.analysis.Constants.DIC_NOISE_CHARACTOR;
import static net.paoding.analysis.Constants.DIC_NOISE_WORD;
import static net.paoding.analysis.Constants.DIC_SKIP_PREFIX;
import static net.paoding.analysis.Constants.DIC_UNIT;
import static net.paoding.analysis.Constants.getProperty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.paoding.analysis.Constants;
import net.paoding.analysis.dictionary.BinaryDictionary;
import net.paoding.analysis.dictionary.Dictionary;
import net.paoding.analysis.dictionary.HashBinaryDictionary;
import net.paoding.analysis.dictionary.support.filewords.FileWordsReader;
import net.paoding.analysis.exception.PaodingAnalysisException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	protected Log log = LogFactory.getLog(this.getClass());

	// -------------------------------------------------

	/**
	 * 词汇表字典
	 */
	protected Dictionary vocabularyDictionary;

	/**
	 * 姓氏字典
	 * 
	 */
	protected Dictionary confucianFamilyNamesDictionary;

	/**
	 * 忽略的单字
	 */
	protected Dictionary noiseCharactorsDictionary;

	/**
	 * 忽略的词语
	 * 
	 */
	protected Dictionary noiseWordsDictionary;

	/**
	 * 计量单位
	 */
	protected Dictionary unitsDictionary;

	// -------------------------------------------------

	protected Map<String, Set<String>> allWords;

	protected String dicHome;
	protected String skipPrefix;
	protected String noiseCharactor;
	protected String noiseWord;
	protected String unit;
	protected String confucianFamilyName;
	protected String charsetName;

	// ----------------------

	public FileDictionaries() {
	}

	public FileDictionaries(Properties p) {
		setProperties(p);
	}

	public void setProperties(Properties p) {
		this.dicHome = getProperty(p, DIC_HOME);
		this.skipPrefix = getProperty(p, DIC_SKIP_PREFIX);
		this.noiseCharactor = getProperty(p, DIC_NOISE_CHARACTOR);
		this.noiseWord = getProperty(p, DIC_NOISE_WORD);
		this.unit = getProperty(p, DIC_UNIT);
		this.confucianFamilyName = getProperty(p, DIC_CONFUCIAN_FAMILY_NAME);
		this.charsetName = getProperty(p, DIC_CHARSET);
		log.debug(Constants.DIC_HOME + "=" + this.dicHome);
	}

	// -------------------------------------------------

	/**
	 * 词汇表字典
	 * 
	 * @return
	 */
	public synchronized Dictionary getVocabularyDictionary() {
		if (vocabularyDictionary == null) {
			Set<String> vocabularySet = getVocabularyWords();
			String[] words = vocabularySet.toArray(new String[vocabularySet
					.size()]);
			Arrays.sort(words);
			// 大概有5639个字有词语，故取0x2fff=x^13>8000>8000*0.75=6000>5639
			vocabularyDictionary = new HashBinaryDictionary(words, 0x2fff,
					0.75f);
		}
		return vocabularyDictionary;
	}

	/**
	 * 姓氏字典
	 * 
	 * @return
	 */
	public synchronized Dictionary getConfucianFamilyNamesDictionary() {
		if (confucianFamilyNamesDictionary == null) {
			Set<String> confucianFamilyNamesSet = getConfucianFamilyNames();
			String[] words = confucianFamilyNamesSet
					.toArray(new String[confucianFamilyNamesSet.size()]);
			Arrays.sort(words);
			confucianFamilyNamesDictionary = new BinaryDictionary(words);
		}
		return confucianFamilyNamesDictionary;
	}

	/**
	 * 忽略的词语
	 * 
	 * @return
	 */
	public synchronized Dictionary getNoiseCharactorsDictionary() {
		if (noiseCharactorsDictionary == null) {
			Set<String> noiseCharactorsSet = getNoiseCharactors();
			String[] words = noiseCharactorsSet
					.toArray(new String[noiseCharactorsSet.size()]);
			Arrays.sort(words);
			noiseCharactorsDictionary = new HashBinaryDictionary(words, 256,
					0.75f);
		}
		return noiseCharactorsDictionary;
	}

	/**
	 * 忽略的单字
	 * 
	 * @return
	 */
	public synchronized Dictionary getNoiseWordsDictionary() {
		if (noiseWordsDictionary == null) {
			Set<String> noiseWordsSet = getNoiseWords();
			String[] words = noiseWordsSet.toArray(new String[noiseWordsSet
					.size()]);
			Arrays.sort(words);
			noiseWordsDictionary = new BinaryDictionary(words);
		}
		return noiseWordsDictionary;
	}

	/**
	 * 计量单位
	 * 
	 * @return
	 */
	public synchronized Dictionary getUnitsDictionary() {
		if (unitsDictionary == null) {
			Set<String> unitsSet = getUnits();
			String[] words = unitsSet.toArray(new String[unitsSet.size()]);
			Arrays.sort(words);
			unitsDictionary = new HashBinaryDictionary(words, 1024, 0.75f);
		}
		return unitsDictionary;
	}

	// ---------------------------------------------------------------
	// 以下为辅助性的方式-类私有或package私有

	protected Set<String> getVocabularyWords() {
		Map<String, Set<String>> dics = loadAllWordsIfNecessary();
		Set<String> result = null;
		Iterator<String> iter = dics.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if (isSkipForVacabulary(name)) {
				continue;
			}
			if (result == null) {
				result = new HashSet<String>(dics.get(name));
			} else {
				result.addAll(dics.get(name));
			}
		}
		// 根据CJKKnife的要求，这里将noise词、字从词汇表移出，以免在切词把他们视为词典规定的成词，而还要从另外判断移出。
		Set<String> noiseWordDic = getNoiseWords();
		if (noiseWordDic != null) {
			result.removeAll(noiseWordDic);
		}
		Set<String> noiseCharactorDic = getNoiseCharactors() ;
		if (noiseCharactorDic != null) {
			result.removeAll(noiseCharactorDic);
		}
		return result;
	}

	protected Set<String> getConfucianFamilyNames() {
		return getDictionaryWords(confucianFamilyName);
	}

	protected Set<String> getNoiseWords() {
		return getDictionaryWords(noiseWord);
	}

	protected Set<String> getNoiseCharactors() {
		return getDictionaryWords(noiseCharactor);
	}

	protected Set<String> getUnits() {
		return getDictionaryWords(unit);
	}

	protected Set<String> getDictionaryWords(String dicNameRelativeDicHome) {
		Map<String, Set<String>> dics = loadAllWordsIfNecessary();
		Set<String> ret = dics.get(dicNameRelativeDicHome);
		return ret == null ? new HashSet<String>() : ret;
	}

	// -------------------------------------

	/**
	 * 读取字典安装目录及子孙目录下的字典文件；并以该字典相对安装目录的路径(包括该字典的文件名，但不包括扩展名)作为key。
	 * 比如，如果字典安装在dic目录下，该目录下有division/china.dic，则该字典文件对应的key是"division/china"
	 */
	protected synchronized Map<String, Set<String>> loadAllWordsIfNecessary() {
		if (allWords == null) {
			try {
				log.info("loading dictionaries from " + dicHome);
				allWords = FileWordsReader.readWords(dicHome, charsetName);
				if (allWords.size() == 0) {
					String message = "Not found any dictionary files, have you set the 'paoding.dic.home' right? ("
							+ this.dicHome + ")";
					log.error(message);
					throw new PaodingAnalysisException(message);
				}
				log.info("loaded success!");
			} catch (IOException e) {
				throw toRuntimeException(e);
			}
		}
		return allWords;
	}

	/**
	 * 
	 * @param dicName
	 */
	protected synchronized void refreshDicWords(String dicPath) {
		int index = dicPath.lastIndexOf(".dic");
		String dicName = dicPath.substring(0, index);
		if (allWords != null) {
			try {
				Map<String, Set<String>> temp = FileWordsReader.readWords(
						dicHome + dicPath, charsetName);
				allWords.put(dicName, temp.values().iterator().next());
			} catch (FileNotFoundException e) {
				// 如果源文件已经被删除了，则表示该字典不要了
				allWords.remove(dicName);
			} catch (IOException e) {
				throw toRuntimeException(e);
			}
			if (!isSkipForVacabulary(dicName)) {
				this.vocabularyDictionary = null;
			}
			// 如果来的的是noiseWord
			if (isNoiseWordDicFile(dicName)) {
				this.noiseWordsDictionary = null;
				//noiseWord和vocabulary有关，所以需要更新vocabulary
				this.vocabularyDictionary = null;
			}
			// 如果来的的是noiseCharactors
			else if (isNoiseCharactorDicFile(dicName)) {
				this.noiseCharactorsDictionary = null;
				//noiseCharactorsDictionary和vocabulary有关，所以需要更新vocabulary
				this.vocabularyDictionary = null;
			}
			// 如果来的的是单元
			else if (isUnitDicFile(dicName)) {
				this.unitsDictionary = null;
			}
			// 如果来的的是亚洲人人姓氏
			else if (isConfucianFamilyNameDicFile(dicName)) {
				this.confucianFamilyNamesDictionary = null;
			}
		}
	}

	// ---------------------------------------

	protected final boolean isSkipForVacabulary(String dicNameRelativeDicHome) {
		return dicNameRelativeDicHome.startsWith(skipPrefix)
				|| dicNameRelativeDicHome.indexOf("/" + skipPrefix) != -1;
	}

	protected boolean isUnitDicFile(String dicName) {
		return dicName.equals(this.unit);
	}

	protected boolean isNoiseCharactorDicFile(String dicName) {
		return dicName.equals(this.noiseCharactor);
	}

	protected boolean isNoiseWordDicFile(String dicName) {
		return dicName.equals(this.noiseWord);
	}

	protected boolean isConfucianFamilyNameDicFile(String dicName) {
		return dicName.equals(this.confucianFamilyName);
	}

	// --------------------------------------

	protected RuntimeException toRuntimeException(IOException e) {
		return new PaodingAnalysisException(e);
	}
}
