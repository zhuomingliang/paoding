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

import java.util.Properties;

import net.paoding.analysis.Constants;
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

	protected void init() {
		Properties properties = PaodingMaker.getProperties();
		String mode = Constants.getProperty(properties, Constants.ANALYZER_MODE);
		Paoding paoding = PaodingMaker.make(properties);
		setKnife(paoding);
		setMode(mode);
	}
}
