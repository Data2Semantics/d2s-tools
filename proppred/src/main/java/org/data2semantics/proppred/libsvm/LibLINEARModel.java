package org.data2semantics.proppred.libsvm;
import de.bwaldvogel.liblinear.Model;


public class LibLINEARModel {
	private Model model;
	
	LibLINEARModel(Model model) {
		this.model = model;
	}
	
	Model getModel() {
		return model;
	}
	
	public WeightIndexPair[][] getFeatureWeights() {
		WeightIndexPair[][] weights = new WeightIndexPair[model.getNrClass()][];
		double[] llw = model.getFeatureWeights();
		
		for (int i = 0; i < weights.length; i++) {
			weights[i] = new WeightIndexPair[model.getNrFeature()];
		}
		
		for (int i = 0; i < llw.length; i++) {
			weights[i % model.getNrClass()][i / model.getNrClass()] = new WeightIndexPair(llw[i], (i/model.getNrClass())+1);
		}
		
		return weights;
	}
	
	
	public class WeightIndexPair implements Comparable<WeightIndexPair> {
		private double weight;
		private int index;
		
		public int compareTo(WeightIndexPair weight2) {
			return -Double.compare(weight, weight2.weight); 
		}

		public WeightIndexPair(double weight, int index) {
			super();
			this.weight = weight;
			this.index = index;
		}

		public double getWeight() {
			return weight;
		}

		public int getIndex() {
			return index;
		}
	}
	
}
