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
		Reader reader = new StringReader(input);
		TokenStream ts = analyzer.tokenStream("", reader);
		Token token;
		try {
			int c = 0;
			while ((token = ts.next()) != null) {
				c ++;
				if (c % 10 == 0) {
					out.println();
				}
				out.print(token.termText() + "/");
			}
			out.println();
		} catch (IOException e) {
			// nerver happen!
		}
	}

	static class Helper {
		static String readText(String path, String encoding) throws IOException {
			File f;
			if (path.startsWith("classpath:")) {
				path = path.substring("classpath:".length());
				URL url = Estimate.class.getClassLoader().getResource(path);
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
			InputStream in = new FileInputStream(f);
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
