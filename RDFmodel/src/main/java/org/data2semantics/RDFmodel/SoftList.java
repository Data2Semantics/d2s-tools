package org.data2semantics.RDFmodel;

import java.util.ArrayList;

public class SoftList<E> extends ArrayList<E> {
	
	private static final long serialVersionUID = 34118092483375321L;

	public SoftList() { super(); }
	@Override
	public E set(int ix, E item) {
		if (ix >= size()) {
			ensureCapacity(ix+1); // make sure at most one resize occurs
			for (int i=size(); i<=ix; i++) add(null);
		}
		return super.set(ix, item);
	}	
			
	@Override public E get(int ix) { return ix>=size() ? null : super.get(ix); }
}
