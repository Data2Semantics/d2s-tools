package org.data2semantics.tools.libsvm;

public class LibSVMPrediction {
	private double label;
	private double[] decisionValue;
	private double probability;
	
	public LibSVMPrediction(double label) {
		this.label = label;
	}
	
	public double getLabel() {
		return label;
	}
	public void setLabel(double label) {
		this.label = label;
	}
	public double[] getDecisionValue() {
		return decisionValue;
	}
	public void setDecisionValue(double[] decisionValue) {
		this.decisionValue = decisionValue;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	
	

}
