/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.0
 * 
 */
public class CharSet {
	
	public static boolean isArabianNumber(char ch) {
		return ch >= '0' && ch <= '9';
	}

	public static boolean isLetter(char ch) {
		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
	}

	public static boolean isCjkUnifiedIdeographs(char ch) {
		return ch >= 0x4E00 && ch < 0xA000;
	}

}
