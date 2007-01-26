/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.dictionary;

import java.util.Arrays;


/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.0
 * 
 */
public class Main0 {

	static String[] words = { "开始", "建立", "数据仓库", "经销", "经销商", "商品", "品味",
			"看出", "问题" };

	static String segment = "当经销商品味茶叶时，看出问题了。";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arrays.sort(words);
		System.out.println(Arrays.toString(words));
		Dictionary dic = new BinaryDictionary(words);
		//
		System.out.println(segment);
		//
		String input = segment;

		//
		int index = 1, count;
		int segmentLength = segment.length();
		for (int begin = 0; begin < segmentLength; begin++) {
			for (index = begin + 1, count = 1; index <= segmentLength; index++, count++) {
				Hit word = dic.search(input, begin, count);
				if (word.isUndefined()) {
					break;
				} else if (word.isUnclosed()) {
					continue;
				} else {
					System.out.println("--" + begin + "," + count + ":" + word.getWord());
				} 
			}

		}
	}

}
