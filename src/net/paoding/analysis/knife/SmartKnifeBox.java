package net.paoding.analysis.knife;

public class SmartKnifeBox extends KnifeBox implements Knife {

	public int dissect(Collector collector, CharSequence beaf, int offset) {
		while (offset >= 0 && offset < beaf.length()) {
			offset = super.dissect(collector, beaf, offset);
		}
		return offset;
	}
}
