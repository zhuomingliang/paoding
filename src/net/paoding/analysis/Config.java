package net.paoding.analysis;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Config {
	private static Properties p;

	public static Properties properties() {
		if (p == null) {
			Properties p = new Properties();
			try {
				URL url = Config.class.getClassLoader().getResource(
						"paoding-analysis.properties");
				if (url == null) {
					System.out
							.println("not found paoding-analysis.properties. using default.");
				} else {
					InputStream in = url.openStream();
					p.load(in);
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Config.p = p;
		}
		return p;
	}
}
