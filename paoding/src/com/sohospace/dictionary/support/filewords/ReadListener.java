/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.dictionary.support.filewords;


/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.0
 * 
 */
public interface ReadListener {
	public boolean onFileBegin(String file);
	public void onFileEnd(String file);
	public void onWord(String word);
}
