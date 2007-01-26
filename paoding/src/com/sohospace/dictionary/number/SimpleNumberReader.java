package com.sohospace.dictionary.number;

import java.util.Arrays;
/**
 * @deprecated
 * @author zhiliang.wang@yahoo.com.cn
 *
 */
public class SimpleNumberReader implements NumberReader {

	public Number read(char[] input, int begin) {
		int pointer = begin;
		int count = 0;
		char[] buffer = null;
		for (char digit; pointer < input.length && (digit = isDigit(input[pointer])) != '\0'; pointer ++, count ++) {
			if (buffer == null) {
				buffer = new char[16];
			}
			if (count == buffer.length) {
				char[] newOne = new char[2 * buffer.length];
				System.arraycopy(buffer, 0, newOne, 0, count);
				buffer = newOne;
			}
			buffer[count] = digit;
		}
		//
		if (count == 0) {
			return null;
		}
		String numberValue = new String(buffer, 0, count);
		return new Number(numberValue, begin, pointer);
	}
	
	private final static char[] chars = new char[]{
			'©–','Áã',
			'Ò»','Ò¼',
			'¶ş','ÙE',
			'Èı','Èş',
			'ËÄ','ËÁ',
			'Îå','Îé',
			'Áù','ê‘',
			'Æß','Æâ',
			'°Ë','°Æ',
			'¾Å','¾Á',
	};
	private final static Char2Number[] sortedChars = new Char2Number[chars.length];
	private final static int charsLastIndex = chars.length - 1;
	static {
		final int mode = chars.length / 9;
		for (int i = 0; i < chars.length; i++ ) {
			sortedChars[i] = new Char2Number(chars[i], (char)('0' + (i/mode)));
		}
		Arrays.sort(sortedChars);
	}
	public static char isDigit(char ch) {
		if (ch >= '0' && ch <='9')
			return ch;
		if (ch < sortedChars[0].key || ch > sortedChars[charsLastIndex].key)
			return '\0';
		int index = Arrays.binarySearch(sortedChars, new Char2Number(ch, '\0'));
		if (index < 0) 
			return '\0';
		return sortedChars[index].number;
	}
	
	static class Char2Number implements Comparable<Char2Number> {
		
		char key;
		char number;
		
		public Char2Number(char key, char number) {
			super();
			this.key = key;
			this.number = number;
		}


		public int compareTo(Char2Number o) {
			return key - o.key;
		}
	}
	
}
