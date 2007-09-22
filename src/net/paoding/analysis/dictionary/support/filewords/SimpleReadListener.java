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
package net.paoding.analysis.dictionary.support.filewords;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import net.paoding.analysis.knife.CharSet;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 1.0
 * 
 */
public class SimpleReadListener implements ReadListener {
	private Map/* <String, Set<String>> */dics = new Hashtable/* <String, Set<String>> */();
	private HashSet/* <String> */words = new HashSet/* <String> */();

	public boolean onFileBegin(String file) {
		if (!file.endsWith(".dic")) {
			return false;
		}
		words = new HashSet/* <String> */();
		return true;
	}

	public void onFileEnd(String file) {
		String name = file.substring(0, file.length() - 4);
		dics.put(name, words);
		words = null;
	}

	public void onWord(String word) {
		word = word.trim().toLowerCase();
		if (word.length() == 0 || word.charAt(0) == '#'
				|| word.charAt(0) == '-') {
			return;
		}
		//去除汉字数字词
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			int num = CharSet.toNumber(ch);
			if (num >= 0) {
				if (i == 0) {
					if (num > 10) {//"十二" vs "千万"
						break;
					}
				}
				if (num == 2) {
					if (word.equals("两") || word.equals("两两")) {
						break;
					}
				}
				if (i + 1 == word.length()) {
					return;
				}
			}
			else {
				break;
			}
		}
		words.add(word);
	}

	public Map/* <String, Set<String>> */getResult() {
		return dics;
	}

}