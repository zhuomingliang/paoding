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
package com.sohospace.paoding;

import com.sohospace.dictionary.Dictionary;
import com.sohospace.dictionary.Hit;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 */
public class NumberKnife extends CharKnife {

	private Dictionary units;

	public NumberKnife() {
	}
	
	public NumberKnife(Dictionary units) {
		this.units = units;
	}

	public Dictionary getUnits() {
		return units;
	}

	public void setUnits(Dictionary units) {
		this.units = units;
	}

	public boolean assignable(CharSequence beaf, int index) {
		return CharSet.isArabianNumber(beaf.charAt(index));
	}
	
	@Override
	protected boolean isTokenChar(CharSequence beaf, int history, int index) {
		char ch = beaf.charAt(index);
		return CharSet.isArabianNumber(ch) || ch == '.';
	}

	protected void collect(Collector collector, CharSequence beaf, int offset,
			int end, String word) {
		super.collect(collector, beaf, offset, end, word);
		if (units != null) {
			Hit wd;
			int i = end + 1;
			while (i <= beaf.length()
					&& (wd = units.search(beaf, end, i - end)).isHit()) {
				collector.collect(word + beaf.subSequence(end, i), offset, i);
				end++;
				if (!wd.isUnclosed()) {
					break;
				}
				i++;
			}
		}
	}


}
