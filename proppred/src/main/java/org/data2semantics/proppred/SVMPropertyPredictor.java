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

/* kernel, LibSVMParameters, extractionDepth (other extraction parameters maybe), class/regression
 * 
 * 
 */


/**
 * This class is a Support Vector Machine and {@link org.data2semantics.proppred.kernels.GraphKernel} based implementation of the {@link PropertyPredictor} Interface.
 * 
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
	private double[] cs = { 0.001, 0.01, 0.1, 1.0, 10, 100, 1000 };
	private int extractionDepth = 2;

	public SVMPropertyPredictor() {
		kernel = new WLSubTreeKernel(0, true);

		trainGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>();
		trainLabels = new ArrayList<String>();
		labelMap = new TreeMap<String, Integer>();
		valueMap = new TreeMap<String, Value>();

		params = new LibSVMParameters(LibSVMParameters.EPSILON_SVR);
		params.setItParams(cs);
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.data2semantics.proppred.PropertyPredictor#train(org.data2semantics
	 * .tools.rdf.RDFDataSet, java.util.List, java.util.List)
	 */
	public void train(RDFDataSet dataset, List<Resource> instances,
			List<Value> labels) {
		Map<Resource, List<Statement>> dummyMap = new HashMap<Resource, List<Statement>>();
		for (Resource instance : instances) {
			dummyMap.put(instance, null);
		}
		train(dataset, instances, labels, dummyMap);
	}

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

		System.out.println(labels);
		System.out.println(Arrays.toString(target));
		
		LibSVMPrediction[] pred = LibSVM.crossValidate(kernelMatrix, target,
				params, 10);
		
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
				} else {	
					predictions.add(valueMap.get(revMap.get((int) p)));
				}
			}
		}

		return predictions;
	}

}
