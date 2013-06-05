package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEAR;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.Prediction;
import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.proppred.libsvm.evaluation.Accuracy;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationFunction;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationUtils;
import org.data2semantics.proppred.libsvm.evaluation.F1;
import org.data2semantics.proppred.libsvm.evaluation.MeanAbsoluteError;
import org.data2semantics.proppred.libsvm.evaluation.MeanSquaredError;
import org.data2semantics.proppred.libsvm.evaluation.Task1Score;
import org.data2semantics.proppred.libsvm.evaluation.Task1ScoreForBins;
import org.data2semantics.proppred.libsvm.evaluation.Task1ScoreForBothBins;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.data2semantics.tools.rdf.RDFMultiDataSet;
import org.data2semantics.tools.rdf.RDFSparqlDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

public class Task1Experiment extends RDFMLExperiment {

	public static void main(String[] args) {
		long seed = 1;
		createTask1DataSet(1, seed);

		//		double[] bins = {-0.5, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 7.5, 9.5, 14.5, 75.5};
		//double[] bins = {0.5, 1.5, 3.5, 6.5, 22.5};
		double[] bins = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 12.5, 15.5, 18.5, 23.5};
		

		long[] seeds = {11, 21, 31, 41, 51, 61, 71, 81, 91, 101};
		double[] cs = {1, 10, 100, 1000, 10000};	

		int[] depths = {1,2,3,4};
		int[] iterations = {0};

		double[] ps1 = {1};
		double[] ps2 = {0.000001, 0.00001, 0.0001, 0.001, 0.01};
		

		List<Double> target = new ArrayList<Double>();	
		List<Double> targetBins = new ArrayList<Double>();	

