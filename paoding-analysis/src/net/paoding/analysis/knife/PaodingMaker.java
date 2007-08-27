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

import static net.paoding.analysis.Constants.DIC_CHARSET;
import static net.paoding.analysis.Constants.DIC_CONFUCIAN_FAMILY_NAME;
import static net.paoding.analysis.Constants.DIC_DETECTOR_INTERVAL;
import static net.paoding.analysis.Constants.DIC_HOME;
import static net.paoding.analysis.Constants.DIC_NOISE_CHARACTOR;
import static net.paoding.analysis.Constants.DIC_NOISE_WORD;
import static net.paoding.analysis.Constants.DIC_SKIP_PREFIX;
import static net.paoding.analysis.Constants.DIC_UNIT;
import static net.paoding.analysis.Constants.KNIFE_CLASS;
import static net.paoding.analysis.Constants.getProperty;

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
	private PaodingMaker() {
	}

	private static Log log = LogFactory.getLog(PaodingMaker.class);

	private static ObjectHolder<Properties> propertiesHolder = new ObjectHolder<Properties>();

	private static ObjectHolder<Paoding> paodingHolder = new ObjectHolder<Paoding>();

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
		return make("classpath:paoding-analysis.properties");
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
		return make(loadProperties(propertiesPath));
	}

	/**
	 * 根据给定的属性对象获取一个Paoding对象．
	 * <p>
	 * 
	 * @param properties
	 * @return
	 */
	public static Paoding make(Properties p) {
		preheatProperties(p);
		return implMake(p);
	}

	// -------------------私有 或 辅助方法----------------------------------

	private static void preheatProperties(Properties p) {

		if (p.getProperty("paoding.dic.home.absolute.path") == null) {
			String dicHome = getProperty(p, DIC_HOME);
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
	}

	// --------------------------------------------------

	private static Properties loadProperties(String path) {
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
					throw new PaodingAnalysisException("Not found " + path
							+ " in classpath.");
				}
				f = new File(url.getFile());
			} else {
				f = new File(path);
				if (!f.exists()) {
					throw new PaodingAnalysisException("Not found " + path
							+ " in system.");
				}
			}
			String lastModified = "" + f.lastModified();
			Properties p = propertiesHolder.get(f.getAbsolutePath());
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
			return p;
		} catch (Exception e) {
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

	@SuppressWarnings("unchecked")
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
		paoding = paodingHolder.get(paodingKey);
		if (fromHolder != null) {
			if (paoding != null) {
				return paoding;
			}
		} else {
			paodingHolder.remove(paodingKey);
		}
		String dicHomeAbsolutePath = p.getProperty("paoding.dic.home.absolute.path");
		String interval = getProperty(p, DIC_DETECTOR_INTERVAL);
		paoding = new Paoding();
		paoding.setDicHomeAbsolutePath(dicHomeAbsolutePath);
		paoding.setInterval(Integer.parseInt(interval));
		try {
			// 包装字典。
			// 将自动寻找，若存在则读取类路径中的paoding-analysis.properties文件
			// 若不存在该配置文件，则一切使用默认设置，即字典在文件系统当前路径的dic下(非类路径dic下)

			String dicHome = getProperty(p, DIC_HOME);
			String skipPrefix = getProperty(p, DIC_SKIP_PREFIX);
			String noiseCharactor = getProperty(p, DIC_NOISE_CHARACTOR);
			String noiseWord = getProperty(p, DIC_NOISE_WORD);
			String unit = getProperty(p, DIC_UNIT);
			String confucianFamilyName = getProperty(p,	DIC_CONFUCIAN_FAMILY_NAME);
			String charsetName = getProperty(p, DIC_CHARSET);
			
			log.debug(Constants.DIC_HOME + "=" + dicHome);

			Dictionaries dictionaries = new FileDictionaries(dicHome,
					skipPrefix, noiseCharactor, noiseWord, unit,
					confucianFamilyName, charsetName);
			paoding.setDictionaries(dictionaries);
			Enumeration names = p.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				// 以paoding.knife.class开头的被认为是knife对象
				if (name.startsWith(KNIFE_CLASS)) {
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
			paoding.startDetecting();
			return paoding;
		} catch (Exception e) {
			throw new PaodingAnalysisException("Wrong paoding analysis config:"
					+ e.getMessage(), e);
		}
	}

	static class ObjectHolder<T> {

		private ObjectHolder() {
		}

		private Map<Object, T> objects = new HashMap<Object, T>();

		public T get(Object name) {
			return objects.get(name);
		}

		public void set(Object name, T object) {
			objects.put(name, object);
		}

		public void remove(Object name) {
			objects.remove(name);
		}

	}

}
