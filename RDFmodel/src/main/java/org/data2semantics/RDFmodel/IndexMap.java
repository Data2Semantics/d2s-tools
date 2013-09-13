package org.data2semantics.RDFmodel;
import java.util.HashMap;


public class IndexMap<T> extends HashMap<T, Integer> {

	private static final long serialVersionUID = -4039446230914102275L;

	public int map(T item) {
		Integer ix = get(item);
		if (ix==null) { ix = size(); put(item, ix); }
		return ix;
	}
	
}
