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
 * @since 1.0
 * 
 */
public class CJKKnife implements Knife, DictionariesWare {

	// -------------------------------------------------

	private Dictionary vocabulary;
	private Dictionary noiseWords;
	private Dictionary noiseCharactors;
	private Dictionary units;

	// -------------------------------------------------

	public CJKKnife() {
	}

	public CJKKnife(Dictionaries dictionaries) {
		setDictionaries(dictionaries);
	}

	public void setDictionaries(Dictionaries dictionaries) {
		vocabulary = dictionaries.getVocabularyDictionary();
		noiseWords = dictionaries.getNoiseWordsDictionary();
		noiseCharactors = dictionaries.getNoiseCharactorsDictionary();
		units = dictionaries.getUnitsDictionary();
	}

	// -------------------------------------------------

	/**
	 * 分解以CJK字符开始的，后可带阿拉伯数字、英文字母、横线、下划线的字符组成的语句
	 */
	public int assignable(Beef beef, int history, int index) {
		char ch = beef.charAt(index);
		if (CharSet.isCjkUnifiedIdeographs(ch))
			return ASSIGNED;
		if (CharSet.isArabianNumber(ch) || CharSet.isLantingLetter(ch)
				|| ch == '-' || ch == '_')
			return POINT;
		return LIMIT;
	}

