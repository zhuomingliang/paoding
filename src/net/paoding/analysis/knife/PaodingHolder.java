package net.paoding.analysis.knife;

import java.util.HashMap;
import java.util.Map;

public class PaodingHolder {

	private PaodingHolder() {
	}

	private static Map<Object, Paoding> paodings = new HashMap<Object, Paoding>();

	public static Paoding get(Object name) {
		return paodings.get(name);
	}

	public static void set(Object name, Paoding paoding) {
		paodings.put(name, paoding);
	}

}
