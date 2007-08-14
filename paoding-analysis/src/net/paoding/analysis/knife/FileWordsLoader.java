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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import net.paoding.analysis.Constants;
import net.paoding.analysis.dictionary.support.filewords.FileWordsReader;
import net.paoding.analysis.dictionary.support.merging.Merger;
import net.paoding.analysis.exception.PaodingAnalysisException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 */
public class FileWordsLoader {

	private static Log log = LogFactory.getLog(FileWordsLoader.class);

	private String dicHome;
	private String skipPrefix;
	private String noiseCharactor;
	private String noiseWord;
	private String unit;
	private String confucianFamilyName;
	private String charsetName;

	public FileWordsLoader() {
	}

	public FileWordsLoader(Properties p) {
		setProperties(p);
	}

	public void setProperties(Properties p) {
		// dicHome
		this.dicHome = p.getProperty(Constants.DIC_HOME, "dic/");
		// dicHome = dicHome.replaceAll("\\", "/");
		if (!dicHome.endsWith("/")) {
			this.dicHome = this.dicHome + "/";
		}
		log.info(Constants.DIC_HOME + "=" + this.dicHome);
		//
		this.skipPrefix = p.getProperty(Constants.DIC_SKIP_PREFIX, "x-");
		this.noiseCharactor = p.getProperty(Constants.DIC_NOISE_CHARACTOR,
				"x-noise-charactor");
		this.noiseWord = p
				.getProperty(Constants.DIC_NOISE_WORD, "x-noise-word");
		this.unit = p.getProperty(Constants.DIC_UNIT, "x-unit");
		this.confucianFamilyName = p.getProperty(
				Constants.DIC_CONFUCIAN_FAMILY_NAME, "x-confucian-family-name");
		this.charsetName = p.getProperty(Constants.DIC_CHARSET, "UTF-8");
	}

	public LinkedList<String> getVocabulary() {
		try {
			Map<String, LinkedList<String>> cjk = FileWordsReader.readWords(
					dicHome, charsetName);
			LinkedList<String> result = null;
			Iterator<String> iter = cjk.keySet().iterator();
			log.info(dicHome);
			while (iter.hasNext()) {
				String name = iter.next();
				log.info(name);
				if (name.startsWith(skipPrefix)
						|| name.indexOf("/" + skipPrefix) != -1) {
					continue;
				}
				if (result == null) {
					result = cjk.get(name);
				} else {
					Merger.merge(result, cjk.get(name));
				}
			}
			if (result == null) {
				String message = "Not found any dictionary files, have you set the 'paoding.dic.home' right? ("
						+ this.dicHome + ")";
				log.error(message);
				throw new PaodingAnalysisException(message);
			}
			Merger.remove(result, cjk.get(noiseWord));
			Merger.remove(result, cjk.get(noiseCharactor));
			return result;
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	protected LinkedList<String> getDictionary(String dicNameRelativeDicHome) {
		try {
			Map<String, LinkedList<String>> dics = FileWordsReader.readWords(
					dicHome + dicNameRelativeDicHome + ".dic", charsetName);
			return dics.size() == 0 ? new LinkedList<String>() : dics.values()
					.iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> getConfucianFamilyNames() {
		return getDictionary(confucianFamilyName);
	}

	public LinkedList<String> getNoiseWords() {
		return getDictionary(noiseWord);
	}

	public LinkedList<String> getNoiseCharactors() {
		return getDictionary(noiseCharactor);
	}

	public LinkedList<String> getUnits() {
		return getDictionary(unit);
	}

	// -------------------------------------

	protected RuntimeException toRuntimeException(IOException e) {
		return new PaodingAnalysisException(e);
	}

}
