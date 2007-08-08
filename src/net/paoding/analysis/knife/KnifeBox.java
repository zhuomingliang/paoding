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
package net.paoding.analysis.knife;

import java.util.ArrayList;
import java.util.List;

/**
 * KnifeBox负责决策当遇到字符串指定位置时应使用的Knife对象.
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see Paoding
 * 
 * @since 1.0
 * 
 */
public class KnifeBox implements Knife {

	private ArrayList<Knife> knives = new ArrayList<Knife>();

	public KnifeBox() {
	}

	public KnifeBox(List<Knife> knives) {
		this.setKnives(knives);
	}

	public void addKnife(Knife k) {
		knives.add(k);
	}

	public void removeKnife(Knife k) {
		knives.remove(k);
	}

	public List<Knife> getKnives() {
		ArrayList<Knife> knives = new ArrayList<Knife>();
		knives.addAll(this.knives);
		return knives;
	}

	public void setKnives(List<Knife> knives) {
		this.knives.clear();
		this.knives.addAll(knives);
	}

	public boolean assignable(CharSequence beaf, int index) {
		return true;
	}

	public int dissect(Collector collector, CharSequence beaf, int offset) {
		int size = knives.size();
		Knife knife;
		for (int i = 0; i < size; i++) {
			knife = knives.get(i);
			if (knife.assignable(beaf, offset)) {
				return knife.dissect(collector, beaf, offset);
			}
		}
		return ++offset;
	}
}
