package org.data2semantics.proppred.learners.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationUtils {

	public static <T> List<Double> createTarget(List<T> labels) {
		Map<T, Integer> labelMap = new HashMap<T, Integer>();
		List<Double> target = new ArrayList<Double>();
		int t = 0;
		
		for (T label : labels) {
			if (!labelMap.containsKey(label)) {
				t += 1;
				labelMap.put(label, t);
			} 
			target.add((double) labelMap.get(label));	
		}	
		return target;	
	}
	
	public static double[] target2Doubles(List<Double> target) {
		double[] ret = new double[target.size()];
		for (int i = 0; i < target.size(); i++) {
			ret[i] = target.get(i);
		}
		return ret;
	}
	
	public static Map<Double, Double> computeClassCounts(List<Double> target) {
		Map<Double, Double> counts = new HashMap<Double, Double>();

		for (double t : target) {
			if (!counts.containsKey(t)) {
				counts.put(t, 1.0);
			} else {
				counts.put(t, counts.get(t) + 1);
			}
		}
		return counts;
	}
	
}
