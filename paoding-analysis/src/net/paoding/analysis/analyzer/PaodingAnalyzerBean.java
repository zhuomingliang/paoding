package net.paoding.analysis.analyzer;

import java.io.Reader;

import net.paoding.analysis.knife.Knife;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class PaodingAnalyzerBean extends Analyzer {

	// -------------------------------------------------

	/**
	 * 最大切分和最小切分兼有
	 */
	public static final int DEFAULT_MODE = 1;

	/**
	 * @deprecated 请使用DEFAULT_MODE
	 */
	public static final int WRITER_MODE = DEFAULT_MODE;

	/**
	 * 按最大切分
	 */
	public static final int MAX_MODE = 2;

	/**
	 * @deprecated 请使用MAX_MODE
	 */
	public static final int QUERY_MODE = MAX_MODE;

	// -------------------------------------------------
	/**
	 * 用于向PaodingTokenizer提供，分解文本字符
	 * 
	 * @see PaodingTokenizer#next()
	 * 
	 */
	private Knife knife;

	/**
	 * @see #DEFAULT_MODE
	 * @see #MAX_MODE
	 */
	private int mode = DEFAULT_MODE;

	// -------------------------------------------------

	public PaodingAnalyzerBean() {
	}

	/**
	 * @see #setKnife(Knife)
	 * @param knife
	 */
	public PaodingAnalyzerBean(Knife knife) {
		this.knife = knife;
	}

	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzerBean(Knife knife, int mode) {
		this.knife = knife;
		this.mode = mode;
	}

	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzerBean(Knife knife, String mode) {
		this.knife = knife;
		this.setMode(mode);
	}

	public static Analyzer defaultMode(Knife knife) {
		return new PaodingAnalyzerBean(knife, DEFAULT_MODE);
	}

	public static Analyzer maxMode(Knife knife) {
		return new PaodingAnalyzerBean(knife, MAX_MODE);
	}

	/**
	 * 
	 * @param knife
	 * @return
	 * @deprecated 请使用defaultMode替代
	 * 
	 */
	public static Analyzer writerMode(Knife knife) {
		return defaultMode(knife);
	}

	/**
	 * 
	 * @param knife
	 * @return
	 * @deprecated 请使用maxMode替代
	 */
	public static Analyzer queryMode(Knife knife) {
		return maxMode(knife);
	}

	// -------------------------------------------------

	public Knife getKnife() {
		return knife;
	}

	public void setKnife(Knife knife) {
		this.knife = knife;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * 设置分析器模式。写模式(WRITER_MODE)或检索模式(QUERY_MODE)其中一种。默认为写模式。
	 * <p>
	 * WRITER_MODE在建立索引时使用，能够使分析器对每个可能的词语建立索引<br>
	 * QUERY_MODE在用户搜索时使用，使用户检索的结果匹配度最大化
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setMode(String mode) {
		if ("default".equalsIgnoreCase(mode) || "writer".equalsIgnoreCase(mode)
				|| "index".equalsIgnoreCase(mode)
				|| ("" + DEFAULT_MODE).equals(mode)) {
			this.mode = DEFAULT_MODE;
		} else if ("max".equalsIgnoreCase(mode)
				|| "query".equalsIgnoreCase(mode)
				|| ("" + MAX_MODE).equals(mode)) {
			this.mode = MAX_MODE;
		}
	}

	// -------------------------------------------------

	public TokenStream tokenStream(String fieldName, Reader reader) {
		if (knife == null) {
			throw new NullPointerException("knife should be set before token");
		}
		// PaodingTokenizer是TokenStream实现，使用knife解析reader流入的文本
		return new PaodingTokenizer(reader, knife, createTokenCollector());
	}

	protected TokenCollector createTokenCollector() {
		switch (mode) {
		case DEFAULT_MODE:
			return new DefaultTokenCollector();
		case MAX_MODE:
			return new MaxTokenCollector();
		default:
			throw new IllegalArgumentException("wrong mode");
		}
	}
}
