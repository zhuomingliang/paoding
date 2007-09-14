package net.paoding.analysis.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class Estimate {
	private Analyzer analyzer;

	public Estimate() {
	}

	public Estimate(Analyzer analyzer) {
		setAnalyzer(analyzer);
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public void test() {
		this.test(System.out);
	}

	public void test(String input) {
		this.test(System.out, input);
	}

	public void test(PrintStream out) {
		this.test(System.out, "3亿人喝脏水，两个报告直面水污染问题，全国人大常委会委员建议：加大水污染的责与罚!");
	}

	public void test(PrintStream out, String input) {
		try {
			Reader reader = new StringReader(input);
			long begin = System.currentTimeMillis();
			TokenStream ts = analyzer.tokenStream("", reader);
			Token token;
			LinkedList list = new LinkedList();
			while ((token = ts.next()) != null) {
				list.add(token);
			}
			long end= System.currentTimeMillis();
			int c = 0;
			Iterator iter = list.iterator();
			int size = list.size();
			int skipEnd = size - 500;
			if (skipEnd < 0) {
				skipEnd = size;
			}
			else {
				skipEnd = skipEnd - skipEnd % 10;
			}
			boolean dotted = true;
			while (iter.hasNext()) {
				token = (Token) iter.next();
				if (c < 500 || c >= skipEnd) {
					if (c % 10 == 0) {
						if (c != 0) {
							out.println();
						}
						out.print(c + ":\t");
					}
					out.print(token.termText() + "/");
				}
				else if(dotted){
					System.out.print("\n......  ......  ......");
					dotted = false;
				}
				c ++;
			}
			if (c == 0) {
				System.out.println("\tAll are noise characters or words");
			}
			else {
				if (c % 10 != 1) {
					System.out.println();
				}
				System.out.println();
				System.out.println("\t分词器" + analyzer.getClass().getName());
				System.out.println("\t分词耗时 " + (end - begin) + "ms (不包括打印时间)");
			}
		} catch (IOException e) {
			// nerver happen!
		}
	}

	static class Helper {
		static String readText(String path, String encoding) throws IOException {
			InputStream in;
			if (path.startsWith("classpath:")) {
				path = path.substring("classpath:".length());
				URL url = Estimate.class.getClassLoader().getResource(path);
				if (url == null) {
					throw new IllegalArgumentException("Not found " + path
							+ " in classpath.");
				}
				System.out.println("read content from:" + url.getFile());
				in = url.openStream();
			} else {
				File f = new File(path);
				if (!f.exists()) {
					throw new IllegalArgumentException("Not found " + path
							+ " in system.");
				}
				System.out.println("read content from:" + f.getAbsolutePath());
				in = new FileInputStream(f);
			}
			
			Reader re;
			if (encoding != null) {
				re = new InputStreamReader(in, encoding);
			} else {
				re = new InputStreamReader(in);
			}
			char[] chs = new char[1024];
			int count;
			StringBuilder content = new StringBuilder();
			while ((count = re.read(chs)) != -1) {
				content.append(chs, 0, count);
			}
			return content.toString();
		}
	}
}
