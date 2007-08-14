package net.paoding.analysis;

public interface Constants {

	//
	String DIC_HOME = "paoding.dic.home";

	//
	String DIC_CHARSET = "paoding.dic.charset";

	// singleton or protype
	String MAKE_PROTYPE = "paoding.make.protype";

	// dictionaries which are skip
	String DIC_SKIP_PREFIX = "paoding.dic.skip.prefix";

	// chinese/cjk charactors that will not token
	String DIC_NOISE_CHARACTOR = "paoding.dic.noise-charactor";

	// chinese/cjk words that will not token
	String DIC_NOISE_WORD = "paoding.dic.noise-word";

	// unit words, like "ge", "zhi", ...
	String DIC_UNIT = "paoding.dic.unit";

	// like "Wang", "Zhang", ...
	String DIC_CONFUCIAN_FAMILY_NAME = "paoding.dic.confucian-family-name";

	//
	String KNIFE_CLASS = "paoding.knife.class";
}