		for (Value label : labels) {
			double val = LiteralUtil.getDoubleValue(label,0);
			target.add(val);

			for (int i=0; i < bins.length-1; i++) {
				if (val > bins[i] && val <= bins[i+1]) {
					targetBins.add(i+1.0);
				}
			}
		}


		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);

		boolean inference = true;

		List<EvaluationFunction> evalFuncs1 = new ArrayList<EvaluationFunction>();
		evalFuncs1.add(new Task1ScoreForBins(bins));
		
		List<EvaluationFunction> evalFuncs2 = new ArrayList<EvaluationFunction>();
		evalFuncs2.add(new Task1Score());
		evalFuncs2.add(new MeanSquaredError());
		evalFuncs2.add(new MeanAbsoluteError());

		for (int d : depths) {			
			for (int it : iterations) {
				resTable.newRow("");

				LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
				linParms.setEvalFunction(new Task1ScoreForBothBins(bins));
				linParms.setDoCrossValidation(false);
				linParms.setSplitFraction((float) 0.8);
				linParms.setEps(0.00001);
				linParms.setPs(ps1);
				
				Map<Double, Double> counts = EvaluationUtils.computeClassCounts(targetBins);
				int[] wLabels = new int[counts.size()];
				double[] weights = new double[counts.size()];

				for (double label : counts.keySet()) {
					wLabels[(int) label - 1] = (int) label;
					weights[(int) label - 1] = 1 / counts.get(label);
				}
				linParms.setWeightLabels(wLabels);
				linParms.setWeights(weights);

				
				
				LibLINEARParameters linParms2 = new LibLINEARParameters(LibLINEARParameters.SVR_DUAL, cs);
				linParms2.setEvalFunction(new Task1Score());
				linParms2.setDoCrossValidation(false);
				linParms2.setSplitFraction((float) 0.8);
				linParms2.setEps(0.00001);
				linParms2.setPs(ps2);
				linParms2.setBias(1);

				//RDFFeatureVectorKernel kernel = new RDFWLSubTreeKernel(it, d, inference, true);

				RDFFeatureVectorKernel kernel = new RDFIntersectionTreeEdgeVertexPathKernel(d, inference, true);

				
				//KernelExperiment<RDFWLSubTreeKernel> exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF: " + d + " " + it);

				Map<EvaluationFunction, double[]> resultMap = new HashMap<EvaluationFunction,double[]>();
				Map<EvaluationFunction, double[]> resultMap2 = new HashMap<EvaluationFunction,double[]>();
				
				List<Result> results = new ArrayList<Result>();

				for (EvaluationFunction evalFunc : evalFuncs1) {
					Result res = new Result();
					double[] resA = new double[seeds.length];
					res.setLabel(evalFunc.getLabel());
					res.setScores(resA);
					res.setHigherIsBetter(evalFunc.isHigherIsBetter());
					results.add(res);
					resultMap.put(evalFunc, resA);
				}
				
				for (EvaluationFunction evalFunc : evalFuncs2) {
					Result res = new Result();
					double[] resA = new double[seeds.length];
					res.setLabel(evalFunc.getLabel());
					res.setScores(resA);
					res.setHigherIsBetter(evalFunc.isHigherIsBetter());
					results.add(res);
					resultMap2.put(evalFunc, resA);
				}

				Result compR = new Result();
				results.add(compR);


				long tic, toc;

				List<Double> tempLabels = new ArrayList<Double>();
				List<Double> tempLabelsBins = new ArrayList<Double>();
				tempLabels.addAll(target);
				tempLabelsBins.addAll(targetBins);

				tic = System.currentTimeMillis();
				SparseVector[] fv = kernel.computeFeatureVectors(dataset, instances, blackList);
				toc = System.currentTimeMillis();

				List<SparseVector> fvList = Arrays.asList(fv);


				compR.setLabel("kernel comp time");

				for (int j = 0; j < seeds.length; j++) {
					Collections.shuffle(fvList, new Random(seeds[j]));
					Collections.shuffle(tempLabels, new Random(seeds[j]));
					Collections.shuffle(tempLabelsBins, new Random(seeds[j]));	

					fv = fvList.toArray(new SparseVector[1]);
					double[] targetA = new double[tempLabels.size()];
					double[] targetABins = new double[tempLabelsBins.size()];
					for (int i = 0; i < targetA.length; i++) {
						targetA[i] = tempLabels.get(i);
						targetABins[i] = tempLabelsBins.get(i);
					}


					Prediction[] pred = LibLINEAR.trainTestSplit(fv, targetABins, linParms, linParms.getSplitFraction());			
					Prediction[] pred2 = LibLINEAR.trainTestSplit(fv, targetA, linParms2, linParms2.getSplitFraction());
					
					/*
					double avg = 0;
					for (double val : targetA) {
						avg += val;
					}
					avg /= targetA.length;
					
					for (Prediction p : pred2) {
						p.setLabel(avg);
					}*/
					
					
					double[] targetSplit = LibLINEAR.splitTestTarget(targetA, linParms.getSplitFraction());

					

					for (EvaluationFunction ef : evalFuncs1) {
						resultMap.get(ef)[j] = ef.computeScore(targetSplit, pred);	
					}
					
					for (EvaluationFunction ef : evalFuncs2) {
						resultMap2.get(ef)[j] = ef.computeScore(targetSplit, pred2);	
					}

				} 

				double[] comp = {toc - tic};
				compR.setScores(comp);

				for (Result res : results) {
					resTable.addResult(res);
				}	
			}

		}

		saveResults(resTable, "task1_" + seed + ".ser");

		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "task1_" + seed + ".txt");



	}

	private static void createTask1DataSet(double fraction, long seed) {
		RDFFileDataSet d = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\LDMC_Task1_train.ttl", RDFFormat.TURTLE);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\adms.ttl", RDFFormat.TURTLE);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\ns.ttl", RDFFormat.TURTLE);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\skos.rdf", RDFFormat.RDFXML);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\v1.owl", RDFFormat.RDFXML);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\schemaorg.owl", RDFFormat.RDFXML);
		
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\lookup\\describeList\\all.ttl", RDFFormat.TURTLE);
		
		RDFMultiDataSet test = new RDFMultiDataSet();
		test.addRDFDataSet(d);
		List<String> dbpns = new ArrayList<String>();
		dbpns.add("http://dbpedia.org");
		RDFSparqlDataSet sds = new RDFSparqlDataSet("http://dbpedia.org/sparql", dbpns);
		sds.setLogFile("test.txt");
		//test.addRDFDataSet(sds);
		
		dataset = test;
		
		Random rand = new Random(seed);



		List<Statement> stmts = dataset.getStatementsFromStrings(null, RDF.TYPE.toString(), "http://purl.org/procurement/public-contracts#Contract");
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), "http://purl.org/procurement/public-contracts#numberOfTenders", null);

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() < fraction) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
			}
		}

		removeSmallClasses(5);
		createBlackList();

		double[] target = new double[labels.size()];
		for (int i = 0; i < target.length; i++) {
			target[i] = LiteralUtil.getDoubleValue(labels.get(i),0);
		}
		Map<Double, Double> cc = LibSVM.computeClassCounts(target);
		Double[] keys = cc.keySet().toArray(new Double[1]);
		Arrays.sort(keys);

		for (double key : keys) {
			System.out.println(key + " -> " + cc.get(key));
		}
	}


}
