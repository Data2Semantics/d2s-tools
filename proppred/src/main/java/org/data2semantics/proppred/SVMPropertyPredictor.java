package org.data2semantics.proppred;

import java.util.ArrayList;
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
import org.openrdf.model.URI;
import org.openrdf.model.Value;


public class SVMPropertyPredictor implements PropertyPredictor {
	private GraphKernel<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> kernel;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> trainGraphs;
	private List<String> trainLabels;
	private Map<String, Value> valueMap;
	private Map<String, Integer> labelMap;
	private LibSVMModel trainedModel;

	private LibSVMParameters params;
	private double[] cs = {0.001, 0.01, 0.1, 1.0, 10, 100, 1000};
	private int extractionDepth = 2;

	public SVMPropertyPredictor() {
		kernel = new WLSubTreeKernel(2, true);

		trainGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		trainLabels = new ArrayList<String>();
		labelMap = new TreeMap<String, Integer>();
		valueMap = new TreeMap<String, Value>();

		params = new LibSVMParameters(params.C_SVC);
		params.setItParams(cs);
	}



	public void train(RDFDataSet dataset, List<Resource> instances, List<Value> labels) {
		DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> subGraph;

		for (Resource instance : instances) {
			subGraph = GraphFactory.copyDirectedGraph2GraphWithRoot(GraphFactory.createDirectedGraph(dataset.getSubGraph(instance, extractionDepth, false, false, null)), instance.toString());
			trainGraphs.add(subGraph);
		}	
		System.out.println("Constructed dataset.");

		for (Value label : labels) {
			trainLabels.add(label.toString());
			valueMap.put(label.toString(), label);
		}

		double[][] kernelMatrix = kernel.compute(trainGraphs);
		System.out.println("Computed kernel.");

		double[] target = LibSVM.createTargets(trainLabels, labelMap);
		LibSVMPrediction[] pred = LibSVM.crossValidate(kernelMatrix, target, params, 10);		
		System.out.println("10-fold CV accuracy: " + LibSVM.computeAccuracy(target, LibSVM.extractLabels(pred)));

		trainedModel = LibSVM.trainSVMModel(kernelMatrix, target, params);
		System.out.println("Trained model.");
	}

	public List<Value> predict(RDFDataSet dataset, List<Resource> instances) {
		List<Value> predictions = new ArrayList<Value>();

		if (trainedModel == null) {
			System.out.println("Please train first.");
		} else {

			DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> subGraph;
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>();

			for (Resource instance : instances) {
				subGraph = GraphFactory.copyDirectedGraph2GraphWithRoot(GraphFactory.createDirectedGraph(dataset.getSubGraph(instance, extractionDepth, false, false, null)), instance.toString());
				testGraphs.add(subGraph);
			}	
			System.out.println("Constructed prediction set.");

			double[][] kernelMatrix = kernel.compute(trainGraphs, testGraphs);
			System.out.println("Computed kernel.");

			double[] pred = LibSVM.extractLabels(LibSVM.testSVMModel(trainedModel, kernelMatrix));
			Map<Integer, String> revMap = LibSVM.reverseLabelMap(labelMap);	

			for (double p : pred) {
				predictions.add(valueMap.get(revMap.get((int) p)));
			}
		}

		return predictions;
	}

}
