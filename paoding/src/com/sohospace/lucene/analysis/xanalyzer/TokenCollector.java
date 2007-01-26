/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.lucene.analysis.xanalyzer;

import java.util.Iterator;

import org.apache.lucene.analysis.Token;

import com.sohospace.paoding.Collector;
/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 * @since 1.1
 */
public interface TokenCollector extends Collector {

	/**
	 * 
	 * @return
	 */
	public Iterator<Token> iterator();
}
