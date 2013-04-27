package org.data2semantics.exp.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.LibSVMPrediction;
import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFFVvsKernelExperiment extends KernelExperiment<RDFWLSubTreeKernel> {
	private LibSVMParameters svmParms;
	private List<Value> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private Result accR1;
	private Result f1R1;
	private Result accR2;
	private Result f1R2;
	private Result compR;


	
	public RDFFVvsKernelExperiment(RDFWLSubTreeKernel kernel, long[] seeds,
			LibSVMParameters svmParms, RDFDataSet dataset,
			List<Resource> instances,  List<Value> labels, List<Statement> blackList) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
		
		accR1  = new Result();
		f1R1   = new Result();
		accR2  = new Result();
		f1R2   = new Result();
		compR = new Result();
		
		results.add(accR1);
		results.add(f1R1);
		results.add(accR2);
		results.add(f1R2);
		results.add(compR);
	}



	public void run() {		
		long tic, toc;
		
		List<Value> tempLabels = new ArrayList<Value>();
		tempLabels.addAll(labels);

		tic = System.currentTimeMillis();
		SparseVector[] fv = kernel.computeFeatureVectors(dataset, instances, blackList);
		toc = System.currentTimeMillis();

		double avg = 0;
		for (SparseVector v : fv) {
			avg += v.size();
		}
		avg /= fv.length;

		System.out.println("Average non-zero elements: " + avg);
		
		double[] accFV = new double[seeds.length];
		double[] f1FV = new double[seeds.length];
		double[] accK = new double[seeds.length];
		double[] f1K = new double[seeds.length];
		
		accR1.setLabel("acc");
		f1R1.setLabel("f1");
		accR2.setLabel("acc");
		f1R1.setLabel("f1");
		compR.setLabel("kernel comp time");
		
		for (int j = 0; j < seeds.length; j++) {
			//matrix = Kernel.shuffle(matrix, seeds[j]);
			List<SparseVector> fvList = Arrays.asList(fv);
			Collections.shuffle(fvList, new Random(seeds[j]));
			fv = fvList.toArray(new SparseVector[1]);
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
			
			
			//svmParms.setLinear();
			//LibSVMPrediction[] predA = LibSVM.crossValidate(fv, target, svmParms, 10);
			//svmParms.setLinear();
			//LibSVMPrediction[] predB = LibSVM.crossValidate(fv, target, svmParms, 10);
	
			double[][] matrix = Kernel.featureVectors2Kernel(fv, false);
			svmParms.setPrecomputedKernel();
			LibSVMPrediction[] predA = LibSVM.crossValidate(matrix, target, svmParms, 10);		
			matrix = Kernel.featureVectors2Kernel(fv, true);
			svmParms.setPrecomputedKernel();
			LibSVMPrediction[] predB = LibSVM.crossValidate(matrix, target, svmParms, 10);
			
			accFV[j] = LibSVM.computeAccuracy(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predA));
			f1FV[j]  = LibSVM.computeF1(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predA));
			accK[j]  = LibSVM.computeAccuracy(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
			f1K[j]   = LibSVM.computeF1(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
	
		}
		
		accR1.setScores(accFV);
		f1R1.setScores(f1FV);
		accR2.setScores(accK);
		f1R2.setScores(f1K);
		
		double[] comp = {0.0, 0.0};
		comp[0] = toc - tic;
		comp[1] = toc - tic;
		compR.setScores(comp);
	}
	
	
}
