/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.lucene.analysis.xanalyzer.collector;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;

import com.sohospace.lucene.analysis.xanalyzer.TokenCollector;
/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 * @since 1.1
 */
public class WriterTokenCollector implements TokenCollector {

	/**
	 * 存储当前被knife分解而成的Token对象
	 * 
	 */
	private LinkedList<Token> tokens;

	/**
	 * Collector接口实现。<br>
	 * 构造词语Token对象，并放置在tokens中
	 * 
	 */
	public void collect(String word, int begin, int end) {
		if (tokens == null) {
			this.tokens = new LinkedList<Token>();
		}
		this.tokens.add(new Token(word, begin, end));
	}

	public Iterator<Token> iterator() {
		if (this.tokens == null) {
			this.tokens = new LinkedList<Token>();
		}
		Iterator<Token> iter = this.tokens.iterator();
		this.tokens = null;
		return iter;
	}

}
