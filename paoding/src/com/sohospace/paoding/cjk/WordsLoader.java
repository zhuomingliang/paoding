/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding.cjk;

import java.util.LinkedList;
/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 */
public interface WordsLoader {

	public LinkedList<String> loadCJKVocabulary();

	public LinkedList<String> loadCJKConfucianFamilyNames();

	public LinkedList<String> loadCJKXwords();

	public LinkedList<String> loadCJKXchars();

	public LinkedList<String> loadCJKUnit();
}
