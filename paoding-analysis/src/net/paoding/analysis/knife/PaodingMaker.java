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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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


	// --------------------------------------------------

	public static Properties getProperties() {
		return getProperties(DEFAULT_PROPERTIES_PATH);
	}

	public static Properties getProperties(String path) {
		if (path == null) {
			throw new NullPointerException("path should not be null!");
		}
		try {
			//
			Properties p = (Properties) propertiesHolder.get(path);
			if (p == null || modified(p)) {
				p = loadProperties(new Properties(), path);
				propertiesHolder.set(path, p);
				paodingHolder.remove(path);
				postPropertiesLoaded(p);
				String absolutePaths = p.getProperty("paoding.analysis.properties.files.absolutepaths");
				log.info("config paoding analysis from: " + absolutePaths);
			}
			return p;
		} catch (IOException e) {
			throw new PaodingAnalysisException(e);
		}
	}


	// -------------------私有 或 辅助方法----------------------------------
	
	private static boolean modified(Properties p)
			throws FileNotFoundException {
		String lastModifieds = p
				.getProperty("paoding.analysis.properties.lastModifieds");
		String[] lastModifedsArray = lastModifieds.split(";");
		String files = p.getProperty("paoding.analysis.properties.files");
		String[] filesArray = files.split(";");
		for (int i = 0; i < filesArray.length; i++) {
			File file = getFile(filesArray[i]);
			if (file.exists() && !String.valueOf(file.lastModified()).equals(lastModifedsArray[i])) {
				return true;
			}
		}
		return false;
	}

	private static Properties loadProperties(Properties p, String path)
			throws IOException {
		URL url;
		File file;
		String absolutePath;
		InputStream in;
		// 若ifexists为真表示如果该文件存在则读取他的内容，不存在则忽略它
		boolean skipWhenNotExists = false;
		if (path.startsWith("ifexists:")) {
			skipWhenNotExists = true;
			path = path.substring("ifexists:".length());
		}
		if (path.startsWith("classpath:")) {
			path = path.substring("classpath:".length());
			url = getClassLoader().getResource(path);
			if (url == null) {
				if (skipWhenNotExists) {
					return p;
				}
				throw new FileNotFoundException("Not found " + path
						+ " in classpath.");
			}
			file = new File(url.getFile());
			in = url.openStream();
		} else {
			file = new File(path);
			if (skipWhenNotExists && !file.exists()) {
				return p;
			}
			in = new FileInputStream(file);
		}
		absolutePath = file.getAbsolutePath();
		p.load(in);
		in.close();
		String lastModifieds = p.getProperty("paoding.analysis.properties.lastModifieds");
		String files = p.getProperty("paoding.analysis.properties.files");
		String absolutePaths = p.getProperty("paoding.analysis.properties.files.absolutepaths");
		if (lastModifieds == null) {
			p.setProperty("paoding.dic.properties.path", path);
			lastModifieds = String.valueOf(file.lastModified());
			files = path;
			absolutePaths = absolutePath;
		} else {
			lastModifieds = lastModifieds + ";" + file.lastModified();
			files = files + ";" + path;
			absolutePaths = absolutePaths + ";" + absolutePath;
		}
		p.setProperty("paoding.analysis.properties.lastModifieds", lastModifieds);
		p.setProperty("paoding.analysis.properties.files", files);
		p.setProperty("paoding.analysis.properties.files.absolutepaths", absolutePaths);
		String importsValue = p.getProperty("paoding.imports");
		if (importsValue != null) {
			p.remove("paoding.imports");
			String[] imports = importsValue.split(";");
			for (int i = 0; i < imports.length; i++) {
				loadProperties(p, imports[i]);
			}
		}
		return p;
	}
	
	private static void postPropertiesLoaded(Properties p) {
		if ("done".equals(p.getProperty("paoding.analysis.postPropertiesLoaded"))) {
			return;
		}
		// 获取词典安装目录配置：
		// 如配置了PAODING_DIC_HOME环境变量，则将其作为字典的安装主目录
		// 否则使用属性文件的paoding.dic.home配置
		// 但是如果属性文件中强制配置paoding.dic.home.config-first=this，
		// 则优先考虑属性文件的paoding.dic.home配置，
		// 此时只有当属性文件没有配置paoding.dic.home时才会采用环境变量的配置
		String dicHomeBySystemEnv = System
				.getenv(Constants.ENV_PAODING_DIC_HOME);
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
		p.setProperty(Constants.DIC_HOME, dicHome);// writer to the properites
													// object
		// 将dicHome转化为一个系统唯一的绝对路径，记录在属性对象中
		File dicHomeFile = getFile(dicHome);
		if (!dicHomeFile.exists()) {
			throw new PaodingAnalysisException("not found the dic home dirctory! " + dicHomeFile.getAbsolutePath());
		}
		if (!dicHomeFile.isDirectory()){
			throw new PaodingAnalysisException("dic home should not be a file, but a directory!");
		}
		p.setProperty("paoding.dic.home.absolute.path", dicHomeFile.getAbsolutePath());
		p.setProperty("paoding.analysis.postPropertiesLoaded", "done");
	}

	
	private static Paoding implMake(Properties p) {
		// 将要返回的Paoding对象，它可能是新创建的，也可能使用paodingHolder中已有的Paoding对象
		Paoding paoding;
		// 作为本次返回的Paoding对象在paodingHolder中的key，使之后同样的key不会重复创建Paoding对象
		Object paodingKey;
		// 如果该属性对象是通过PaodingMaker由文件读入的，则必然存在paoding.dic.properties.path属性
		// 详细请参考loadProperties方法)
		String path = p.getProperty("paoding.dic.properties.path");
		// 如果该属性由文件读入，则文件地址作为Paoding对象在paodingHolder中的key
		if (path != null) {
			paodingKey = path;
		// 否则以属性文件作为其key，之后只要进来的是同一个属性对象，都返回同一个Paoding对象
		} else {
			paodingKey = p;
		}
		paoding = (Paoding) paodingHolder.get(paodingKey);
		if (paoding != null) {
			return paoding;
		}
		// 如果PaodingHolder中并没有缓存该属性文件或对象对应的Paoding对象，
		// 则根据给定的属性创建一个新的Paoding对象，并在返回之前存入paodingHolder
		String dicHomeAbsolutePath = p.getProperty("paoding.dic.home.absolute.path");
		String interval = getProperty(p, Constants.DIC_DETECTOR_INTERVAL);
		paoding = new Paoding();
		paoding.setDicHomeAbsolutePath(dicHomeAbsolutePath);
		paoding.setInterval(Integer.parseInt(interval));
		try {
			//将字典命名规则参数读出来，从而创建FileDictionaries
			String skipPrefix = getProperty(p, Constants.DIC_SKIP_PREFIX);
			String noiseCharactor = getProperty(p, Constants.DIC_NOISE_CHARACTOR);
			String noiseWord = getProperty(p, Constants.DIC_NOISE_WORD);
			String unit = getProperty(p, Constants.DIC_UNIT);
			String confucianFamilyName = getProperty(p, Constants.DIC_CONFUCIAN_FAMILY_NAME);
			String combinatorics = getProperty(p, Constants.DIC_FOR_COMBINATORICS);
			String charsetName = getProperty(p, Constants.DIC_CHARSET);
			Dictionaries dictionaries = new FileDictionaries(dicHomeAbsolutePath,
					skipPrefix, noiseCharactor, noiseWord, unit,
					confucianFamilyName, combinatorics, charsetName);
			paoding.setDictionaries(dictionaries);
			//寻找传说中的Knife。。。。
			final Map /* <String, Knife> */ knifeMap = new HashMap /* <String, Knife> */ ();
			List /* <Knife> */ knifeList = new LinkedList/* <Knife> */();
			List /* <Function> */ functions = new LinkedList/* <Function> */();
			Iterator iter = p.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry e = (Map.Entry) iter.next();
				final String key = (String) e.getKey();
				final String value = (String) e.getValue();
				int index = key.indexOf(Constants.KNIFE_CLASS);
				if (index == 0 && key.length() > Constants.KNIFE_CLASS.length()) {
					final int end = key.indexOf('.', Constants.KNIFE_CLASS.length());
					if (end  == -1) {
						Class clazz = Class.forName(value);
						Knife knife = (Knife) clazz.newInstance();
						if (knife instanceof DictionariesWare) {
							((DictionariesWare) knife)
									.setDictionaries(dictionaries);
						}
						knifeList.add(knife);
						knifeMap.put(key, knife);
						log.info("add knike: " + value);
					}
					else {
						// 由于属性对象属于hash表，key的读取顺序不和文件的顺序一致，不能保证属性设置时，knife对象已经创建
						// 所以这里只定义函数放到functions中，待到所有的knife都创建之后，在执行该程序
						functions.add(new Function() {
							public void run() throws Exception {
								String knifeName = key.substring(0, end);
								Object obj = knifeMap.get(knifeName);
								if (!obj.getClass().getName().equals("org.springframework.beans.BeanWrapperImpl")) {
									Class beanWrapperImplClass = Class.forName("org.springframework.beans.BeanWrapperImpl");
									Method setWrappedInstance = beanWrapperImplClass.getMethod("setWrappedInstance", new Class[]{Object.class});
									Object beanWrapperImpl = beanWrapperImplClass.newInstance();
									setWrappedInstance.invoke(beanWrapperImpl, new Object[]{obj});
									knifeMap.put(knifeName, beanWrapperImpl);
									obj = beanWrapperImpl;
								}
								String propertyName = key.substring(end + 1);
								Method setPropertyValue = obj.getClass().getMethod("setPropertyValue", new Class[]{String.class, Object.class});
								setPropertyValue.invoke(obj, new Object[]{propertyName, value});
							}
						});
					}
				}
			}
			// 完成所有留后执行的程序
			for (Iterator iterator = functions.iterator(); iterator.hasNext();) {
				Function function = (Function) iterator.next();
				function.run();
			}
			// 把刀交给庖丁
			paoding.setKnives(knifeList);
			
			// Paoding对象创建成功！此时可以将它寄放到paodingHolder中，给下次重复利用
			paodingHolder.set(paodingKey, paoding);
			// 启动字典动态转载/卸载检测器
			// 侦测时间间隔(秒)。默认为60秒。如果设置为０或负数则表示不需要进行检测
			paoding.startAutoDetecting();
			return paoding;
		} catch (Exception e) {
			throw new PaodingAnalysisException("", e);
		}
	}


	private static File getFile(String path) {
		File file;
		URL url;
		if (path.startsWith("classpath:")) {
			path = path.substring("classpath:".length());
			url = getClassLoader().getResource(path);
			final boolean fileExist = url != null;
			file = new File(fileExist ? url.getFile() : path) {
				private static final long serialVersionUID = 4009013298629147887L;

				public boolean exists() {
					return fileExist;
				}
			};
		} else {
			file = new File(path);
		}
		return file;
	}

	private static ClassLoader getClassLoader() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = PaodingMaker.class.getClassLoader();
		}
		return loader;
	}

	private static String getProperty(Properties p, String name) {
		return Constants.getProperty(p, name);
	}
	
	//--------------------------------------------------------------------
	
	private static class ObjectHolder/* <T> */{

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
	
	private static interface Function {
		public void run() throws Exception;
	}

}
