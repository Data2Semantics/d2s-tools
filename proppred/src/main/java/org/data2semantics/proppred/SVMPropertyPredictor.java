package org.data2semantics.proppred;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.data2semantics.proppred.kernels.GraphKernel;
import org.data2semantics.proppred.kernels.WLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMModel;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.LibSVMPrediction;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import cern.colt.Arrays;


/**
 * This class is a Support Vector Machine (using {@link libsvm.LibSVM}) and {@link org.data2semantics.proppred.kernels.GraphKernel} based implementation of the {@link PropertyPredictor} Interface.
 * Classification, Regression and Outlier Detection (via One-Class SVM) are all supported.
 * 
 * @author Gerben
 *
 */
public class SVMPropertyPredictor implements PropertyPredictor {
	private GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> kernel;
	private List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs;
	private List<String> trainLabels;
	private Map<String, Value> valueMap;
	private Map<String, Integer> labelMap;
	private LibSVMModel trainedModel;

	private LibSVMParameters params;
	private int extractionDepth;

	
	
	/**
	 * Construct the default SVMPropertyPredictor. A C-SVC support vector machine is used, with a WLSubtreeKernel and extraction depth 2. This setting is good to start with for a new classification task.
	 * 
	 */
	public SVMPropertyPredictor() {
		this(new WLSubTreeKernel(2,true), 2);
		this.setDefaultLibSVMParams();
	}
	
	/**
	 * Default C-SVM settings, but one can specify the graph kernel used.
	 * 
	 * @param kernel an instance of a GraphKernel
	 */
	public SVMPropertyPredictor(GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> kernel) {
		this(kernel, 2);
	}
	
	/**
	 * Default C-SVM settings.
	 * 
	 * @param kernel an instance of GraphKernel
	 * @param extractionDepth the depth used in subgraph extraction
	 */
	public SVMPropertyPredictor(GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> kernel, int extractionDepth) {
		this.kernel = kernel;
		this.extractionDepth = extractionDepth;	
		this.setDefaultLibSVMParams();
		
		trainGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>();
		trainLabels = new ArrayList<String>();
		labelMap = new TreeMap<String, Integer>();
		valueMap = new TreeMap<String, Value>();
	}
	

	/**
	 * Using the params object different algorithms from LibSVM can be chosen (C-SVC, nu-SVC for classification, nu-SVR, epsilon-SVR for regression and one-class for outlier detection).
	 * 
	 * 
	 * @param kernel an instance of GraphKernel
	 * @param extractionDepth depth used in subgraph extraction
	 * @param params parameters for the LibSVM library. When using algorithms with the nu parameter (nu-SVC,nu-SVR and one-class) make sure the iteration parameters are set between 0 and 1.
	 */
	public SVMPropertyPredictor(GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> kernel, int extractionDepth, LibSVMParameters params) {
		this(kernel, extractionDepth);
		this.params = params;
	}
	
	private void setDefaultLibSVMParams() {
		this.params = new LibSVMParameters(LibSVMParameters.C_SVC);
		double[] cs = {0.001, 0.01, 0.1, 1.0, 10, 100, 1000};
		this.params.setItParams(cs);
	}
	
	

