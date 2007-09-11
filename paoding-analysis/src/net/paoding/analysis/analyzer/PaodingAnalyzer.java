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
package net.paoding.analysis.analyzer;

import java.io.Reader;

import net.paoding.analysis.knife.CJKKnife;
import net.paoding.analysis.knife.Knife;
import net.paoding.analysis.knife.Paoding;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * PaodingAnalyzer是基于“庖丁解牛”框架的Lucene词语分析器，是“庖丁解牛”框架对Lucene的适配器。
 * <p>
 * 
 * PaodingAnalyzer是线程安全的：并发情况下使用同一个PaodingAnalyzer实例是可行的。<br>
 * PaodingAnalyzer是可复用的：推荐多次同一个PaodingAnalyzer实例。
 * <p>
 * 
 * 如有需要特别调整，应通过构造函数或knife设置器(setter)配置自订制的Knife实例。
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see PaodingTokenizer
 * @see Knife
 * @see Paoding
 * @see CJKKnife
 * @see TokenCollector
 * 
 * @since 1.0
 * 
 */
public class PaodingAnalyzer extends Analyzer {

	// -------------------------------------------------

	/**
	 * 最大切分和最小切分兼有
	 */
	public static final int DEFAULT_MODE = 1;

	/**
	 * 按最大切分
	 */
	public static final int MAX_MODE = 2;

	// -------------------------------------------------
	/**
	 * 用于向PaodingTokenizer提供，分解文本字符
	 * 
	 * @see PaodingTokenizer#next()
	 * 
	 */
	private Knife knife;

	/**
	 * @see #DEFAULT_MODE
	 * @see #MAX_MODE
	 */
	private int mode = DEFAULT_MODE;

	// -------------------------------------------------

	public PaodingAnalyzer() {
	}

	/**
	 * @see #setKnife(Knife)
	 * @param knife
	 */
	public PaodingAnalyzer(Knife knife) {
		this.knife = knife;
	}

	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzer(Knife knife, int mode) {
		this.knife = knife;
		this.mode = mode;
	}
	


	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzer(Knife knife, String mode) {
		this.knife = knife;
		this.setMode(mode);
	}

	public static PaodingAnalyzer defaultMode(Knife knife) {
		return new PaodingAnalyzer(knife, DEFAULT_MODE);
	}
	
	
	public static PaodingAnalyzer maxMode(Knife knife) {
		return new PaodingAnalyzer(knife, MAX_MODE);
	}

	// -------------------------------------------------

	public Knife getKnife() {
		return knife;
	}

	public void setKnife(Knife knife) {
		this.knife = knife;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * 设置分析器模式。写模式(WRITER_MODE)或检索模式(QUERY_MODE)其中一种。默认为写模式。
	 * <p>
	 * WRITER_MODE在建立索引时使用，能够使分析器对每个可能的词语建立索引<br>
	 * QUERY_MODE在用户搜索时使用，使用户检索的结果匹配度最大化
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public void setMode(String mode) {
		if ("default".equalsIgnoreCase(mode) || "writer".equalsIgnoreCase(mode) || "index".equalsIgnoreCase(mode)){
			this.mode = DEFAULT_MODE;
		}
		else if ("max".equalsIgnoreCase(mode) || "query".equalsIgnoreCase(mode)){
			this.mode = MAX_MODE;
		}
	}

	// -------------------------------------------------

	public TokenStream tokenStream(String fieldName, Reader reader) {
		if (knife == null) {
			throw new NullPointerException("knife should be set before token");
		}
		// PaodingTokenizer是TokenStream实现，使用knife解析reader流入的文本
		return new PaodingTokenizer(reader, knife, createTokenCollector());
	}

	protected TokenCollector createTokenCollector() {
		switch (mode) {
		case DEFAULT_MODE:
			return new DefaultTokenCollector();
		case MAX_MODE:
			return new MaxTokenCollector();
		default:
			throw new IllegalArgumentException("wrong mode");
		}
	}

}
