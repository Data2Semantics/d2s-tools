package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFCombinedKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLBiSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class AffiliationExperiment extends RDFMLExperiment {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";
	
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataFile = args[i];
			}
		}		
		
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};

		boolean inference = true;
		
		createAffiliationPredictionDataSet(1);
		
		
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		List<Double> target = EvaluationUtils.createTarget(labels);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Accuracy());
		linParms.setDoCrossValidation(false);
		linParms.setNumFolds(10);
		
		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
		int[] wLabels = new int[counts.size()];
		double[] weights = new double[counts.size()];

		for (double label : counts.keySet()) {
			wLabels[(int) label - 1] = (int) label;
			weights[(int) label - 1] = 1 / counts.get(label);
		}
		linParms.setWeightLabels(wLabels);
		linParms.setWeights(weights);


		createAffiliationPredictionDataSet(1);

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);
		
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("RDF WL forward");

				KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF fwd: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);
		
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("RDF WL reverse");

				KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true, true, false), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF rev: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);
		
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("RDF WL Bi");

				KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFWLBiSubTreeKernel(it, i, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF Bi: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);

	
		
		
		resTable.addCompResults(resTable.getBestResults());
		//resTable.addCompResults(table2.getBestResults());
		System.out.println(resTable);

	}



	private static void createAffiliationPredictionDataSet(double frac) {
		Random rand = new Random(1);

		// Read in data set
		dataset = new RDFFileDataSet(dataFile, RDFFormat.N3);

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
