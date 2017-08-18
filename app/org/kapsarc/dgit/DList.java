package org.kapsarc.dgit;

import java.util.ArrayList;

public class DList<T> extends ArrayList<T> {
	@Override
	public String toString() {
		String ret = "";
		boolean fresh = true;
		for (int i = 0; i < size(); i++) {
			if (fresh) {
				ret += get(i).toString();
			} else {
				ret = ret + " , " + get(i).toString();
			}
		}
		return ret;
	}

	public String toString(String prefix, String suffix) {
		return prefix + toString() + suffix;
	}

	public static void main(String[] args) {
		DList<String> l = new DList<>();
		l.add("123");
		l.add("222");
		System.out.println(l);
	}
}
