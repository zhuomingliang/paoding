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

import net.paoding.analysis.dictionary.Dictionary;
import net.paoding.analysis.dictionary.Hit;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 */
public class NumberKnife extends CombinatoricsKnife implements DictionariesWare {

	private Dictionary units;
	
	public NumberKnife() {
	}

	public NumberKnife(Dictionaries dictionaries) {
		setDictionaries(dictionaries);
	}

	public void setDictionaries(Dictionaries dictionaries) {
		super.setDictionaries(dictionaries);
		units = dictionaries.getUnitsDictionary();
	}
	

	public int assignable(Beef beef, int history, int index) {
		char ch = beef.charAt(index);
		if (CharSet.isArabianNumber(ch))
			return ASSIGNED;
		if (index > history) {
			if (CharSet.isLantingLetter(ch) || ch == '.' || ch == '-' || ch == '_') {
				if (CharSet.isLantingLetter(ch)
						|| !CharSet.isArabianNumber(beef.charAt(index + 1))) {
					//分词效果
					//123.456		->123.456/
					//123.abc.34	->123/123.abc.34/abc/34/	["abc"、"abc/34"系由LetterKnife分出，非NumberKnife]
					//没有或判断!CharSet.isArabianNumber(beef.charAt(index + 1))，则分出"123."，而非"123"
					//123.abc.34	->123./123.abc.34/abc/34/
					return POINT;
				}
				return ASSIGNED;
			}
		}
		return LIMIT;
	}
	
	protected int collectLimit(Collector collector, Beef beef,
			int offset, int point, int limit) {
		int sup = super.collectLimit(collector, beef, offset, point, limit);
		if (units == null) return sup;
		// 2.2两
		//    ^=_point
		//     
		final int _point = point != -1 ? point : limit;
		if (CharSet.isCjkUnifiedIdeographs(beef.charAt(_point))) {
			Hit wd;
			int count = 1;
			while ((wd = units.search(beef, _point, count)).isHit()) {
				collectIfNotNoise(collector, beef, offset, _point + count);
				if (!wd.isUnclosed()) {
					int _limit = _point + count;
					return  sup > _limit ? sup : _limit;
				}
				count++;
			}
		}
		return sup;
	}

}
