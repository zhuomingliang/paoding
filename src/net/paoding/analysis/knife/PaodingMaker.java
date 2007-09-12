/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.analysis.knife;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.paoding.analysis.Constants;
import net.paoding.analysis.exception.PaodingAnalysisException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.0
 */
public class PaodingMaker {

	public static final String DEFAULT_PROPERTIES_PATH = "classpath:paoding-analysis.properties";

	private PaodingMaker() {
	}

	private static Log log = LogFactory.getLog(PaodingMaker.class);

	private static ObjectHolder/* <Properties> */propertiesHolder = new ObjectHolder/* <Properties> */();

	private static ObjectHolder/* <Paoding> */paodingHolder = new ObjectHolder/* <Paoding> */();

	// ----------------获取Paoding对象的方法-----------------------

	/**
	 * 
	 * 读取类路径下的paoding-analysis.properties文件，据之获取一个Paoding对象．
	 * <p>
	 * 第一次调用本方法时，从该属性文件中读取配置，并创建一个新的Paoding对象，之后，如果
	 * 属性文件没有变更过，则每次调用本方法都将返回先前创建的Paoding对象。而不重新构建 Paoding对象。
	 * <p>
	 * 
	 * 如果配置文件没有变更，但词典文件有变更。仍然是返回同样的Paoding对象。而且是，只要
	 * 词典文件发生了变更，Paoding对象在一定时间内会收到更新的。所以返回的Paoding对象 一定是最新配置的。
	 * 
	 * 
	 * 
	 * @return
	 */
	public static Paoding make() {
		return make(DEFAULT_PROPERTIES_PATH);
	}

	/**
	 * 读取类指定路径的配置文件(如果配置文件放置在类路径下，则应该加"classpath:"为前缀)，据之获取一个新的Paoding对象．
	 * <p>
	 * 
	 * 第一次调用本方法时，从该属性文件中读取配置，并创建一个新的Paoding对象，之后，如果
	 * 属性文件没有变更过，则每次调用本方法都将返回先前创建的Paoding对象。而不重新构建 Paoding对象。
	 * <p>
	 * 
	 * 如果配置文件没有变更，但词典文件有变更。仍然是返回同样的Paoding对象。而且是，只要
	 * 词典文件发生了变更，Paoding对象在一定时间内会收到更新的。所以返回的Paoding对象 一定是最新配置的。
	 * 
	 * @param propertiesPath
	 * @return
	 */
	public static Paoding make(String propertiesPath) {
		return make(getProperties(propertiesPath));
	}

	/**
	 * 根据给定的属性对象获取一个Paoding对象．
	 * <p>
	 * 
	 * @param properties
	 * @return
	 */
	public static Paoding make(Properties p) {
		postPropertiesLoaded(p);
		return implMake(p);
	}

	// -------------------私有 或 辅助方法----------------------------------

	private static String getProperty(Properties p, String name) {
		return Constants.getProperty(p, name);
	}

	// --------------------------------------------------

	public static Properties getProperties() {
		return getProperties(DEFAULT_PROPERTIES_PATH);
	}

