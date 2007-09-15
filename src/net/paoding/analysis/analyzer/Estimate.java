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
	private String print;
	private PrintGate printGate;
	

	public Estimate() {
		this.setPrint("50");//默认只打印前50行分词效果
	}

	public Estimate(Analyzer analyzer) {
		setAnalyzer(analyzer);
		this.setPrint("50");//默认只打印前50行分词效果
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setPrint(String print) {
		if (print == null || print.length() == 0 || print.equalsIgnoreCase("null") || print.equalsIgnoreCase("no")) {
			printGate = null;
			this.print = null;
		}
		else {
			printGate = new LinePrintGate();
			printGate.setPrint(print, 10);
			this.print = print;
		}
	}

	public String getPrint() {
		return print;
	}

	public void test(String input) {
		this.test(System.out, input);
	}

	public void test(PrintStream out, String input) {
		try {
			Reader reader = new StringReader(input);
			long begin = System.currentTimeMillis();
			TokenStream ts = analyzer.tokenStream("", reader);
			Token token;
			LinkedList list = new LinkedList();
			int wordsCount = 0;
			while ((token = ts.next()) != null) {
				if (printGate != null && printGate.filter(wordsCount)) {
					list.add(new CToken(token, wordsCount));
				}
				wordsCount++;
			}
			long end = System.currentTimeMillis();
			int c = 0;
			if (list.size() > 0) {
				Iterator iter = list.iterator();
				CToken ctoken;
				while (iter.hasNext()) {
					ctoken = (CToken) iter.next();
					c = ctoken.i;
					token = ctoken.t;
					if (c % 10 == 0) {
						if (c != 0) {
							out.println();
						}
						out.print((c/10 + 1)+ ":\t");
					}
					out.print(token.termText() + "/");
				}
			}
			if (wordsCount == 0) {
				System.out.println("\tAll are noise characters or words");
			} else {
				if (c % 10 != 1) {
					System.out.println();
				}
				System.out.println();
				System.out.println("\t分词器" + analyzer.getClass().getName());
				System.out.println("\t内容长度 " + input.length() + "字符， 分 " + wordsCount
						+ "个词");
				System.out
						.println("\t分词耗时 " + (end - begin) + "ms ");
			}
		} catch (IOException e) {
			// nerver happen!
		}
	}
	
	static class CToken {
		Token t;
		int i;
		
		CToken(Token t, int i) {
			this.t = t;
			this.i = i;
		}
	}

	static interface PrintGate {
		public void setPrint(String print, int unitSize);
		boolean filter(int count);
	}
	
	static class PrintGateToken implements PrintGate {
		private int begin;
		private int end;
		public void setBegin(int begin) {
			this.begin = begin;
		}
		public void setEnd(int end) {
			this.end = end;
		}

		public void setPrint(String print, int unitSize) {
			int i = print.indexOf('-');
			if (i > 0) {
				int bv = Integer.parseInt(print.substring(0, i));
				int ev = Integer.parseInt(print.substring(i + 1));
				setBegin(unitSize * (Math.abs(bv) - 1) );//第5行，是从第40开始的
				setEnd(unitSize * Math.abs(ev));//到第10行，是截止于100(不包含该边界)
			}
			else {
				setBegin(0);
				int v = Integer.parseInt(print);
				setEnd(unitSize * (Math.abs(v)));
			}
		}
		public boolean filter(int count) {
			return count >= begin && count < end;
		}
	}
	
	static class LinePrintGate implements PrintGate {

		private PrintGate[] list;
		
		public void setPrint(String print, int unitSize) {
			String[] prints = print.split(",");
			list = new PrintGate[prints.length];
			for (int i = 0; i < prints.length; i++) {
				PrintGateToken pg = new PrintGateToken();
				pg.setPrint(prints[i], unitSize);
				list[i] = pg;
			}
		}
		
		public boolean filter(int count) {
			for (int i = 0; i < list.length; i++) {
				if (list[i].filter(count)) {
					return true;
				}
			}
			return false;
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
			re.close();
			return content.toString();
		}
	}
}
