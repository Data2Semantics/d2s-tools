package org.data2semantics.tools.experiments;

import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;

public class DataSetParameters {
	private RDFDataSet rdfDataSet;
	private String property;
	private List<String> blackList;
	private int depth;
	private boolean includeInverse;
	private boolean includeInference;
	
	public DataSetParameters(RDFDataSet rdfDataSet, String property,
			List<String> blackList, int depth, boolean includeInverse,
			boolean includeInference) {
		super();
		this.rdfDataSet = rdfDataSet;
		this.property = property;
		this.blackList = blackList;
		this.depth = depth;
		this.includeInverse = includeInverse;
		this.includeInference = includeInference;
	}

	public RDFDataSet getRdfDataSet() {
		return rdfDataSet;
	}

	public void setRdfDataSet(RDFDataSet rdfDataSet) {
		this.rdfDataSet = rdfDataSet;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public List<String> getBlackList() {
		return blackList;
	}

	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public boolean isIncludeInverse() {
		return includeInverse;
	}

	public void setIncludeInverse(boolean includeInverse) {
		this.includeInverse = includeInverse;
	}

	public boolean isIncludeInference() {
		return includeInference;
	}

	public void setIncludeInference(boolean includeInference) {
		this.includeInference = includeInference;
	}
}
