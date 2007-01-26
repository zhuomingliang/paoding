/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

import java.util.ArrayList;

/**
 * KnifeBox负责决策当遇到字符串指定位置时应使用的Knife对象.
 * <p>
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @see Paoding
 * 
 * @since 1.0
 * 
 */
public class KnifeBox implements Knife {

	private ArrayList<Knife> knives = new ArrayList<Knife>(); 
	
	public void addKnife(Knife k) {
		knives.add(k);
	}
	
	public ArrayList<Knife> getKnives() {
		return knives;
	}

	public void setKnives(ArrayList<Knife> knives) {
		this.knives = knives;
	}

	public boolean assignable(CharSequence beaf, int index) {
		return true;
	}
	
	public int dissect(Collector collector, CharSequence beaf, int offset) {
		int size = knives.size();
		Knife knife;
		for (int i = 0; i < size; i++) {
			knife = knives.get(i);
			if (knife.assignable(beaf, offset)){
				return knife.dissect(collector, beaf, offset);
			}
		}
		return ++offset;
	}
}
