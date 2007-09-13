package net.paoding.analysis.analyzer;

import java.io.IOException;

import net.paoding.analysis.knife.PaodingMaker;

public class TryPaodingAnalyzer {
	
	public static void main(String[] args) {
	
		String input = "有一次考试的作文题，我用地方成语(闽南语)写作文答题，"
				+ "老师看不懂然后给不及格，批评说作为一个中国人应该写规范汉语！" + "我无语良久。。。";
		if (args.length == 1) {
			input = args[0];
		}
		String file = null;
		String charset = null;
		String mode = null;
		String properties = PaodingMaker.DEFAULT_PROPERTIES_PATH;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--file") || args[i].equals("-f")) {
				file = args[++i];
			} else if (args[i].equals("--charset") || args[i].equals("-c")) {
				charset = args[++i];
			} else if (args[i].equals("--mode") || args[i].equals("-m")) {
				mode = args[++i];
			}
			else if (args[i].equals("--properties") || args[i].equals("-p")) {
				properties = args[++i];
			}
			else if (args[i].equals("--input") || args[i].equals("-i")) {
				input = args[++i];
			}
			else if (args[i].equals("--help") || args[i].equals("-h") || args[i].equals("?")) {
				String app = System.getProperty("paoding.try.app", TryPaodingAnalyzer.class.getSimpleName());
				String cmd = System.getProperty("paoding.try.cmd", "java " + TryPaodingAnalyzer.class.getName());
				System.out.println(app + "的用法:");
				System.out.println("\t" + cmd + " 中华人民共和国");
				System.out.println("OR:");
				System.out.println("\t" + cmd + " [--help|-h|? ][--file|-f file ][--charset|-c charset ][--properties|-p path-of-properties ][--mode|-m mode ][--input|-i 中华人民共和国 ][ 中华人民共和国]");
				System.out.println("\n选项说明:");
				System.out.println("\t--file, -f:\n\t\t文章以文件的形式输入，在前缀加上\"classpath:\"表示从类路径中寻找该文件。");
				System.out.println("\t--charset, -c:\n\t\t文章的字符集编码，比如gbk,utf-8等。如果没有设置该选项，则使用Java环境默认的字符集编码。");
				System.out.println("\t--properties, -p:\n\t\t不读取默认的类路径下的庖丁分词属性文件，而使用指定的文件，在前缀加上\"classpath:\"表示从类路径中寻找该文件。");
				System.out.println("\t--mode, -m:\n\t\t强制使用给定的mode的分词器；可以设定为default,max或指定类名的其他mode(指定类名的，需要加前缀\"class:\")。");
				System.out.println("\t--input, -i:\n\t\t要被分词的文章内容；当没有通过-f或--file指定文章输入文件时可选择这个选项指定要被分词的内容。");
				System.out.println("\n示例:");
				System.out.println("\t" + cmd + " -h");
				System.out.println("\t" + cmd + " 中华人民共和国");
				System.out.println("\t" + cmd + " -m max 中华人民共和国");
				System.out.println("\t" + cmd + " -f e:/content.txt -c gbk");
				System.out.println("\t" + cmd + " -f e:/content.txt -c gbk -m max");
				return;
			}
			else {
				input = args[i];//!!没有++i
			}
		}
		if (file != null) {
			try {
				input = Estimate.Helper.readText(file, charset);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		PaodingAnalyzer analyzer = new PaodingAnalyzer(properties);
		if (mode != null) {
			analyzer.setMode(mode);
		}
		Estimate estimate = new Estimate(analyzer);
		System.out.println("input:\n" + input);
		System.out.println("result:");
		estimate.test(input);
	}
}
