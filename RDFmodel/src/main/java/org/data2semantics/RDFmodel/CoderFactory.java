package org.data2semantics.RDFmodel;

public interface CoderFactory<T> {
	public Coder<T> build();
}
