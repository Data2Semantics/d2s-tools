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
import org.data2semantics.proppred.libsvm.LibLINEAR;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.Prediction;
import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFLinearVSKernelExperiment extends KernelExperiment<RDFWLSubTreeKernel> {
	private LibSVMParameters svmParms;
	private LibLINEARParameters linearParms;
	
	private List<Value> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private Result accL;
	private Result f1L;
	private Result accK;
	private Result f1K;
	private Result compR;
	private Result compL;
	private Result compK;


	
	public RDFLinearVSKernelExperiment(RDFWLSubTreeKernel kernel, long[] seeds,
			LibSVMParameters svmParms, LibLINEARParameters linearParms, RDFDataSet dataset,
			List<Resource> instances,  List<Value> labels, List<Statement> blackList) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.linearParms = linearParms;
		this.labels = labels;
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
		
		accL  = new Result();
		f1L   = new Result();
		accK  = new Result();
		f1K   = new Result();
		compR = new Result();
		compL = new Result();
		compK = new Result();
		
		results.add(accL);
		results.add(f1L);
		results.add(accK);
		results.add(f1K);
		results.add(compR);
		results.add(compL);
		results.add(compK);
	}



	public void run() {		
		long tic, toc;		

		tic = System.currentTimeMillis();
		SparseVector[] fv = kernel.computeFeatureVectors(dataset, instances, blackList);
		toc = System.currentTimeMillis();
		
		List<SparseVector> fvList = Arrays.asList(fv);
		kernel = null; // Remove the kernel since it takes up a lot of memory and we only need the FV's
		
		List<Value> tempLabels = new ArrayList<Value>();
		tempLabels.addAll(labels);

		
		double[] comp = {0.0, 0.0};
		comp[0] = toc - tic;
		comp[1] = toc - tic;
		compR.setScores(comp);

		double avg = 0;
		for (SparseVector v : fv) {
			avg += v.size();
		}
		avg /= fv.length;

		System.out.println("Average non-zero elements: " + avg);
		
		double[] accLA = new double[seeds.length];
		double[] f1LA = new double[seeds.length];
		double[] accKA = new double[seeds.length];
		double[] f1KA = new double[seeds.length];
		double[] compLA = new double[seeds.length];
		double[] compKA = new double[seeds.length];
			
		accL.setLabel("acc");
		f1L.setLabel("f1");
		accK.setLabel("acc");
		f1K.setLabel("f1");
		compR.setLabel("kernel comp time");
		compL.setLabel("linear comp time");
		compK.setLabel("svm comp time");
	
		
		
		for (int j = 0; j < seeds.length; j++) {
			Collections.shuffle(fvList, new Random(seeds[j]));
			Collections.shuffle(tempLabels, new Random(seeds[j]));
			
			fv = fvList.toArray(new SparseVector[1]);
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
			
			linearParms.setWeightLabels(wLabels);
			linearParms.setWeights(weights);
			
		
			
			tic = System.currentTimeMillis();
			Prediction[] predA = LibLINEAR.crossValidate(Kernel.convert2BinaryFeatureVectors(fv), target, linearParms, 5);		
			toc = System.currentTimeMillis();
			compLA[j] = toc - tic;			
			
			svmParms.setPrecomputedKernel();
			tic = System.currentTimeMillis();
			Prediction[] predB = LibLINEAR.crossValidate(fv, target, linearParms, 5);
			
		//	double[][] matrix = Kernel.featureVectors2Kernel(fv);
		//	Prediction[] predB = LibSVM.crossValidate(matrix, target, svmParms, 5);
			toc = System.currentTimeMillis();
			compKA[j] = toc - tic;	
			
			accLA[j] = LibSVM.computeAccuracy(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predA));
			f1LA[j]  = LibSVM.computeF1(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predA));

			accKA[j] = LibSVM.computeAccuracy(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
			f1KA[j]  = LibSVM.computeF1(LibSVM.createTargets(tempLabels), LibSVM.extractLabels(predB));
	
		}
		
		accL.setScores(accLA);
		f1L.setScores(f1LA);
		accK.setScores(accKA);
		f1K.setScores(f1KA);
		compL.setScores(compLA);
		compK.setScores(compKA);	
	}	
}
