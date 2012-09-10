package org.data2semantics.tools.experiments;

import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;

public class PropertyPredictionDataSetParameters {
	private RDFDataSet rdfDataSet;
	private String property;
	private String invProperty;
	private int depth;
	private boolean includeInverse;
	private boolean includeInference;
	
	public PropertyPredictionDataSetParameters(RDFDataSet rdfDataSet, String property,
			String invProperty, int depth, boolean includeInverse,
			boolean includeInference) {
		super();
		this.rdfDataSet = rdfDataSet;
		this.property = property;
		this.invProperty = invProperty;
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

	public String getInvProperty() {
		return invProperty;
	}

	public void setInvProperty(String invProperty) {
		this.invProperty = invProperty;
	}	
}
