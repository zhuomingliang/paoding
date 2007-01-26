/*
 * 本代码所有权归作者所有 但在保持源代码不被破坏以及所有人署名的基础上 任何人可自由无限使用
 */
package com.sohospace.paoding;

/**
 * Collector接收Knife切割文本得到的词语。
 * <p>
 * 
 * @author zhiliang.wang@yahoo.com.cn
 * 
 * @see Knife
 * 
 * @since 1.0
 * 
 */
public interface Collector {

	/**
	 * 当Knife从文本流中获取一个词语时，本方法被调用。 <br>
	 * 调用的顺序与词语在文本流中的顺序是否一致视不同实现可能有不同的策略。
	 * <p>
	 * 
	 * 如当Knife收到“中国当代社会现象”文本流中的“社会”时，传入的参数分别将是：(“社会”, 4, 6)
	 * 
	 * @param word
	 *            接收到的词语
	 * @param offset
	 *            该词语在文本流中的偏移位置
	 * @param end
	 *            该词语在文本流中的结束位置(词语不包括文本流end位置的字符)，end-offset是为word的长度
	 * 
	 *         
	 */
	public void collect(String word, int offset, int end);
}
