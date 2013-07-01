/*
 * Kernels don't work on this task, take depth 1 and the labels there, that works best.
 * 
 */
package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.Experimenter;
import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearKernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearVSKernelExperiment;
import org.data2semantics.exp.experiments.RDFOldKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEAR;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.evaluation.Accuracy;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationFunction;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationUtils;
import org.data2semantics.proppred.libsvm.evaluation.F1;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class DMoLDThemeExperiment extends RDFMLExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";

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
