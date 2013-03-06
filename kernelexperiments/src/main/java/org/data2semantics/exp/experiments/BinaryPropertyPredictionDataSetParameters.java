package org.data2semantics.exp.experiments;

import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.URI;

public class BinaryPropertyPredictionDataSetParameters {
	private RDFDataSet rdfDataSet;
	private String property;
	private String invProperty;
	private String classObject;
	private String instanceProperty;
	private String instanceObject;
	private int depth;
	private boolean includeInverse;
	private boolean includeInference;
	private List<URI> instances;
	
	public BinaryPropertyPredictionDataSetParameters(RDFDataSet rdfDataSet,
			String property, String invProperty, String classObject,
			String instanceProperty, String instanceObject, int depth,
			boolean includeInverse, boolean includeInference) {
		super();
		this.rdfDataSet = rdfDataSet;
		this.property = property;
		this.invProperty = invProperty;
		this.classObject = classObject;
		this.instanceProperty = instanceProperty;
		this.instanceObject = instanceObject;
		this.depth = depth;
		this.includeInverse = includeInverse;
		this.includeInference = includeInference;
	}
		
	public BinaryPropertyPredictionDataSetParameters(RDFDataSet rdfDataSet,
			String property, String invProperty, String classObject, List<URI> instances, int depth,
			boolean includeInverse, boolean includeInference) {
		super();
		this.rdfDataSet = rdfDataSet;
		this.property = property;
		this.invProperty = invProperty;
		this.classObject = classObject;
		this.depth = depth;
		this.includeInverse = includeInverse;
		this.includeInference = includeInference;
		this.instances = instances;
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

	public String getInvProperty() {
		return invProperty;
	}

	public void setInvProperty(String invProperty) {
		this.invProperty = invProperty;
	}

	public String getClassObject() {
		return classObject;
	}

	public void setClassObject(String classObject) {
		this.classObject = classObject;
	}

	public String getInstanceProperty() {
		return instanceProperty;
	}

	public void setInstanceProperty(String instanceProperty) {
		this.instanceProperty = instanceProperty;
	}

	public String getInstanceObject() {
		return instanceObject;
	}

	public void setInstanceObject(String instanceObject) {
		this.instanceObject = instanceObject;
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
}
