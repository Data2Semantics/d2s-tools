package org.data2semantics.proppred.kernels.graphkernels;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;



/**
 * GraphKernel interface, compute kernel on list of DirectedMultigraphWithRoot
 * 
 */
public interface GraphKernel extends Kernel {
		
	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs);
	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs, List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs);
	
}
