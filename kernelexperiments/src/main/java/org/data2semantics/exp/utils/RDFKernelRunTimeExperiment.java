package org.data2semantics.exp.utils;

import java.util.List;

import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFKernelRunTimeExperiment extends KernelExperiment<RDFGraphKernel> {
	private LibSVMParameters svmParms;
	private List<Value> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private Result compR;



	public RDFKernelRunTimeExperiment(RDFGraphKernel kernel, long[] seeds,
			LibSVMParameters svmParms, RDFDataSet dataset,
			List<Resource> instances,  List<Value> labels, List<Statement> blackList) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;

		compR = new Result();

		results.add(compR);
	}



	public void run() {		
		long tic, toc;

		double[] comp = new double[1];	
		compR.setLabel("kernel comp time");

		tic = System.currentTimeMillis();
		kernel.compute(dataset, instances, blackList);
		toc = System.currentTimeMillis();		
		comp[0] = toc - tic;		


		compR.setScores(comp);
	}


}
