package org.data2semantics.exp.experiments;

public class ExperimentResults {
	private String label;
	private Result accuracy;
	private Result f1;
	private Result averagePrecision;
	private Result rPrecision;
	private Result ndcg;
	
	public ExperimentResults() {
		super();
	}

	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public Result getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Result accuracy) {
		this.accuracy = accuracy;
	}

	public Result getF1() {
		return f1;
	}

	public void setF1(Result f1) {
		this.f1 = f1;
	}

	public Result getAveragePrecision() {
		return averagePrecision;
	}

	public void setAveragePrecision(Result averagePrecision) {
		this.averagePrecision = averagePrecision;
	}

	public Result getrPrecision() {
		return rPrecision;
	}

	public void setrPrecision(Result rPrecision) {
		this.rPrecision = rPrecision;
	}

	public Result getNdcg() {
		return ndcg;
	}

	public void setNdcg(Result ndcg) {
		this.ndcg = ndcg;
	}

	
	
}
