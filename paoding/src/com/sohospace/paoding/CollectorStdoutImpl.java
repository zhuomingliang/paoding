/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 *
 */
public class CollectorStdoutImpl implements Collector {

	private static ThreadLocal<Integer> tl = new ThreadLocal<Integer>(){
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};
	
	public void collect(String word, int begin, int end) {
		int c = tl.get() + 1;
		tl.set(c);
		System.out.println(c + ":\t[" + begin + ", " + end + ")=" + word);
	}

}
