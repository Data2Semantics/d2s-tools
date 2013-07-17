package org.data2semantics.proppred.kernels.graphkernels;

import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;



/**
 * GraphKernel interface, compute a kernel matrix on a list of DirectedMultigraphWithRoot's
 * 
 */
public interface GraphKernel extends Kernel {
		
	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs);
	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs, List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs);
	
}
