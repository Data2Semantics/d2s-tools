package org.data2semantics.proppred.kernels;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;



/**
 * Abstract GraphKernel class, implements some methods needed for all graph kernels and defines abstract compute methods
 * that need to be implemented by any graph kernel extending this class. The class has a protected normalize boolean, which can be used to 
 * indicate whether a kernel should be normalized. However the implementing class is responsible for doing this normalization, for which the 
 * provided normalize methods can be used. This design decision was made so that the implementation of the compute methods is not restricted.
 * 
 * TODO generalize to graphs that do not necessarily have a root node. However, this requires a reimplementation of the IntersectionTreeKernels
 * 
 * @author Gerben
 *
 * @param <G> Any graph that extends the DirectedMultigraphWihRoot can be used
 */
public abstract class GraphKernel<G extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> extends Kernel {
	protected static final String ROOTID = "ROOT1337";
		
	public GraphKernel() {
		this(true);
	}

	public GraphKernel(boolean normalize) {
		super(normalize);
		this.label = "Graph Kernel";
	}
		
	public abstract double[][] compute(List<? extends G> trainGraphs);
	
	public abstract double[][] compute(List<? extends G> trainGraphs, List<? extends G> testGraphs);
		
	
	
	
	
}
