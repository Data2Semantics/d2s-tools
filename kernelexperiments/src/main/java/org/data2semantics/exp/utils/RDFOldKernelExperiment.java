package org.data2semantics.exp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFOldKernelExperiment extends KernelExperiment<RDFGraphKernel> {
	private LibSVMParameters svmParms;
	private List<Value> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private Result accR;
	private Result f1R;
	private Result compR;


	/**
	 * @deprecated Should use {@link RDFGraphKernelExperiment}
	 *  
	 * @param kernel
	 * @param seeds
	 * @param svmParms
	 * @param dataset
	 * @param instances
	 * @param labels
	 * @param blackList
	 */
	
	public RDFOldKernelExperiment(RDFGraphKernel kernel, long[] seeds,
			LibSVMParameters svmParms, RDFDataSet dataset,
			List<Resource> instances,  List<Value> labels, List<Statement> blackList) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
		
		accR  = new Result();
		f1R   = new Result();
		compR = new Result();
		
		results.add(accR);
		results.add(f1R);
		results.add(compR);
	}



	public void run() {		
		long tic, toc;
		
		List<Value> tempLabels = new ArrayList<Value>();
		tempLabels.addAll(labels);

		tic = System.currentTimeMillis();
		double[][] matrix = kernel.compute(dataset, instances, blackList);
		toc = System.currentTimeMillis();
		

		

		
		double[] acc = new double[seeds.length];
		double[] f1 = new double[seeds.length];
		
		accR.setLabel("Accuracy");
		f1R.setLabel("F1");
		
		compR.setLabel("kernel comp time");
		
		for (int j = 0; j < seeds.length; j++) {
			matrix = KernelUtils.shuffle(matrix, seeds[j]);
			Collections.shuffle(tempLabels, new Random(seeds[j]));
			
			double[] target = LibSVM.createTargets(tempLabels);
			
			// set the weights man
			Map<Double, Double> counts = LibSVM.computeClassCounts(target);
			int[] wLabels = new int[counts.size()];
			double[] weights = new double[counts.size()];
			
			for (double label : counts.keySet()) {
				wLabels[(int) label - 1] = (int) label;
				weights[(int) label - 1] = 1 / counts.get(label);
			}
			svmParms.setWeightLabels(wLabels);
			svmParms.setWeights(weights);
			
			
			
			Prediction[] predB = LibSVM.crossValidate(matrix, target, svmParms, svmParms.getNumFolds());
			acc[j] = LibSVM.computeAccuracy(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
			f1[j]  = LibSVM.computeF1(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
		}
		
		accR.setScores(acc);
		f1R.setScores(f1);
		
		double[] comp = {0.0, 0.0};
		comp[0] = toc - tic;
		comp[1] = toc - tic;
		compR.setScores(comp);
	}
	
	
}
