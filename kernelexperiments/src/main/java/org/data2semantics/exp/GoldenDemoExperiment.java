package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
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

public class GoldenDemoExperiment extends RDFMLExperiment {
	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {1, 10, 100, 1000};	

		int[] depths = {1,2,3};
		int[] iterations = {0};

		double fraction = 0.1;
		
		dataset = new RDFFileDataSet("datasets\\Stadsverkeer.ttl", RDFFormat.TURTLE);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());
		
		


		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(3);

		boolean inference = true;
		for (int d : depths) {
			resTable.newRow("");
			for (int it : iterations) {

				List<List<Result>> res = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] s2 = {seed};

					loadDataSet(fraction, seed);
					
					List<Double> targets = EvaluationUtils.createTarget(labels);

					LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
					linParms.setEvalFunction(new Accuracy());
					linParms.setDoCrossValidation(false);
					linParms.setSplitFraction((float) 0.8);
					linParms.setEps(0.1);

					Map<Double, Double> counts = EvaluationUtils.computeClassCounts(targets);
					int[] wLabels = new int[counts.size()];
					double[] weights = new double[counts.size()];

					for (double label : counts.keySet()) {
						wLabels[(int) label - 1] = (int) label;
						weights[(int) label - 1] = 1 / counts.get(label);
					}
					linParms.setWeightLabels(wLabels);
					linParms.setWeights(weights);

					
					RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), s2, linParms, dataset, instances, targets, blackList, evalFuncs);
					res.add(exp.getResults());

					System.out.println("Running WL RDF: " + d + " " + it);
					exp.run();
				}
				for (Result res2 : Result.mergeResultLists(res)) {
					resTable.addResult(res2);
				}
			}
		}
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);










	}

	public static void loadDataSet(double fraction, long seed) {

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.data2semantics.org/core/color", null, true);

		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		Random rand = new Random(seed);

		for (Statement stmt : stmts) {
			if (rand.nextDouble() < fraction) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}

		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(null, null, labels.get(i), true));
			if (labels.get(i) instanceof Resource) {
				newBL.addAll(dataset.getStatements((Resource) labels.get(i), null, null, true));
			}
		}

		blackList = newBL;
		blackLists = new HashMap<Resource, List<Statement>>();
		
		for (Resource instance : instances) {
			blackLists.put(instance, blackList);
		}	

		
		removeSmallClasses(10);

		System.out.println("# Cells with a color: " + instances.size());
		System.out.println(EvaluationUtils.computeClassCounts(EvaluationUtils.createTarget(labels)));
	}
}
