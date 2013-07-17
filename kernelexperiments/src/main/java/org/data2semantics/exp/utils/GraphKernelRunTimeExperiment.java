package org.data2semantics.exp.utils;

import java.util.List;

import org.data2semantics.proppred.kernels.graphkernels.GraphKernel;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.openrdf.model.Value;

public class GraphKernelRunTimeExperiment extends KernelExperiment<GraphKernel> {
	private LibSVMParameters svmParms;
	private List<Value> labels;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> dataset;
	private Result compR;


	public GraphKernelRunTimeExperiment(GraphKernel kernel, long[] seeds,
			LibSVMParameters svmParms, List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> dataset,
			List<Value> labels) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.dataset = dataset;

		compR = new Result();
		results.add(compR);
	}



	public void run() {		
		long tic, toc;

		double[] comp = new double[1];
		compR.setLabel("kernel comp time");


		tic = System.currentTimeMillis();
		double[][] matrix = kernel.compute(dataset);
		toc = System.currentTimeMillis();
		comp[0] = toc-tic;

		compR.setScores(comp);
	}


}