	public int dissect(Collector collector, Beef beef, int offset) {

		// 当point == -1时表示本次分解没有遇到POINT性质的字符；
		// 如果point != -1，该值表示POINT性质字符的开始位置，
		// 这个位置将被返回，下一个Knife将从point位置开始分词
		int point = -1;

		// 记录同质字符分词结束极限位置(不包括limit位置的字符)-也就是assignable方法遇到LIMIT性质的字符的位置
		// 如果point==-1，limit将被返回，下一个Knife将从limit位置开始尝试分词
		int limit = offset + 1;

		// 构建point和limit变量的值:
		// 往前直到遇到LIMIT字符；
		// 其中如果遇到第一次POINT字符，则会将它记录为point
		GO_UNTIL_LIMIT: while (true) {
			switch (assignable(beef, offset, limit)) {
			case LIMIT:
				break GO_UNTIL_LIMIT;
			case POINT:
				if (point == -1) {
					point = limit;
				}
			}
			limit++;
		}

		// 如果从offset到beef.length()都是本次Knife的责任，则应读入更多的未读入字符，以支持一个词分在两次beef中的处理
		// 魔幻逻辑：
		// Beef承诺:如果以上GO_UNTIL_LIMIT循环最终把limit值设置为beef.length则表示还为未读入字符。
		// 因为beef一定会在文本全部结束后加入一个char='\0'的值作为最后一个char标志结束。
		// 这样以上的GO_UNTIL_LIMIT将在limit=beef.length()之前就已经break，此时limit!=beef.length
		if (offset > 0 && limit == beef.length()) {
			return -offset;
		}

		// 记录当前正在检视(是否是词典词语)的字符串在beef中的始止位置(包含开始位置，不包含结束位置)
		int curWordOffset = offset, curWordEnd;

		// 记录当前被检视的字符串的长度，它的值恒等于(curWordEnd - curWordOffset)
		int curWordLength;

		// 当前检视的字符串的判断结果
		Hit curWord = null;

		// 限制要判断的字符串的最大开始位置
		// 这个变量不随着程序的运行而变化
		final int offsetLimit;
		if (point != -1)
			offsetLimit = point;
		else
			offsetLimit = limit;

		// 记录到当前为止所分出的词典词语的最大结束位置
		int maxDicWordEnd = offset;

		// 记录最近的不在词典中的字符串(称为孤立字符串)在beef的位置，-1表示没有这个位置
		int isolatedOffset = -1;

		// 记录到当前为止经由词典所切出词的最大长度。
		// 用于辅助判断是否调用shouldBeWord()方法，以把前后有如引号、书名号之类的，但还没有被切出的字符串当成一个词
		// 详见本方法后面对maxDicWordLength的应用以及shouldBeWord()的实现
		int maxDicWordLength = 0;

		// 第1个循环定位被检视字符串的开始位置
		// 被检视的字符串开始位置的极限是offsetLimit，而非limit
		for (; curWordOffset < offsetLimit; curWordOffset++) {

			// 第二个循环定位被检视字符串的结束位置(不包含该位置的字符)
			// 它的起始状态是：被检视的字符串一长度为1，即结束位置为开始位置+1
			curWordEnd = curWordOffset + 1;
			curWordLength = 1;
			for (; curWordEnd <= limit; curWordEnd++, curWordLength++) {

				// 通过词汇表判断，返回判断结果curWord
				curWord = vocabulary.search(beef, curWordOffset, curWordLength);

				// ---------------分析返回的判断结果--------------------------

				// 1)
				// 从词汇表中找到了该词语...
				if (curWord.isHit()) {

					// 1.1)
					// 确认孤立字符串的结束位置(也就是curWordOffset)，
					// 并调用子方法先把之前的孤立字符串进行类似二元分词(但不完全一样，这里仅是为方便而简化说明)
					// 孤立字符串分解完毕，将孤立字符串开始位置isolatedOffset清空
					if (isolatedOffset >= 0) {
						dissectIsolated(collector, beef, isolatedOffset,
								curWordOffset - isolatedOffset);
						isolatedOffset = -1;
					}

					// 1.2)
					// 通知collector本次找到的词语
					// 魔幻逻辑：
					// 这里不需要执行过滤是否是noise词采用，直接通知collector便可：
					// 为了性能考虑，词汇表已经承诺会过滤noise词汇表的词，这样就意味着从词汇表找到的词一定不是noise词汇
					// 参见：FileDictionaries.getVocabularyWords()方法
					collector.collect(curWord.getWord(), curWordOffset,
							curWordEnd);

					// 1.3)
					// 更新最大结束位置
					if (maxDicWordEnd < curWordEnd) {
						maxDicWordEnd = curWordEnd;
					}

					// 1.4)
					// 更新词语最大长度变量的值
					if (curWordOffset == offset
							&& maxDicWordLength < curWordLength) {
						maxDicWordLength = curWordLength;
					}
				}

				// 若isolatedFound==true，表示词典没有该词语
				boolean isolatedFound = curWord.isUndefined();

				// 若isolatedFound==false，则通过Hit的next属性检视词典没有beef的从offset到curWordEnd
				// + 1位置的词
				// 这个判断完全是为了减少一次词典检索而设计的，
				// 如果去掉这个if判断，并不影响程序的正确性(但是会多一次词典检索)
				if (!isolatedFound && !curWord.isHit()) {
					isolatedFound = curWordEnd >= limit
							|| beef.charAt(curWordEnd) < curWord.getNext()
									.charAt(curWordLength);
				}
				// 2)
				// 词汇表中没有该词语，且没有以该词语开头的词汇...
				// -->讲它记录为孤立词语
				if (isolatedFound) {
					if (isolatedOffset < 0 && curWordOffset >= maxDicWordEnd) {
						isolatedOffset = curWordOffset;
					}
					break;
				}

				// ^^^^^^^^^^^^^^^^^^分析返回的判断结果^^^^^^^^^^^^^^^^^^^^^^^^
			}
		}

		// 上面循环分词结束后，可能存在最后的几个未能从词典检索成词的孤立字符串，
		// 此时isolatedOffset不一定等于一个有效值(因为这些孤立字虽然不是词语，但是词典可能存在以它为开始的词语，
		// 只要执行到此才能知道这些虽然是前缀的字符串已经没有机会成为词语了)
		// 所以不能通过isolatedOffset来判断是否此时存在有孤立词，判断依据转换为：
		// 最后一个词典的词的结束位置是否小于offsetLimit(!!offsetLimit, not Limit!!)
		if (maxDicWordEnd < offsetLimit) {
			dissectIsolated(collector, beef, maxDicWordEnd, offsetLimit
					- maxDicWordEnd);
		}

		// 现在是利用maxDicWordLength的时候了
		// 如果本次负责的所有字符串文本没有作为一个词被切分出(包括词典切词和孤立串切分)，
		// 那如果它被showAsWord方法认定为应该作为一个词切分，则将它切出来
		int len = limit - offset;
		if (len > 2 && len != maxDicWordLength
				&& shouldBeWord(beef, offset, limit)) {
			collector.collect(beef.subSequence(offset, limit).toString(),
					offset, limit);
		}

		// 按照point和limit的语义，返回下一个Knife开始切词的开始位置
		return point == -1 ? limit : point;
	}

	// -------------------------------------------------

	/**
	 * 对非词汇表中的字词分词
	 * 
	 * @param cellector
	 * @param beef
	 * @param offset
	 * @param count
	 */
	protected void dissectIsolated(Collector collector, Beef beef, int offset,
			int count) {
		int end = offset + count;
		Hit word = null;
		int nearEnd = end - 1;
		for (int i = offset, j = i; i < end;) {
			j = skipXword(beef, i, end);
			if (j >= 0 && i != j) {
				i = j;
				continue;
			}
			j = collectNumber(collector, beef, i, end);
			if (j >= 0 && i != j) {
				i = j;
				continue;
			}
			word = noiseCharactors.search(beef, i, 1);
			if (word.isHit()) {
				i++;
				continue;
			}
			// 头字
			if (i == offset) {
				// 百度门事件=百度+门+...!=百度+门事+...
				// collect(collector, beef, offset, offset + 1);
			}
			// 尾字
			if (i == nearEnd) {
				// if (nearEnd != offset) {
				// collect(collector, beef, nearEnd, end);
				// }
			}
			// 穷尽二元分词
			else {
				collector.collect(beef.subSequence(i, i + 2).toString(), i,
						i + 2);
			}
			i++;
		}
	}

