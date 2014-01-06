package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.proppred.kernels.Bucket;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.learners.SparseVector;

import org.nodes.MapUTGraph;
import org.nodes.UGraph;
import org.nodes.ULink;
import org.nodes.UNode;
import org.nodes.UTGraph;

/**
 * Class implementing the Weisfeiler-Lehman graph kernel for general Undirected graphs.
 * The current implementation can be made more efficient, since the compute function for test examples recomputes the label dictionary, instead
 * of reusing the one created during training. This makes the applicability of the implementation slightly more general.
 * 
 * TODO include a boolean for saving the labelDict to speed up computation of the kernel in the test phase.
 * 
 * 
 * @author Gerben
 *
 */
public class WLUSubTreeKernel implements MoleculeKernel<UGraph<String>>, LinearMoleculeKernel<UGraph<String>> {
	private int iterations = 2;
	protected String label;
	protected boolean normalize;

	/**
	 * Construct a WLSubTreeKernel. 
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WLUSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
		this.label = "WL Undirected SubTree Kernel, it=" + iterations;
	}	

	public WLUSubTreeKernel(int iterations) {
		this(iterations, true);
	}


	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(List<UGraph<String>> trainGraphs) {
		// Have to use UGraph implementation for copying.
		// List<UTGraph<StringLabel,?>> graphs = copyGraphs(trainGraphs);
		List<UTGraph<StringLabel,Object>> graphs = copyGraphs(trainGraphs);

		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();

		int startLabel = 1;
		int currentLabel = 1;

		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		computeFVs(graphs, featureVectors, 1, currentLabel-1);
		// Math.sqrt(1.0 / ((double) (iterations + 1)))

		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFVs(graphs, featureVectors, 1, currentLabel-1);
			// Math.sqrt((2.0 + i) / ((double) (iterations + 1)))		
		}

		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}

	public double[][] compute(List<UGraph<String>> trainGraphs) {
		double[][] kernel = KernelUtils.initMatrix(trainGraphs.size(), trainGraphs.size());
		computeKernelMatrix(computeFeatureVectors(trainGraphs), kernel);				
		return kernel;
	}


	/**
	 * First step in the Weisfeiler-Lehman algorithm, applied to directedgraphs with edge labels.
	 * 
	 * @param graphs
	 * @param startLabel
	 * @param currentLabel
	 */
	private void relabelGraphs2MultisetLabels(List<UTGraph<StringLabel,Object>> graphs, int startLabel, int currentLabel) {
		Map<String, Bucket<UNode<StringLabel>>> buckets = new HashMap<String, Bucket<UNode<StringLabel>>>();

		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			buckets.put(Integer.toString(i), new Bucket<UNode<StringLabel>>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		for (UTGraph<StringLabel,?> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				buckets.get(vertex.label().toString()).getContents().addAll(vertex.neighbors());
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (UTGraph<StringLabel,?> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				vertex.label().append("_");
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < currentLabel; i++) {
			// Process vertices
			Bucket<UNode<StringLabel>> bucket = buckets.get(Integer.toString(i));			
			for (UNode<StringLabel> vertex : bucket.getContents()) {
				vertex.label().append(bucket.getLabel());
				vertex.label().append("_");
			}
		}
	}

	/**
	 * Second step in the WL algorithm. We compress the long labels into new short labels
	 * 
	 * @param graphs
	 * @param labelDict
	 * @param currentLabel
	 * @return
	 */
	private int compressGraphLabels(List<UTGraph<StringLabel,Object>> graphs, Map<String, String> labelDict, int currentLabel) {
		String label;

		for (UTGraph<StringLabel,Object> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				label = labelDict.get(vertex.label().toString());
				if (label == null) {
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(vertex.label().toString(), label);
				}
				vertex.label().clear();
				vertex.label().append(label);
			}
		}
		return currentLabel;
	}


	/**
	 * Compute feature vector for the graphs based on the label dictionary created in the previous two steps
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param startLabel
	 * @param currentLabel
	 */
	private void computeFVs(List<UTGraph<StringLabel,Object>> graphs, SparseVector[] featureVectors, double weight, int lastIndex) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);
			//featureVectors[i] = new SparseVector();
			//featureVectors[i] = new double[currentLabel - startLabel];		
			//Arrays.fill(featureVectors[i], 0.0);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (UNode<StringLabel> vertex : graphs.get(i).nodes()) {
				index = Integer.parseInt(vertex.label().toString());	
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				//featureVectors[i][index] += 1.0;
			}
		}
	}



	/**
	 * Use the feature vectors to compute a kernel matrix.
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param kernel
	 * @param iteration
	 */
	private void computeKernelMatrix(SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
				kernel[j][i] = kernel[i][j];
			}
		}
	}

	private static List<UTGraph<StringLabel,Object>> copyGraphs(List<UGraph<String>> graphs) {
		List<UTGraph<StringLabel,Object>> newGraphs = new ArrayList<UTGraph<StringLabel, Object>>();
				
		for (UGraph<String> graph : graphs) {
			UTGraph<StringLabel,Object> newGraph = new MapUTGraph<StringLabel,Object>();
			for (UNode<String> vertex : graph.nodes()) {
				newGraph.add(new StringLabel(vertex.label()));
			}
			for (ULink<String> edge : graph.links()) {
				newGraph.nodes().get(edge.first().index()).connect(newGraph.nodes().get(edge.second().index()), null);
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}
	
}