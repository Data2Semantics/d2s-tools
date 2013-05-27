package org.data2semantics.proppred.libsvm.evaluation;

import org.data2semantics.proppred.libsvm.Prediction;

public class Task1ScoreForBins implements EvaluationFunction {
	private double[] bins;
	
	public Task1ScoreForBins(double[] bins) {
		this.bins = bins;
	}
	
	public double computeScore(double[] target, Prediction[] prediction) {
		double total = 0;
		for (int i = 0; i < target.length; i++) {
			double scoreA = target[i];
			double scoreB = (bins[(int) prediction[i].getLabel()] + bins[(int) prediction[i].getLabel()-1]) / 2.0;		
			total += score(scoreA,scoreB);
		}
		total /= target.length;
		return total;
	}

	public boolean isBetter(double scoreA, double scoreB) {
		return (scoreA < scoreB) ? true : false;
	}
	
	private double score(double scoreA, double scoreB) {
		return (2.0 / (1.0 + Math.exp(-1.0 * Math.abs(scoreA - scoreB) / Math.min(scoreA, scoreB)))) - 1;
	}
	
	public String getLabel() {
		return "T1 Score";
	}
	
	public boolean isHigherIsBetter() {
		return false;
	}
}
