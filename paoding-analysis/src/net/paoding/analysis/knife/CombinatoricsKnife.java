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

import java.util.HashSet;

import net.paoding.analysis.dictionary.Dictionary;
import net.paoding.analysis.dictionary.Hit;

/**
 * 排列组合Knife。
 * <p>
 * 
 * 该Knife把遇到的非LIMIT字符视为一个单词分出。<br>
 * 同时如果有以该词语开头的字符串在x-for-combinatorics.dic出现也会切出
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 1.0
 * 
 */
public abstract class CombinatoricsKnife implements Knife, DictionariesWare{

	protected Dictionary combinatoricsDictionary;
	
	protected HashSet/* <String> */noiseTable;

	public CombinatoricsKnife() {
	}

	public CombinatoricsKnife(String[] noiseWords) {
		setNoiseWords(noiseWords);
	}

	public void setNoiseWords(String[] noiseWords) {
		noiseTable = new HashSet/* <String> */((int) (noiseWords.length * 1.5));
		for (int i = 0; i < noiseWords.length; i++) {
			noiseTable.add(noiseWords[i]);
		}
	}

	public void setDictionaries(Dictionaries dictionaries) {
		combinatoricsDictionary = dictionaries.getCombinatoricsDictionary();
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
		// 如果最后一个字符也是ASSIGNED以及POINT，
		// 且beef之前已经被分解了一部分(从而能够腾出空间以读入新的字符)，则需要重新读入字符后再分词
		if (limit == beef.length() && offset > 0) {
			return -offset;
		}

		// 收集从offset分别到point以及limit的词
		// 注意这里不收集从point到limit的词
		// ->当然可能从point到limit的字符也可能是一个词，不过这不是本次分解的责任
		// ->如果认为它应该是个词，那么只要配置对应的其它Knife实例，该Knife会有机会把它切出来的
		// ->因为我们会返回point作为下一个Knife分词的开始。
		if (point != -1) {
			collectPoint(collector, beef, offset, point);
		}
		collectLimit(collector, beef, offset, limit);

		//检索是否有以该词语位前缀的词典词语
		//若有，则将它解出
		int followedEnd = limit;
		if (combinatoricsDictionary != null && limit < beef.length() && beef.charAt(limit) > 0xFF) {
			followedEnd = tryFollows(collector, beef, offset, limit);
		}
		// 如果从词典找到相应的词，则下一个Knife直接从这个词的结束位置开始，而不是返回point或limit
		if (followedEnd != limit) {
			return followedEnd;
		}
		return nextOffset(beef, offset, point, limit);
	}

	protected void collectPoint(Collector collector, Beef beef,
			int offset, int point) {
		collectIfNotNoise(collector, beef, offset, point);
	}

	protected void collectLimit(Collector collector, Beef beef,
			int offset, int limit) {
		collectIfNotNoise(collector, beef, offset, limit);
	}

	protected int tryFollows(Collector collector, Beef beef,
			int offset, int limit) {
		int ret = limit;
		for (int end = limit + 1, count = limit - offset + 1; end <= beef
				.length(); end++, count++) {
			Hit hit = combinatoricsDictionary.search(beef, offset, end);
			if (hit.isUndefined()) {
				break;
			} else if (hit.isHit()) {
				collectIfNotNoise(collector, beef, offset, end);
				// 每前进1个位置收到词语，将ret自增
				ret++;
			}
			// gotoNextChar为true表示在词典中存在以当前词为开头的词，
			boolean gotoNextChar = hit.isUnclosed() && end < beef.length()
					&& beef.charAt(end) >= hit.getNext().charAt(count);
			if (!gotoNextChar) {
				break;
			}
		}
		return ret;
		// TODO:
		// 可能存在情况:
		// 刚好词语分隔在两次beef之中，比如"U"刚好是此次beef的最后字符，而"盘"是下一次beef的第一个字符
		// 这种情况现在CombinatoricsKnife还没机制办法识别将之处理为一个词语
	}

	protected void collectIfNotNoise(Collector collector, Beef beef,
			int offset, int end) {
		// 将offset和end之间的词(不包含end位置)创建出来给word
		// 如果该词语为噪音词，则重新丢弃之(设置为null)，
		String word = beef.subSequence(offset, end).toString();
		if (noiseTable != null && noiseTable.contains(word)) {
			word = null;
		}

		// 否则发送消息给collect方法，表示Knife新鲜出炉了一个内容为word的候选词语
		// 即：最终决定是否要把这个词语通知给collector的是collect方法
		if (word != null) {
			doCollect(collector, word, beef, offset, end);
		}
	}

	/**
	 * 收集分解出的候选词语。 默认实现是将该候选词语通知给收集器collector。<br>
	 * 子类覆盖本方法可以更灵活地控制词语的收录，例如控制仅当word满足一些额外条件再决定是否收集，<br>
	 * 或依上下文环境收集更多的相关词语
	 * 
	 * @param collector
	 * @param word
	 * @param beef
	 * @param offset
	 * @param end
	 */
	protected void doCollect(Collector collector, String word,
			Beef beef, int offset, int end) {
		collector.collect(word, offset, end);
	}

	protected int nextOffset(Beef beef, int offset, int point,
			int limit) {
		// 按照postition和limit的语义说明，返回下一个Knife可以从该位置开始分解
		return point != -1 ? point : limit;
	}
}
