package org.data2semantics.proppred.kernels;

import java.util.List;

import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

public interface FeatureVectorKernel extends Kernel {
	public SparseVector[] computeFeatureVectors(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs);
	public SparseVector[] computeFeatureVectors(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs, List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs);

}
