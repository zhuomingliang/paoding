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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.paoding.analysis.Config;
import net.paoding.analysis.dictionary.support.filewords.FileWordsReader;
import net.paoding.analysis.dictionary.support.merging.Merger;

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

	public FileWordsLoader() {
		setProperties(Config.properties());
	}

	public FileWordsLoader(Properties p) {
		setProperties(p);
	}

	private void setProperties(Properties p) {
		// dicHome
		this.dicHome = p.getProperty("paoding.dic.home", "dic/");
		dicHome = dicHome.replace('\\', '/');
		if (!dicHome.endsWith("/")) {
			this.dicHome = this.dicHome + "/";
		}
		log.info("paoding.dic.home=" + this.dicHome);
		//
		this.skipPrefix = p.getProperty("paoding.dic.skip.prefix", "x-");
		this.noiseCharactor = p.getProperty("paoding.dic.noise-charactor",
				"x-noise-charactor");
		this.noiseWord = p
				.getProperty("paoding.dic.noise-word", "x-noise-word");
		this.unit = p.getProperty("paoding.dic.unit", "x-unit");
		this.confucianFamilyName = p.getProperty(
				"paoding.dic.confucian-family-name", "x-confucian-family-name");
	}

	public LinkedList<String> getVocabulary() {
		try {
			Map<String, LinkedList<String>> cjk = FileWordsReader
					.readWords(dicHome);
			LinkedList<String> result = null;
			Iterator<String> iter = cjk.keySet().iterator();
			while (iter.hasNext()) {
				String name = iter.next();
				if (name.startsWith(skipPrefix)) {
					continue;
				}
				if (result == null) {
					result = cjk.get(name);
				} else {
					Merger.merge(result, cjk.get(name));
				}
			}
			Merger.remove(result, cjk.get(noiseWord));
			Merger.remove(result, cjk.get(noiseCharactor));
			return result;
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> getConfucianFamilyNames() {
		try {
			return FileWordsReader.readWords(
					dicHome + confucianFamilyName + ".dic").values().iterator()
					.next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> getNoiseWords() {
		try {
			return FileWordsReader.readWords(dicHome + noiseWord + ".dic")
					.values().iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> getNoiseCharactors() {
		try {
			return FileWordsReader.readWords(dicHome + noiseCharactor + ".dic")
					.values().iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	public LinkedList<String> getUnits() {
		try {
			return FileWordsReader.readWords(dicHome + unit + ".dic").values()
					.iterator().next();
		} catch (IOException e) {
			throw toRuntimeException(e);
		}
	}

	// -------------------------------------

	protected RuntimeException toRuntimeException(IOException e) {
		return new RuntimeException(e);
	}

}
