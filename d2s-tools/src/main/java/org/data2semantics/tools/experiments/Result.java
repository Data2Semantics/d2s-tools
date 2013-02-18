package org.data2semantics.tools.experiments;

public class Result {
	private double[] scores;
	private String label;
	
	public Result() {
		this.scores = new double[1];
		this.scores[0] = 0;
		this.label = "Empty Result";
		
	}
	
	public Result(double[] scores, String label) {
		this.scores = scores;
		this.label = label;
	}

	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public double getScore() {
		double total = 0;
		for (double score : scores) {
			total += score;
		}
		return total / scores.length;
	}
	
	
}
