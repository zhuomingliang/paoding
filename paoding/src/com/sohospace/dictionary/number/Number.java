package com.sohospace.dictionary.number;
/**
 * @deprecated
 * @author zhiliang.wang@yahoo.com.cn
 *
 */
public class Number {

	public String value;
	public int begin;
	public int end;
	
	public Number(String value, int begin, int end) {
		super();
		this.value = value;
		this.begin = begin;
		this.end = end;
	}
	
	public int getBegin() {
		return begin;
	}
	public int getEnd() {
		return end;
	}
	public String getValue() {
		return value;
	}
	
	
}
