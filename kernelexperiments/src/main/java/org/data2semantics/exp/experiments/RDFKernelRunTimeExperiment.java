package org.data2semantics.exp.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.Prediction;
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
