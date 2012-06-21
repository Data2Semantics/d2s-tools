package org.data2semantics.tools.libsvm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibSVMWrapper {

	public static double[] crossValidate(double[][] kernel, double[] target, double c) {
		svm_parameter params = new svm_parameter();
		params.kernel_type = params.PRECOMPUTED;
		params.svm_type = params.C_SVC;
		params.C = c;
		params.nr_weight = 0;
		params.eps = 0.0001;
		params.shrinking = 1;
		params.probability = 0;
		double[] prediction = new double[target.length];
		
		svm.svm_cross_validation(createSVMProblem(kernel, target), params, target.length, prediction);
		
		return prediction;
	}
	
	
	public static svm_problem createSVMProblem(double[][] kernel, double[] target) {
		svm_problem prob = new svm_problem();
		svm_node[][] nodes = new svm_node[target.length][target.length + 1];
		
		prob.l = target.length;
		prob.y = target;
		prob.x = nodes;
		
		for (int i = 0; i < nodes.length; i++) {
			nodes[i][0] = new svm_node();
			nodes[i][0].index = 0;
			nodes[i][0].value = i + 1;
			
			for (int j = 1; j < nodes[i].length; j++) {
				nodes[i][j] = new svm_node();
				nodes[i][j].index = 0;
				nodes[i][j].value = kernel[i][j-1];
			}
		}		
		return prob;		
	}
	
	public static double[] createTargets(List<String> labels) {
		Map<String, Integer> labelMap = new HashMap<String, Integer>();
		double[] targets = new double[labels.size()];
		int t = 0;
		int i = 0;
		
		for (String label : labels) {
			if (!labelMap.containsKey(label)) {
				t += 1;
				labelMap.put(label, t);
			} else {
				t = labelMap.get(label);
			}
			targets[i] = t;
			i++;
		}
		return targets;
	}
	
	public static double computeAccuracy(double[] target, double[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == prediction[i]) {
				correct += 1;
			}
		}
		return correct / ((double) target.length);		
	}

	public static double computeMeanAccuracy(double[] target, double[] prediction) {
		Map<Double, Double> targetCounts = computeClassCounts(target);
		double acc = 0, accTemp = 0;
		
		for (double label : targetCounts.keySet()) {
			for (int i = 0; i < prediction.length; i++) {
				if (prediction[i] == label && target[i] == label) {
					accTemp += 1;
				}
			}
			acc += (accTemp / targetCounts.get(label));
			accTemp = 0;
		}	
		return acc / ((double) targetCounts.size());
	}
	
	public static Map<Double, Double> computeClassCounts(double[] target) {
		Map<Double, Double> counts = new HashMap<Double, Double>();

		for (int i = 0; i < target.length; i++) {
			if (!counts.containsKey(target[i])) {
				counts.put(target[i], 1.0);
			} else {
				counts.put(target[i], counts.get(target[i]) + 1);
			}
		}
		return counts;
	}	
}
