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

	private Class modeClass;

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
	 * 设置分析器模式.
	 * <p>
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		if (mode != DEFAULT_MODE && mode != MAX_MODE) {
			throw new IllegalArgumentException("wrong mode:" + mode);
		}
		this.mode = mode;
		this.modeClass = null;
	}

	/**
	 * 设置分析器模式类。
	 * 
	 * @param modeClass
	 *            TokenCollector的实现类。
	 */
	public void setModeClass(Class modeClass) {
		this.modeClass = modeClass;
	}

	public void setModeClass(String modeClass) {
		try {
			this.modeClass = Class.forName(modeClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("not found mode class", e);
		}
	}

	public void setMode(String mode) {
		if (mode.startsWith("class:")) {
			setModeClass(mode.substring("class:".length()));
		} else {
			if ("default".equalsIgnoreCase(mode)
					|| "writer".equalsIgnoreCase(mode)
					|| "index".equalsIgnoreCase(mode)
					|| ("" + DEFAULT_MODE).equals(mode)) {
				setMode(DEFAULT_MODE);
			} else if ("max".equalsIgnoreCase(mode)
					|| "query".equalsIgnoreCase(mode)
					|| ("" + MAX_MODE).equals(mode)) {
				setMode(MAX_MODE);
			}
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
		if (modeClass != null) {
			try {
				return (TokenCollector) modeClass.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("wrong mode class", e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("wrong mode class", e);
			}
		}
		switch (mode) {
		case DEFAULT_MODE:
			return new DefaultTokenCollector();
		case MAX_MODE:
			return new MaxTokenCollector();
		default:
			throw new Error("never happened");
		}
	}
}
