package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.List;

class Bucket<T> {
	private String label;
	private List<T> contents;

	public Bucket(String label) {
		this.label = label;
		contents = new ArrayList<T>();
	}

	public List<T> getContents() {
		return contents;
	}

	public String getLabel() {
		return label;
	}
}
