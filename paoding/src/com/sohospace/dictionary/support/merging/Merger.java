/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.dictionary.support.merging;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @since 1.0
 * 
 */
public class Merger {

	public static void merge(LinkedList<String> a, LinkedList<String> b) {
		ListIterator<String> aIter = (ListIterator<String>) a.iterator();
		ListIterator<String> bIter = (ListIterator<String>) b.iterator();
		while (aIter.hasNext() && bIter.hasNext()) {
			String aWord = aIter.next();
			boolean bGoOn = true;
			while (bGoOn && bIter.hasNext()) {
				String bWord = bIter.next();
				int r = bWord.compareTo(aWord);
				if (r == 0) {
					continue;
				}
				if (r < 0) {
					aIter.previous();
					aIter.add(bWord);
					aIter.next();
				}
				else {
					bIter.previous();
					bGoOn = false;
				}
			}
		}
		while (bIter.hasNext()) {
			a.add(bIter.next());
		}
	}
	
	public static void remove(LinkedList<String> a, LinkedList<String> b) {
		ListIterator<String> aIter = (ListIterator<String>) a.iterator();
		ListIterator<String> bIter = (ListIterator<String>) b.iterator();
		while (aIter.hasNext() && bIter.hasNext()) {
			String aWord = aIter.next();
			boolean bGoOn = true;
			while (bGoOn && bIter.hasNext()) {
				String bWord = bIter.next();
				int r = bWord.compareTo(aWord);
				if (r == 0) {
					aIter.remove();
					if (aIter.hasNext()) {
						aWord = aIter.next();
					}
				}
				else if (r < 0){
					continue;
				}
				else {
					bIter.previous();
					bGoOn = false;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		LinkedList<String> a = new LinkedList<String>();
		LinkedList<String> b = new LinkedList<String>();
		a.add("1");
		a.add("4");
		a.add("a");
		a.add("c");
		
		b.add("2");
		b.add("3");
		b.add("b");
		b.add("d");
		b.add("太阳");
		
		Merger.merge(a, b);
		
		System.out.println(Arrays.toString(a.toArray(new String[]{})));
	}
}
