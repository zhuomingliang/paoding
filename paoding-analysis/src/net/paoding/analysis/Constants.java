package net.paoding.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.0
 */
public class Constants {

	//
	public static final String DIC_HOME = "paoding.dic.home";
	public static final String DIC_HOME_DEFAULT = "dic/";

	//
	public static final String DIC_CHARSET = "paoding.dic.charset";
	public static final String DIC_CHARSET_DEFAULT = "UTF-8";

	// dictionaries which are skip
	public static final String DIC_SKIP_PREFIX = "paoding.dic.skip.prefix";
	public static final String DIC_SKIP_PREFIX_DEFAULT = "x-";

	// chinese/cjk charactors that will not token
	public static final String DIC_NOISE_CHARACTOR = "paoding.dic.noise-charactor";
	public static final String DIC_NOISE_CHARACTOR_DEFAULT = "x-noise-charactor";

	// chinese/cjk words that will not token
	public static final String DIC_NOISE_WORD = "paoding.dic.noise-word";
	public static final String DIC_NOISE_WORD_DEFAULT = "x-noise-word";

	// unit words, like "ge", "zhi", ...
	public static final String DIC_UNIT = "paoding.dic.unit";
	public static final String DIC_UNIT_DEFAULT = "x-unit";

	// like "Wang", "Zhang", ...
	public static final String DIC_CONFUCIAN_FAMILY_NAME = "paoding.dic.confucian-family-name";
	public static final String DIC_CONFUCIAN_FAMILY_NAME_DEFAULT = "x-confucian-family-name";

	// like "Wang", "Zhang", ...
	public static final String DIC_DETECTOR_INTERVAL = "paoding.dic.detector.interval";
	public static final String DIC_DETECTOR_INTERVAL_DEFAULT = "60";

	private static final Map/* <String, String> */ map = new HashMap/* <String, String> */();
	
	static {
		map.put(DIC_HOME, DIC_HOME_DEFAULT);
		map.put(DIC_CHARSET, DIC_CHARSET_DEFAULT);
		map.put(DIC_SKIP_PREFIX, DIC_SKIP_PREFIX_DEFAULT);
		map.put(DIC_NOISE_CHARACTOR, DIC_NOISE_CHARACTOR_DEFAULT);
		map.put(DIC_NOISE_WORD, DIC_NOISE_WORD_DEFAULT);
		map.put(DIC_UNIT, DIC_UNIT_DEFAULT);
		map.put(DIC_CONFUCIAN_FAMILY_NAME, DIC_CONFUCIAN_FAMILY_NAME_DEFAULT);
		map.put(DIC_DETECTOR_INTERVAL, DIC_DETECTOR_INTERVAL_DEFAULT);
	}

	//
	public static final String KNIFE_CLASS = "paoding.knife.class";

	public static String getProperty(Properties p, String name) {
		String value = p.getProperty(name, (String) map.get(name));
		if (name.equals("paoding.dic.home")) {
			value = value.replace('\\', '/');
			if (!value.endsWith("/")) {
				value = value + "/";
			}
		}
		return value;
	}
}
