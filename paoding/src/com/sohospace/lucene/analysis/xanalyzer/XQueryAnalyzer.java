/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.lucene.analysis.xanalyzer;

import com.sohospace.paoding.Knife;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.1
 */
public final class XQueryAnalyzer extends XAnalyzer {

	public XQueryAnalyzer() {
		super.setMode(QUERY_MODE);
	}

	public XQueryAnalyzer(Knife knife) {
		super.setMode(QUERY_MODE);
		setKnife(knife);
	}
	
	public final void setMode(int mode) {
		throw new IllegalStateException("this is a query mode, cound not change it.");
	}

}