	public static Properties getProperties(String path) {
		if (path == null) {
			return new Properties();
		}
		File f = null;
		InputStream in = null;
		try {
			if (path.startsWith("classpath:")) {
				path = path.substring("classpath:".length());
				URL url = PaodingMaker.class.getClassLoader().getResource(path);
				if (url == null) {
					throw new IllegalArgumentException("Not found " + path
							+ " in classpath.");
				}
				f = new File(url.getFile());
			} else {
				f = new File(path);
				if (!f.exists()) {
					throw new IllegalArgumentException("Not found " + path
							+ " in system.");
				}
			}
			String lastModified = "" + f.lastModified();
			Properties p = (Properties) propertiesHolder.get(f
					.getAbsolutePath());
			if (p != null) {
				String lastModifiedInHolder = p
						.getProperty("paoding.dic.properties.lastModified");
				if (lastModified.equals(lastModifiedInHolder)) {
					p.setProperty("paoding.dic.properties.from.holder",
							"not-null");
					return p;
				}
			}
			p = new Properties();
			in = new FileInputStream(f);
			// 保存字典安装目录的绝对路径
			p.setProperty("paoding.dic.properties.path", f.getAbsolutePath());
			// 保存属性文件最后更新时间
			p.setProperty("paoding.dic.properties.lastModified", ""
					+ f.lastModified());
			p.load(in);
			propertiesHolder.set(f.getAbsolutePath(), p);
			// !!
			postPropertiesLoaded(p);
			return p;
		} catch (IOException e) {
			throw new PaodingAnalysisException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static void postPropertiesLoaded(Properties p) {
		if (p.getProperty("paoding.dic.home.absolute.path") != null) {
			return;
		}
		// 获取词典安装目录配置：
		// 如配置了PAODING_DIC_HOME环境变量，则将其作为字典的安装主目录
		// 否则使用属性文件的paoding.dic.home配置
		// 但是如果属性文件中强制配置paoding.dic.home.config-first=this，
		// 则优先考虑属性文件的paoding.dic.home配置，
		// 此时只有当属性文件没有配置paoding.dic.home时才会采用环境变量的配置
		String dicHomeBySystemEnv = System.getenv(Constants.ENV_PAODING_DIC_HOME);
		String dicHome = getProperty(p, Constants.DIC_HOME);
		if (dicHomeBySystemEnv != null) {
			String first = getProperty(p, Constants.DIC_HOME_CONFIG_FIRST);
			if (first != null && first.equalsIgnoreCase("this")) {
				if (dicHome == null) {
					dicHome = dicHomeBySystemEnv;
				}
			} else {
				dicHome = dicHomeBySystemEnv;
			}
		}
		// 如果环境变量和属性文件都没有配置词典安转目录
		// 则尝试在当前目录和类路径下寻找是否有dic目录，
		// 若有，则采纳他为paoding.dic.home
		// 如果尝试后均失败，则抛出PaodingAnalysisException异常
		if (dicHome == null) {
			File f = new File("dic");
			if (f.exists()) {
				dicHome = "dic/";
			} else {
				URL url = PaodingMaker.class.getClassLoader()
						.getResource("dic");
				if (url != null) {
					dicHome = "classpath:dic/";
				}
			}
		}
		if (dicHome == null) {
			throw new PaodingAnalysisException(
					"please set a system env PAODING_DIC_HOME or Config paoding.dic.home in paoding-analysis.properties point to the dictionaries!");
		}
		// 规范化dicHome，并设置到属性文件对象中
		dicHome = dicHome.replace('\\', '/');
		if (!dicHome.endsWith("/")) {
			dicHome = dicHome + "/";
		}
		p.setProperty(Constants.DIC_HOME, dicHome);// writer to the properites object
		// 将dicHome转化为一个系统唯一的绝对路径，记录在属性对象中
		File dicHomeFile;
		if (dicHome.startsWith("classpath:")) {
			String name = dicHome.substring("classpath:".length());
			URL url = PaodingMaker.class.getClassLoader().getResource(name);
			if (url == null) {
				throw new PaodingAnalysisException("file \"" + name
						+ "\" not found in classpath!");
			}
			dicHomeFile = new File(url.getFile());
		} else {
			dicHomeFile = new File(dicHome);
			if (!dicHomeFile.exists()) {
				throw new PaodingAnalysisException("Not found " + dicHome
						+ " in system.");
			}
		}
		p.setProperty("paoding.dic.home.absolute.path", dicHomeFile
				.getAbsolutePath());
	}

	private static Paoding implMake(Properties p) {
		Paoding paoding;
		Object paodingKey;
		// paoding.dic.properties.path这个属性由系统自动设置，不需要外部指定
		String propertiesFileId = p.getProperty("paoding.dic.properties.path");
		if (propertiesFileId != null) {
			paodingKey = propertiesFileId;
		} else {
			paodingKey = p;
		}
		String fromHolder = p.getProperty("paoding.dic.properties.from.holder");
		paoding = (Paoding) paodingHolder.get(paodingKey);
		if (fromHolder != null) {
			if (paoding != null) {
				return paoding;
			}
		} else {
			paodingHolder.remove(paodingKey);
		}
		String dicHomeAbsolutePath = p
				.getProperty("paoding.dic.home.absolute.path");
		String interval = getProperty(p, Constants.DIC_DETECTOR_INTERVAL);
		paoding = new Paoding();
		paoding.setDicHomeAbsolutePath(dicHomeAbsolutePath);
		paoding.setInterval(Integer.parseInt(interval));
		try {
			// 包装字典。
			// 将自动寻找，若存在则读取类路径中的paoding-analysis.properties文件
			// 若不存在该配置文件，则一切使用默认设置，即字典在文件系统当前路径的dic下(非类路径dic下)

			String dicHome = getProperty(p, Constants.DIC_HOME);
			String skipPrefix = getProperty(p, Constants.DIC_SKIP_PREFIX);
			String noiseCharactor = getProperty(p,
					Constants.DIC_NOISE_CHARACTOR);
			String noiseWord = getProperty(p, Constants.DIC_NOISE_WORD);
			String unit = getProperty(p, Constants.DIC_UNIT);
			String confucianFamilyName = getProperty(p,
					Constants.DIC_CONFUCIAN_FAMILY_NAME);
			String charsetName = getProperty(p, Constants.DIC_CHARSET);

			log.debug(Constants.DIC_HOME + "=" + dicHome);

			Dictionaries dictionaries = new FileDictionaries(dicHome,
					skipPrefix, noiseCharactor, noiseWord, unit,
					confucianFamilyName, charsetName);
			paoding.setDictionaries(dictionaries);
			Enumeration names = p.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				// 以paoding.knife.class开头的被认为是knife对象
				if (name.startsWith(Constants.KNIFE_CLASS)) {
					String className = p.getProperty(name);
					Class clazz = Class.forName(className);
					Knife knife = (Knife) clazz.newInstance();
					if (knife instanceof DictionariesWare) {
						((DictionariesWare) knife)
								.setDictionaries(dictionaries);
					}
					// 把刀交给庖丁
					log.info("add knike: " + className);
					paoding.addKnife(knife);
				}
			}
			paodingHolder.set(paodingKey, paoding);
			// 启动字典动态转载/卸载检测器
			// 侦测时间间隔(秒)。默认为60秒。如果设置为０或负数则表示不需要进行检测
			paoding.startAutoDetecting();
			return paoding;
		} catch (Exception e) {
			throw new PaodingAnalysisException("Wrong paoding analysis config:"
					+ e.getMessage(), e);
		}
	}

	static class ObjectHolder/* <T> */{

		private ObjectHolder() {
		}

		private Map/* <Object, T> */objects = new HashMap/* <Object, T> */();

		public Object/* T */get(Object name) {
			return objects.get(name);
		}

		public void set(Object name, Object/* T */object) {
			objects.put(name, object);
		}

		public void remove(Object name) {
			objects.remove(name);
		}

	}

}
