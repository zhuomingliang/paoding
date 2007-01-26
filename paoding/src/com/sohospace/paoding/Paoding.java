/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

/**
 * Paoding是一个背着“刀箱”(内藏各种“刀”)毕生精力“解牛”的人，即“庖丁”。
 * <p>
 * 正因为他拥有各种不同的“刀”，而且能够识别什么“肉(字符)”应该用什么“刀”分割，所以他能游刃有余地把整头牛切割，成为合适的“肉片(词语)”。 <br>
 * 这里的“刀”由Knife扮演，各种“刀”由“刀箱”KnifeBox管理(Paoding对象本身就是一个KnifeBox)，并由KnifeBox决策什么时候出什么“刀”。
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @see Knife
 * @see KnifeBox
 * @see KnifeBoxBean
 * 
 * @since 1.0
 */
public final class Paoding extends KnifeBox implements Knife {

	// -------------------------------------------------
	
	public int dissect(Collector collector, CharSequence beaf, int offset) {
		while (offset >=0 && offset < beaf.length()) {
			offset = super.dissect(collector, beaf, offset);
		}
		return offset;
	}

}
