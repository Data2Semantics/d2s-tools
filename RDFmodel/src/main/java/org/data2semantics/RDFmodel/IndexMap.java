package org.data2semantics.RDFmodel;
import java.util.HashMap;
import java.util.List;


public class IndexMap<T> extends HashMap<T, Integer> {

	private static final long serialVersionUID = -4039446230914102275L;

	public int map(T item) {
		Integer ix = get(item);
		if (ix==null) { ix = size(); put(item, ix); }
		return ix;
	}
	
	public List<T> invert() {
		List<T> res = new SoftList<T>();
		for (java.util.Map.Entry<T, Integer> entry : entrySet()) 
			res.set(entry.getValue(), entry.getKey());
		return res;
	}
	
}
