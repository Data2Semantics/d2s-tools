package org.data2semantics.proppred.libsvm.evaluation;

import org.data2semantics.proppred.libsvm.Prediction;

public class Accuracy implements EvaluationFunction {
	
	public double computeScore(double[] target, Prediction[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == prediction[i].getLabel()) {
				correct += 1;
			}
		}
		return correct / ((double) target.length);	
	}

	public boolean isBetter(double scoreA, double scoreB) {
		if (scoreA > scoreB) {
			return true;
		}
		return false;
	}
	
	public String getLabel() {
		return "Accuracy";
	}
	
	
}
