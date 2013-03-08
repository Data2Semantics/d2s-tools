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
public abstract class GraphKernel<G extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> {
	protected static final String ROOTID = "ROOT1337";
	
	protected String label;
	protected boolean normalize;
	
	public GraphKernel() {
		this(true);
	}

	public GraphKernel(boolean normalize) {
		this.label = "Graph Kernel";
		this.normalize = normalize;
	}
		
	public abstract double[][] compute(List<? extends G> trainGraphs);
	
	public abstract double[][] compute(List<? extends G> trainGraphs, List<? extends G> testGraphs);
		
	public double[][] shuffle(double[][] kernel, long seed) {		
		Double[][] kernelDouble = convert2DoubleObjects(kernel);		
		for (int i = 0; i < kernel.length; i++) {
			Collections.shuffle(Arrays.asList(kernelDouble[i]), new Random(seed));
		}
		Collections.shuffle(Arrays.asList(kernelDouble), new Random(seed));
		return convert2DoublePrimitives(kernelDouble);
	}
	
	public String getLabel() {
		return label;
	}
	
	
	
	protected double[][] normalize(double[][] kernel) {
		double[] ss = new double[kernel.length];
		
		for (int i = 0; i < ss.length; i++) {
			ss[i] = kernel[i][i];
		}
			
		for (int i = 0; i < kernel.length; i++) {
			for (int j = i; j < kernel[i].length; j++) {
				kernel[i][j] /= Math.sqrt(ss[i] * ss[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
		return kernel;
	}
	
	protected double[][] normalize(double[][] kernel, double[] trainSS, double[] testSS) {
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				kernel[i][j] /= Math.sqrt(testSS[i] * trainSS[j]);
			}
		}
		return kernel;
	}
	
	protected double[][] initMatrix(int sizeRows, int sizeColumns) {
		double[][] kernel = new double[sizeRows][sizeColumns];
		for (int i = 0; i < sizeRows; i++) {
			Arrays.fill(kernel[i], 0.0);
		}
		return kernel;
	}
	
	
	/*
	 * Privates 
	 */	
	private Double[][] convert2DoubleObjects(double[][] kernel) {
		Double[][] kernelDouble = new Double[kernel.length][kernel[0].length];
		
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				kernelDouble[i][j] = new Double(kernel[i][j]);
			}
		}
		return kernelDouble;
	}
	
	private double[][] convert2DoublePrimitives(Double[][] kernelDouble) {
		double[][] kernel = new double[kernelDouble.length][kernelDouble[0].length];
		
		for (int i = 0; i < kernelDouble.length; i++) {
			for (int j = 0; j < kernelDouble[i].length; j++) {
				kernel[i][j] = kernelDouble[i][j].doubleValue();
			}
		}
		return kernel;
	}
	
	
	
	
}
