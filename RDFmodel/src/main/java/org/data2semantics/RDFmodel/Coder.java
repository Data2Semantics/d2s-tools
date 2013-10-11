package org.data2semantics.RDFmodel;

public interface Coder<T> {
	public void encode(CoderContext C, T obj);
}
