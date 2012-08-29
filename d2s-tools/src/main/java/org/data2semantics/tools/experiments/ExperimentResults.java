package org.data2semantics.tools.experiments;

public class ExperimentResults {
	private String label;
	private double accuracy;
	private double f1;
	private double averagePrecision;
	private double rPrecision;
	
	public ExperimentResults() {
		super();
	}

	public String getLabel() {
		return label;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getF1() {
		return f1;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public void setF1(double f1) {
		this.f1 = f1;
	}

	public double getAveragePrecision() {
		return averagePrecision;
	}

	public void setAveragePrecision(double averagePrecision) {
		this.averagePrecision = averagePrecision;
	}

	public double getrPrecision() {
		return rPrecision;
	}

	public void setrPrecision(double rPrecision) {
		this.rPrecision = rPrecision;
	}	
	
}
