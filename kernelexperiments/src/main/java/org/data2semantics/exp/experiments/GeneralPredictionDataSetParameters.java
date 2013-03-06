package org.data2semantics.exp.experiments;

import java.util.List;
import java.util.Map;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public class GeneralPredictionDataSetParameters {
	private RDFDataSet rdfDataSet;
	private int depth;
	private boolean includeInverse;
	private boolean includeInference;
	private List<URI> instances;
	private Map<URI, List<Statement>> blacklists;
	

		
	public GeneralPredictionDataSetParameters(RDFDataSet rdfDataSet,
			Map<URI, List<Statement>> blacklists, List<URI> instances, int depth,
			boolean includeInverse, boolean includeInference) {
		super();
		this.rdfDataSet = rdfDataSet;
		this.depth = depth;
		this.includeInverse = includeInverse;
		this.includeInference = includeInference;
		this.instances = instances;
		this.blacklists = blacklists;
	}



	public RDFDataSet getRdfDataSet() {
		return rdfDataSet;
	}

	public void setRdfDataSet(RDFDataSet rdfDataSet) {
		this.rdfDataSet = rdfDataSet;
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

	public List<URI> getInstances() {
		return instances;
	}

	public void setInstances(List<URI> instances) {
		this.instances = instances;
	}



	public Map<URI, List<Statement>> getBlacklists() {
		return blacklists;
	}



	public void setBlacklists(Map<URI, List<Statement>> blacklists) {
		this.blacklists = blacklists;
	}
	
	
}
