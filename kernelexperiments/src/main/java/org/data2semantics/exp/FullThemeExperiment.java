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
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreePathKernel;
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

public class FullThemeExperiment extends RDFMLExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
		long seed = 1;
		double[] fractions = {0.01, 0.02, 0.03, 0.04, 0.05};

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dir")) {
				i++;
				dataDir = args[i];
			}
			if (args[i].equals("-seed")) {
				i++;
				seed = Long.parseLong(args[i]);
			}
			if (args[i].equals("-frac")) {
				i++;
				String[] fracs = args[i].split(",");
				fractions = new double[fracs.length];
				for (int j = 0; j < fracs.length; j++) {
					fractions[j] = Double.parseDouble(fracs[j]);
				}
			}
		}

		long[] seeds = {seed};
		double[] cs = {1, 10, 100, 1000, 10000};	

		int[] depths = {1, 2, 3, 4};
		int[] iterations = {0, 2, 4, 6};
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);

		boolean inference = false;
					

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
			List<Double> target = EvaluationUtils.createTarget(labels);

			System.out.println("Running fraction: " + frac);


			for (int i : depths) {			
				for (int it : iterations) {
					resTable.newRow("");

					LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
					linParms.setDoCrossValidation(false);
					linParms.setNumFolds(0);
					linParms.setSplitFraction((float) 0.7);
					
					Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
					int[] wLabels = new int[counts.size()];
					double[] weights = new double[counts.size()];

					for (double label : counts.keySet()) {
						wLabels[(int) label - 1] = (int) label;
						weights[(int) label - 1] = 1 / counts.get(label);
					}
					linParms.setWeightLabels(wLabels);
					linParms.setWeights(weights);

					

					KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

					System.out.println("Running WL RDF: " + i + " " + it);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
		}

		saveResults(resTable, "geo_theme_" + seed + ".ser");

		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "geo_theme_full_" + seed + ".txt");
	}



	private static void createGeoDataSet(int minSize, double frac, long seed, String property) {
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
