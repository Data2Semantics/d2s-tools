package org.data2semantics.RDFmodel;

public interface CoderFactory<T,C> {

	public Coder<T> construct(C conditional);
	
}
