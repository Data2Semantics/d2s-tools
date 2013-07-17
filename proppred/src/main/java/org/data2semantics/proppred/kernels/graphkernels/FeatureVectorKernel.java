package org.data2semantics.proppred.kernels.graphkernels;

import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;


/**
 * Interface describing a FeatureVectorKernel. Such kernel's should be able to compute SparseVector on the input data (instead of a kernel matrix).
 * 
 * @author Gerben
 *
 */
public interface FeatureVectorKernel extends Kernel {
	public SparseVector[] computeFeatureVectors(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs);
	public SparseVector[] computeFeatureVectors(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs, List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs);
}
