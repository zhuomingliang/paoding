package net.paoding.analysis.knife;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FakeKnife implements Knife, DictionariesWare {

	private Log log = LogFactory.getLog(this.getClass());

	private String name;
	private int intParam;
	private Inner inner = new Inner();

	public void setName(String name) {
		this.name = name;
		log.info("set property: name=" + name);
	}

	public String getName() {
		return name;
	}

	public int getIntParam() {
		return intParam;
	}

	public void setIntParam(int intParam) {
		this.intParam = intParam;
		log.info("set property: intParam=" + intParam);
	}

	public void setInner(Inner inner) {
		this.inner = inner;
	}

	public Inner getInner() {
		return inner;
	}

	public boolean assignable(CharSequence beaf, int index) {
		return false;
	}

	public int dissect(Collector collector, CharSequence beaf, int offset) {
		throw new Error("this knife doesn't accepte any beef");
	}

	public void setDictionaries(Dictionaries dictionaries) {
	}

	class Inner {
		private boolean bool;

		public void setBool(boolean bool) {
			this.bool = bool;
			log.info("set property: bool=" + bool);
		}
		
		public boolean isBool() {
			return bool;
		}
	}

}
