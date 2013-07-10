package org.data2semantics.exp.dmold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFCombinedKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.kernels.text.RDFSimpleTextKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.data2semantics.tools.rdf.RDFMultiDataSet;
import org.data2semantics.tools.rdf.RDFSparqlDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

public class DMoLDTask2Experiment extends RDFMLExperiment {
	private static String dataFile = "C:\\Users\\Gerben\\Dropbox\\D2S\\Task2\\LDMC_Task2_train.ttl";
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataFile = args[i];
			}
		}	
		
		createTask2DataSet(1,11);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};

		
		
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		List<Double> targets = EvaluationUtils.createTarget(labels);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Accuracy());
		linParms.setDoCrossValidation(true);
		linParms.setNumFolds(10);
		
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
		svmParms.setNumFolds(10);
		
		

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(2);

		boolean inference = true;



		

		for (int d : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFWLSubTreeKernel k = new RDFWLSubTreeKernel(it, d, inference, true);
				
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(k, seeds, svmParms, dataset, instances, labels, blackList);
				
	
				System.out.println("Running WL RDF: " + d + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);
		


		for (int d : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFWLSubTreeWithTextKernel k = new RDFWLSubTreeWithTextKernel(it, d, inference, false);
				
				
				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
				exp.setDoCV(true);
				exp.setDoTFIDF(true);

				System.out.println("Running WL RDF text: " + d + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

	
		
		
		for (int d : depths) {
			resTable.newRow("");

			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(d, false, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running Edge Vertex Path: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);
		


		for (int d : depths) {
			resTable.newRow("");

				
			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathWithTextKernel(d, false, inference, false), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
			exp.setDoTFIDF(true);

			System.out.println("Running Edge Vertex Path with Text: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);

		
		for (int d : depths) {
			resTable.newRow("");

			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(d, 1, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);
		

		
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);


	}



	private static void createTask2DataSet(double fraction, long seed) {
		RDFFileDataSet d = new RDFFileDataSet(dataFile, RDFFormat.TURTLE);

		dataset = d;

		Random rand = new Random(seed);



		List<Statement> stmts = dataset.getStatementsFromStrings(null, RDF.TYPE.toString(), "http://purl.org/procurement/public-contracts#Contract");
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), "http://example.com/multicontract", null);

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() < fraction) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
			}
		}

		removeSmallClasses(5);
		createBlackList();

		System.out.println(EvaluationUtils.computeClassCounts(EvaluationUtils.createTarget(labels)));
	}
}
