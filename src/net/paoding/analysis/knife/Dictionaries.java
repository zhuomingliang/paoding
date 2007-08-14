package net.paoding.analysis.knife;

import net.paoding.analysis.dictionary.Dictionary;

/**
 * 中文字典缓存根据地,为{@link CJKKnife}所用。<br>
 * 从本对象可以获取中文需要的相关字典。包括词汇表、姓氏表、计量单位表、忽略的词或单字等。
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see CJKKnife
 * 
 * @since 1.0
 */
public interface Dictionaries {
	/**
	 * 词汇表字典
	 * 
	 * @return
	 */
	public Dictionary getVocabulary();

	/**
	 * 姓氏字典
	 * 
	 * @return
	 */
	public Dictionary getConfucianFamilyNames();

	/**
	 * 忽略的词语
	 * 
	 * @return
	 */
	public Dictionary getNoiseCharactors();

	/**
	 * 忽略的单字
	 * 
	 * @return
	 */
	public Dictionary getNoiseWords();

	/**
	 * 计量单位
	 * 
	 * @return
	 */
	public Dictionary getUnits();
}
