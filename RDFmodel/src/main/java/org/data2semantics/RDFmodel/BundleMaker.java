package org.data2semantics.RDFmodel;

public class BundleMaker<T> {

	private IndexMap<Object> _map = new IndexMap<Object>();
	
	public Bundle<T> bundle(T obj) { return new Bundle<T>(obj, _map.map(obj)); }
	public Bundle<T> bundle(T obj, Object key) { return new Bundle<T>(obj, _map.map(key)); }
	
	public boolean isNew(Bundle<T> oip) { return oip._i == _map.size()-1; }
	public int size() { return _map.size(); }
}

class Bundle<T> {
	private T _t;
	int _i;
	
	public Bundle(T t, int i) { _t = t; _i = i; }
	
	public int getInt() { return _i; }
	public T   getObj() { return _t; }
	
	@Override public int hashCode() { return _i; }
	
	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Bundle<?> other = (Bundle<?>) obj;
		return _i == other._i;
	}
	
}

