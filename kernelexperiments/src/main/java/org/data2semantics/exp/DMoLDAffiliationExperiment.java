package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.RDFLinearKernelExperiment;
import org.data2semantics.exp.experiments.RDFOldKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathKernel;
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

public class DMoLDAffiliationExperiment extends RDFMLExperiment {

	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};
		boolean inference = true;


		createAffiliationPredictionDataSet(1);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());
		List<Double> targets = EvaluationUtils.createTarget(labels);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Accuracy());
		linParms.setDoCrossValidation(true);
		linParms.setSplitFraction((float) 0.8);
		linParms.setEps(0.1);
		linParms.setNumFolds(5);

		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(targets);
		int[] wLabels = new int[counts.size()];
		double[] weights = new double[counts.size()];

		for (double label : counts.keySet()) {
			wLabels[(int) label - 1] = (int) label;
			weights[(int) label - 1] = 1 / counts.get(label);
		}
		linParms.setWeightLabels(wLabels);
		linParms.setWeights(weights);

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(5);
		
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);

		for (int depth : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, depth, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

				System.out.println("Running WL RDF: " + depth + " " + it);
				exp.setDoCV(true);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

		for (int depth : depths) {
			resTable.newRow("");
			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(depth, false, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

			System.out.println("Running EVP: " + depth);
			exp.setDoCV(true);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		
		for (int depth : depths) {
			resTable.newRow("");
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(depth, 1, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}



	private static void createAffiliationPredictionDataSet(double frac) {
		Random rand = new Random(1);

		// Read in data set
		dataset = new RDFFileDataSet("datasets/aifb-fixed_complete.n3", RDFFormat.N3);

		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		// initialize the lists of instances and labels
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			if (rand.nextDouble() <= frac) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}

		//capClassSize(20, 1);
		removeSmallClasses(5);
		// the blackLists data structure
		blackList = new ArrayList<Statement>();
		blackLists = new HashMap<Resource, List<Statement>>();

		// For each instance we add the triples that give the label of the instance (i.e. the URI of the affiliation)
		// In this case this is the affiliation triple and the reverse relation triple, which is the employs relation.
		for (Resource instance : instances) {
			blackList.addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			blackList.addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
		}

		for (Resource instance : instances) {
			blackLists.put(instance, blackList);
		}
	}
}