	protected boolean shouldBeWord(Beef beef, int offset, int end) {
		if (offset > 0 && end < beef.length()) {// 确保前有字符，后也有字符
			int prev = offset - 1;
			// 中文单双引号
			if (beef.charAt(prev) == '“' && beef.charAt(end) == '”') {
				return true;
			} else if (beef.charAt(prev) == '‘' && beef.charAt(end) == '’') {
				return true;
			}
			// 英文单双引号
			else if (beef.charAt(prev) == '\'' && beef.charAt(end) == '\'') {
				return true;
			} else if (beef.charAt(prev) == '\"' && beef.charAt(end) == '\"') {
				return true;
			}
			// 中文书名号
			else if (beef.charAt(prev) == '《' && beef.charAt(end) == '》') {
				return true;
			} else if (beef.charAt(prev) == '〈' && beef.charAt(end) == '〉') {
				return true;
			}
			// 英文尖括号
			else if (beef.charAt(prev) == '<' && beef.charAt(end) == '>') {
				return true;
			}
		}
		return false;
	}

	private final int skipXword(Beef beef, int offset, int end) {
		Hit word;
		for (int k = offset + 2; k <= end; k++) {
			word = noiseWords.search(beef, offset, k - offset);
			if (word.isHit()) {
				offset = k;
			}
			if (word.isUndefined() || !word.isUnclosed()) {
				break;
			}
		}
		return offset;
	}

	private final int collectNumber(Collector collector, Beef beef, int offset,
			int end) {
		int number1 = -1;
		int number2 = -1;
		int cur = offset;
		int bitValue = 0;
		int maxUnit = 0;
		boolean hasDigit = false;// 作用：去除没有数字只有单位的汉字，如“万”，“千”
		for (; cur <= end && (bitValue = toNumber(beef.charAt(cur))) >= 0; cur++) {
			if (bitValue == 2
					&& (beef.charAt(cur) == '两' || beef.charAt(cur) == '俩' || beef
							.charAt(cur) == '倆')) {
				if (cur != offset)
					break;
			}
			if (bitValue >= 0 && bitValue < 10) {
				hasDigit = true;
				if (number2 < 0)
					number2 = bitValue;
				else {
					number2 *= 10;
					number2 += bitValue;
				}
			} else {
				if (number2 < 0) {
					if (number1 < 0) {
						number1 = 1;
					}
					number1 *= bitValue;
				} else {
					if (number1 < 0) {
						number1 = 0;
					}
					if (bitValue >= maxUnit) {
						number1 += number2;
						number1 *= bitValue;
						maxUnit = bitValue;
					} else {
						number1 += number2 * bitValue;
					}
				}
				number2 = -1;
			}
		}
		if (!hasDigit && cur < beef.length()
				&& !units.search(beef, cur, 1).isHit()) {
			return offset;
		}
		if (number2 > 0) {
			if (number1 < 0) {
				number1 = number2;
			} else {
				number1 += number2;
			}
		}
		if (number1 >= 0) {
			collector.collect(String.valueOf(number1), offset, cur);

			// 后面可能跟了计量单位
			Hit wd;
			int i = cur + 1;
			while (i <= beef.length()
					&& (wd = units.search(beef, cur, i - cur)).isHit()) {
				collector.collect(String.valueOf(number1)
						+ beef.subSequence(cur, i), offset, i);
				cur++;
				if (!wd.isUnclosed()) {
					break;
				}
				i++;
			}
		}
		return cur;
	}

	private final int toNumber(char c) {
		switch (c) {
		case '零':
		case '〇':
			return 0;
		case '一':
		case '壹':
			return 1;
		case '二':
		case '两':
		case '俩':
		case '貳':
			return 2;
		case '三':
		case '叁':
			return 3;
		case '四':
		case '肆':
			return 4;
		case '五':
		case '伍':
			return 5;
		case '六':
		case '陸':
			return 6;
		case '柒':
		case '七':
			return 7;
		case '捌':
		case '八':
			return 8;
		case '九':
		case '玖':
			return 9;
		case '十':
		case '什':
			return 10;
		case '百':
		case '佰':
			return 100;
		case '千':
		case '仟':
			return 1000;
		case '万':
		case '萬':
			return 10000;
		case '亿':
		case '億':
			return 100000000;
		default:
			return -1;
		}
	}

}
