package net.paoding.analysis.knife;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.paoding.analysis.Config;

public class PaodingMaker {
	
	private static Log log = LogFactory.getLog(PaodingMaker.class);
	
	public static Paoding make() {
		return make(Config.properties());
	}

	@SuppressWarnings("unchecked")
	public static Paoding make(Properties p) {
		try {
			Paoding paoding = new Paoding();
			// 包装各种字典-将自动寻找，若存在则读取类路径中的paoding-analysis.properties文件
			// 若不存在该配置文件，则一切使用默认设置，即字典在文件系统当前路径的dic下(非类路径dic下)
			Dictionaries dictionaries = new FileDictionaries(p);
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
			return paoding;
		} catch (Exception e) {
			throw new IllegalStateException("Wrong paoding properties config.",
					e);
		}
	}
}
