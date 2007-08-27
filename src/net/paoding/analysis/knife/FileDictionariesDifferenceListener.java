package net.paoding.analysis.knife;

import java.util.LinkedList;
import java.util.List;

import net.paoding.analysis.dictionary.support.detection.Difference;
import net.paoding.analysis.dictionary.support.detection.DifferenceListener;
import net.paoding.analysis.dictionary.support.detection.Node;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 2.0.2
 * 
 */
public class FileDictionariesDifferenceListener implements DifferenceListener {

	private FileDictionaries dictionaries;

	private KnifeBox knifeBox;

	public FileDictionariesDifferenceListener() {
	}

	public FileDictionariesDifferenceListener(Dictionaries dictionaries,
			KnifeBox knifeBox) {
		this.dictionaries = (FileDictionaries) dictionaries;
		this.knifeBox = knifeBox;
	}

	public Dictionaries getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(Dictionaries dictionaries) {
		this.dictionaries = (FileDictionaries) dictionaries;
	}

	public KnifeBox getKnifeBox() {
		return knifeBox;
	}

	public void setKnifeBox(KnifeBox knifeBox) {
		this.knifeBox = knifeBox;
	}

	public synchronized boolean on(Difference diff) {
		List<Node> all = new LinkedList<Node>();
		all.addAll(diff.getDeleted());
		all.addAll(diff.getModified());
		all.addAll(diff.getNewcome());
		for (Node node : all) {
			if (node.isFile()) {
				dictionaries.refreshDicWords(node.getPath());
			}
		}
		for (Knife knife : knifeBox.getKnives()) {
			if (knife instanceof DictionariesWare) {
				((DictionariesWare) knife).setDictionaries(dictionaries);
			}
		}
		return true;
	}

}
