package org.data2semantics.proppred.learners.evaluation;

import org.data2semantics.proppred.learners.Prediction;

public class Task1Score implements EvaluationFunction {

	public double computeScore(double[] target, Prediction[] prediction) {
		double total = 0;
		for (int i = 0; i < target.length; i++) {
			total += score(target[i], Math.round(prediction[i].getLabel()));
		}
		total /= target.length;
		return total;
	}

	public boolean isBetter(double scoreA, double scoreB) {
		return (scoreA < scoreB) ? true : false;
	}
	
	private double score(double scoreA, double scoreB) {
		return (2.0 / (1.0 + Math.exp(-1.0 * Math.abs(scoreA - scoreB) / Math.min(scoreA, scoreB))));
	}
	
	public String getLabel() {
		return "T1 Score";
	}
	
	public boolean isHigherIsBetter() {
		return false;
	}
}
