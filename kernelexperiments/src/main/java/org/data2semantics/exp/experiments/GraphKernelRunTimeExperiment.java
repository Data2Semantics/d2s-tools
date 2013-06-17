package org.data2semantics.exp.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.GraphKernel;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.Prediction;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFSingleDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
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
