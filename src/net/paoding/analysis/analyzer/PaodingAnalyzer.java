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
package net.paoding.analysis.analyzer;

import java.io.IOException;
import java.util.Properties;

import net.paoding.analysis.Constants;
import net.paoding.analysis.knife.Knife;
import net.paoding.analysis.knife.Paoding;
import net.paoding.analysis.knife.PaodingMaker;

/**
 * PaodingAnalyzer是基于“庖丁解牛”框架的Lucene词语分析器，是“庖丁解牛”框架对Lucene的适配器。
 * <p>
 * 
 * PaodingAnalyzer是线程安全的：并发情况下使用同一个PaodingAnalyzer实例是可行的。<br>
 * PaodingAnalyzer是可复用的：推荐多次同一个PaodingAnalyzer实例。
 * <p>
 * 
 * PaodingAnalyzer自动读取类路径下的paoding-analysis.properties属性文件，装配PaodingAnalyzer
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see PaodingAnalyzerBean
 * 
 * @since 1.0
 * 
 */
public class PaodingAnalyzer extends PaodingAnalyzerBean {

	/**
	 * 根据类路径下的paoding-analysis.properties构建一个PaodingAnalyzer对象
	 * <p>
	 * 在一个JVM中，可多次创建，而并不会多次读取属性文件，不会重复读取字典。
	 */
	public PaodingAnalyzer() {
		init();
	}

	protected void init() {
		// 根据PaodingMaker说明，
		// 1、多次调用getProperties()，返回的都是同一个properties实例(只要属性文件没发生过修改)
		// 2、相同的properties实例，PaodingMaker也将返回同一个Paoding实例
		// 根据以上1、2点说明，在此能够保证多次创建PaodingAnalyzer并不会多次装载属性文件和词典
		Properties properties = PaodingMaker.getProperties();
		String mode = Constants
				.getProperty(properties, Constants.ANALYZER_MODE);
		Paoding paoding = PaodingMaker.make(properties);
		setKnife(paoding);
		setMode(mode);
	}

	/**
	 * 本方法为PaodingAnalyzer附带的测试评估方法。 <br>
	 * 执行之可以查看分词效果。以下任选一种方式进行:
	 * <p>
	 * 
	 * java net.paoding.analysis.analyzer.PaodingAnalyzer<br>
	 * java net.paoding.analysis.analyzer.PaodingAnalyzer 中华人民共和国<br>
	 * java net.paoding.analysis.analyzer.PaodingAnalyzer "file=c:/text.txt"<br>
	 * java net.paoding.analysis.analyzer.PaodingAnalyzer "file=c:/text.txt" utf-8<br>
	 * 
	 * !!!file=xxx这样的参数需要加引号
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PaodingAnalyzer analyzer = new PaodingAnalyzer();
		String input = "有一次考试的作文题，我用地方成语(闽南语)写作文答题，"
				+ "老师看不懂然后给不及格，批评说作为一个中国人应该写规范汉语！" + "我无语良久。。。";
		if (args.length > 0) {
			input = args[0];
		}
		String prefix = "file=";
		if (input.startsWith(prefix)) {
			String path = input.substring(prefix.length());
			try {
				input = Estimate.Helper.readText(path,
						(args.length > 1 ? args[1] : null));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		Estimate estimate = new Estimate(analyzer);
		System.out.println("input:\n" + input);
		System.out.println("result:");
		estimate.test(input);
	}

	// --------------------------------------------------

	/**
	 * @param knife
	 * @param default_mode
	 * @deprecated
	 */
	public PaodingAnalyzer(Knife knife, int mode) {
		super(knife, mode);
	}

	/**
	 * 等价于maxMode()
	 * 
	 * @param knife
	 * @return
	 * @deprecated
	 */
	public static PaodingAnalyzer queryMode(Knife knife) {
		return maxMode(knife);
	}

	/**
	 * 
	 * @param knife
	 * @return
	 * @deprecated
	 */
	public static PaodingAnalyzer defaultMode(Knife knife) {
		return new PaodingAnalyzer(knife, DEFAULT_MODE);
	}

	/**
	 * 
	 * @param knife
	 * @return
	 * @deprecated
	 */
	public static PaodingAnalyzer maxMode(Knife knife) {
		return new PaodingAnalyzer(knife, MAX_MODE);
	}

	/**
	 * 等价于defaultMode()
	 * 
	 * @param knife
	 * @return
	 * @deprecated
	 * 
	 */
	public static PaodingAnalyzer writerMode(Knife knife) {
		return defaultMode(knife);
	}
}