	public void train(RDFDataSet dataset, List<Resource> instances,
			List<Value> labels) {
		Map<Resource, List<Statement>> dummyMap = new HashMap<Resource, List<Statement>>();
		for (Resource instance : instances) {
			dummyMap.put(instance, null);
		}
		train(dataset, instances, labels, dummyMap);
	}

	
	/**
	 * Used to train an SVM model. For regression SVM models (nu-SVR,epsilon-SVR) the StringValue of the Value labels should be parseable to a double. 
	 * For one-class SVM the labels do not matter, all instances are considered to be part of the class.	 * 
	 * 
	 */
	public void train(RDFDataSet dataset, List<Resource> instances,
			List<Value> labels, Map<Resource, List<Statement>> blackLists) {
		DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> subGraph;

		for (Resource instance : instances) {
			subGraph = GraphFactory.copyDirectedGraph2GraphWithRoot(
					GraphFactory.createDirectedGraph(dataset.getSubGraph(
							instance, extractionDepth, false, false,
							blackLists.get(instance))), instance.toString());
			trainGraphs.add(subGraph);
		}
		System.out.println("Constructed dataset.");

		double[][] kernelMatrix = kernel.compute(trainGraphs);
		System.out.println("Computed kernel.");

		double[] target = new double[labels.size()];

		if (params.getAlgorithm() == LibSVMParameters.EPSILON_SVR || params.getAlgorithm() == LibSVMParameters.NU_SVR) {
			for (int i = 0; i < labels.size(); i++) {
				target[i] = Double.parseDouble(labels.get(i).stringValue());
			}
			
		} else {
			for (Value label : labels) {
				trainLabels.add(label.toString());
				valueMap.put(label.toString(), label);
			}
			target = LibSVM.createTargets(trainLabels, labelMap);
		}
	
		// Just to indicate the performance of the predictor, we run cross-validation first
		LibSVMPrediction[] pred = LibSVM.crossValidate(kernelMatrix, target, params, 10);
		
		if (params.getAlgorithm() == LibSVMParameters.EPSILON_SVR || params.getAlgorithm() == LibSVMParameters.NU_SVR) {
			System.out.println("10-fold CV MSE: "
					+ LibSVM.computeMeanSquaredError(target, LibSVM.extractLabels(pred)));
		} else {
			System.out.println("10-fold CV accuracy: "
					+ LibSVM.computeAccuracy(target, LibSVM.extractLabels(pred)));
		}		
		
		trainedModel = LibSVM.trainSVMModel(kernelMatrix, target, params);
		System.out.println("Trained model.");
	}

	public List<Value> predict(RDFDataSet dataset, List<Resource> instances) {
		Map<Resource, List<Statement>> dummyMap = new HashMap<Resource, List<Statement>>();
		for (Resource instance : instances) {
			dummyMap.put(instance, null);
		}
		return predict(dataset, instances, dummyMap);
	}

	
	/**
	 * Predict for new instances using a trained SVM model. For regression SVMs the Value's contain a double (as String). 
	 * For one-class the Values contain a String: "normal" for instances falling within the model and "outlier" for instances outside the model.
	 * 
	 */
	public List<Value> predict(RDFDataSet dataset, List<Resource> instances,
			Map<Resource, List<Statement>> blackLists) {
		List<Value> predictions = new ArrayList<Value>();

		if (trainedModel == null) {
			System.out.println("Please train first.");
		} else {

			DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> subGraph;
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>();

			for (Resource instance : instances) {
				subGraph = GraphFactory
						.copyDirectedGraph2GraphWithRoot(GraphFactory
								.createDirectedGraph(dataset.getSubGraph(
										instance, extractionDepth, false,
										false, blackLists.get(instance))),
										instance.toString());
				testGraphs.add(subGraph);
			}
			System.out.println("Constructed prediction set.");

			double[][] kernelMatrix = kernel.compute(trainGraphs, testGraphs);
			System.out.println("Computed kernel.");

			double[] pred = LibSVM.extractLabels(LibSVM.testSVMModel(
					trainedModel, kernelMatrix));
			Map<Integer, String> revMap = LibSVM.reverseLabelMap(labelMap);

			for (double p : pred) {
				if (params.getAlgorithm() == LibSVMParameters.EPSILON_SVR || params.getAlgorithm() == LibSVMParameters.NU_SVR) {
					predictions.add(dataset.createLiteral(Double.toString(p)));
				} else if (params.getAlgorithm() == LibSVMParameters.ONE_CLASS) {
					if (p == 1) {
						predictions.add(dataset.createLiteral("normal"));
					} else {
						predictions.add(dataset.createLiteral("outlier"));
					}
				} else {
					predictions.add(valueMap.get(revMap.get((int) p)));
				}
			}
		}

		return predictions;
	}

}
