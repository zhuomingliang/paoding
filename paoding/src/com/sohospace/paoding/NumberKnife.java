/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

import com.sohospace.dictionary.Dictionary;
import com.sohospace.dictionary.Hit;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 */
public class NumberKnife extends CharKnife {

	private Dictionary units;

	public NumberKnife() {
	}
	
	public NumberKnife(Dictionary units) {
		this.units = units;
	}

	public Dictionary getUnits() {
		return units;
	}

	public void setUnits(Dictionary units) {
		this.units = units;
	}

	public boolean assignable(CharSequence beaf, int index) {
		return CharSet.isArabianNumber(beaf.charAt(index));
	}
	
	@Override
	protected boolean isTokenChar(CharSequence beaf, int history, int index) {
		char ch = beaf.charAt(index);
		return CharSet.isArabianNumber(ch) || ch == '.';
	}

	protected void collect(Collector collector, CharSequence beaf, int offset,
			int end, String word) {
		super.collect(collector, beaf, offset, end, word);
		if (units != null) {
			Hit wd;
			int i = end + 1;
			while (i <= beaf.length()
					&& (wd = units.search(beaf, end, i - end)).isHit()) {
				collector.collect(word + beaf.subSequence(end, i), offset, i);
				end++;
				if (!wd.isUnclosed()) {
					break;
				}
				i++;
			}
		}
	}


}
