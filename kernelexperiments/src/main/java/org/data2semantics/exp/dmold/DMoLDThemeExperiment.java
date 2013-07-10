/*
 * Kernels don't work on this task, take depth 1 and the labels there, that works best.
 * 
 */
package org.data2semantics.exp.dmold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.old.utils.Experimenter;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFLinearVSKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEAR;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class DMoLDThemeExperiment extends RDFMLExperiment {
	private static String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataDir = args[i];
			}
		}		

		double fraction = 0.1;
		long[] seeds = {11, 21, 31, 41, 51, 61, 71, 81, 91, 101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int[] depths = {1, 2, 3};
		int[] iterations = {0, 2, 4, 6};
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(3);

		boolean inference = true;


		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		
		
		for (int i : depths) {	
			resTable.newRow("");	
			for (int it : iterations) {

				List<List<Result>> res = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] seeds2 = {seed};
					
					createGeoDataSet((int)(1000 * fraction), fraction, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
					List<Double> target = EvaluationUtils.createTarget(labels);

					LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
					linParms.setDoCrossValidation(true);
					linParms.setNumFolds(5);

					Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
					int[] wLabels = new int[counts.size()];
					double[] weights = new double[counts.size()];

					for (double label : counts.keySet()) {
						wLabels[(int) label - 1] = (int) label;
						weights[(int) label - 1] = 1 / counts.get(label);
					}
					linParms.setWeightLabels(wLabels);
					linParms.setWeights(weights);

					RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds2, linParms, dataset, instances, target, blackList, evalFuncs);

					System.out.println("Running WL RDF: " + i + " " + it);
					exp.setDoCV(true);
					exp.run();
					res.add(exp.getResults());
				}

				for (Result res2 : Result.mergeResultLists(res)) {
					resTable.addResult(res2);
				}
			}
		}
		System.out.println(resTable);


		for (int i : depths) {	
			resTable.newRow("");	
			for (int it : iterations) {

				List<List<Result>> res = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] seeds2 = {seed};
					
					createGeoDataSet((int)(1000 * fraction), fraction, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
					List<Double> target = EvaluationUtils.createTarget(labels);

					LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
					linParms.setDoCrossValidation(true);
					linParms.setNumFolds(5);

					Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
					int[] wLabels = new int[counts.size()];
					double[] weights = new double[counts.size()];

					for (double label : counts.keySet()) {
						wLabels[(int) label - 1] = (int) label;
						weights[(int) label - 1] = 1 / counts.get(label);
					}
					linParms.setWeightLabels(wLabels);
					linParms.setWeights(weights);

					RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeWithTextKernel(it, i, inference, true), seeds2, linParms, dataset, instances, target, blackList, evalFuncs);

					System.out.println("Running WL RDF with text: " + i + " " + it);
					exp.setDoCV(true);
					exp.run();
					res.add(exp.getResults());
				}

				for (Result res2 : Result.mergeResultLists(res)) {
					resTable.addResult(res2);
				}
			}
		}
		System.out.println(resTable);
		
		
		for (int i : depths) {	
			resTable.newRow("");	

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] seeds2 = {seed};
				createGeoDataSet((int)(1000 * fraction), fraction, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				List<Double> target = EvaluationUtils.createTarget(labels);

				LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
				linParms.setDoCrossValidation(true);
				linParms.setNumFolds(5);

				Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
				int[] wLabels = new int[counts.size()];
				double[] weights = new double[counts.size()];

				for (double label : counts.keySet()) {
					wLabels[(int) label - 1] = (int) label;
					weights[(int) label - 1] = 1 / counts.get(label);
				}
				linParms.setWeightLabels(wLabels);
				linParms.setWeights(weights);

				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(i, false, inference, true), seeds2, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running EVP: " + i);
				exp.setDoCV(true);
				exp.run();
				res.add(exp.getResults());
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		System.out.println(resTable);
		
		for (int i : depths) {	
			resTable.newRow("");	

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] seeds2 = {seed};
				createGeoDataSet((int)(1000 * fraction), fraction, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				List<Double> target = EvaluationUtils.createTarget(labels);

				LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
				linParms.setDoCrossValidation(true);
				linParms.setNumFolds(5);

				Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
				int[] wLabels = new int[counts.size()];
				double[] weights = new double[counts.size()];

				for (double label : counts.keySet()) {
					wLabels[(int) label - 1] = (int) label;
					weights[(int) label - 1] = 1 / counts.get(label);
				}
				linParms.setWeightLabels(wLabels);
				linParms.setWeights(weights);

				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathWithTextKernel(i, false, inference, false), seeds2, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running EVP with text: " + i);
				exp.setDoCV(true);
				exp.setDoTFIDF(true);
				exp.run();
				res.add(exp.getResults());
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		System.out.println(resTable);

		

		for (int i : depths) {	
			resTable.newRow("");	

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] seeds2 = {seed};
				createGeoDataSet((int)(1000 * fraction), fraction, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				List<Double> target = EvaluationUtils.createTarget(labels);

				LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
				svmParms.setNumFolds(5);


				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true), seeds2, svmParms, dataset, instances, labels, blackList);

				System.out.println("Running IST: " + i);
				exp.run();
				res.add(exp.getResults());
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		System.out.println(resTable);	

		saveResults(resTable, "geo_theme_DMoLD.ser");

		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "geo_theme_DMoLD_.txt");
	}



	protected static void createGeoDataSet(int minSize, double frac, long seed, String property) {
		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			for (Statement stmt2 : stmts2) {


				if (rand.nextDouble() < frac) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
			}
		}

		removeSmallClasses(minSize);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
	}

}
