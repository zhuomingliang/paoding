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
	private PaodingMaker(){}

	private static Log log = LogFactory.getLog(PaodingMaker.class);

	/**
	 * 读取类路径下的paoding-analysis.properties文件，据之获取一个Paoding对象．
	 * <p>
	 * 如果paoding-analysis.properties中paoding.make.protype=singleton，
	 * 则每次返回一个先前创建的Paoding对象(如果先前没有创建则创建之)，如果没有配置
	 * 或配置的值不是singleton，则每次返回一个新的Paoding对象。<p>
	 * 
	 * 一般，应不配置或配置为singleton。
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
	 * 如果给定的属性文件中paoding.make.protype=singleton，
	 * 则每次返回一个先前创建的Paoding对象(如果先前没有创建则创建之)，如果没有配置
	 * 或配置的值不是singleton，则每次返回一个新的Paoding对象。<p>
	 * 
	 * 一般，应不配置或配置为singleton。
	 * 
	 * @param properties
	 * @return
	 */
	public static Paoding make(String properties) {
		return implMake(properties(properties), properties);
	}

	/**
	 * 根据给定的属性对象获取一个Paoding对象．
	 * <p>
	 * 如果给定的属性对象中paoding.make.protype=singleton，
	 * 则每次返回一个先前创建的Paoding对象(如果先前没有创建则创建之)，如果没有配置
	 * 或配置的值不是singleton，则每次返回一个新的Paoding对象。<p>
	 * 
	 * 注意的是，要获取先前通过此方法创建的Paoding对象，必须传入上一次传入的属性对象。
	 * 
	 * 一般，应不配置或配置为singleton。
	 * 
	 * @param properties
	 * @return
	 */
	public static Paoding make(Properties p) {
		return implMake(p, p);
	}

	// --------------------------------------------------

	@SuppressWarnings("unchecked")
	private static Paoding implMake(Properties p, Object holderKey) {
		Paoding paoding = null;
		String singleton = p.getProperty(Constants.MAKE_PROTYPE, "singleton");
		//paoding.dic.home.absolute这个属性由系统自动设置，不需要外部指定
		String absoluteDicHome = p.getProperty("paoding.dic.home.absolute");
		if (absoluteDicHome != null) {
			holderKey = absoluteDicHome;
		}
		if ("singleton".equalsIgnoreCase(singleton)) {
			paoding = PaodingHolder.get(holderKey);
			if (paoding != null) {
				return paoding;
			}
		}
		paoding = new Paoding();
		try {
			// 包装各种字典-将自动寻找，若存在则读取类路径中的paoding-analysis.properties文件
			// 若不存在该配置文件，则一切使用默认设置，即字典在文件系统当前路径的dic下(非类路径dic下)
			Dictionaries dictionaries = new FileDictionaries(
					new FileWordsLoader(p));
			Enumeration names = p.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				// 以paoding.knife.class开头的被认为是knife对象
				if (name.startsWith("paoding.knife.class")) {
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
			if ("singleton".equalsIgnoreCase(singleton)) {
				PaodingHolder.set(holderKey, paoding);
			}
			return paoding;
		} catch (Exception e) {
			throw new PaodingAnalysisException("Wrong paoding analysis config:"
					+ e.getMessage(), e);
		}
	}

	private static Properties properties(String path) {
		Properties p = new Properties();
		if (path == null) {
			return p;
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
			in = new FileInputStream(f);
			//保存字典安装目录的绝对路径
			p.setProperty("paoding.dic.home.absolute", f.getAbsolutePath());
			p.load(in);
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
		return p;
	}
}
