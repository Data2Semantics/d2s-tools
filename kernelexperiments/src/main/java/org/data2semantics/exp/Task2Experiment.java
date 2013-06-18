package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.RDFLinearKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFCombinedKernel;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.RDFSimpleTextKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.evaluation.Accuracy;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationFunction;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationUtils;
import org.data2semantics.proppred.libsvm.evaluation.F1;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.data2semantics.tools.rdf.RDFMultiDataSet;
import org.data2semantics.tools.rdf.RDFSparqlDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

public class Task2Experiment extends RDFMLExperiment {

	public static void main(String[] args) {
		createTask2DataSet(1,11);


		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());


		List<Double> targets = EvaluationUtils.createTarget(labels);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Accuracy());
		linParms.setDoCrossValidation(true);
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


		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(3);



		boolean inference = true;

		for (int d : depths) {
			resTable.newRow("");

			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgePathKernel(d, false, inference, false), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

			//RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
			exp.setDoTFIDF(true);

			System.out.println("Running Edge Path: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);


		for (int d : depths) {
			resTable.newRow("");

			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(d, false, inference, false), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

			//RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
			exp.setDoTFIDF(true);

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

			//RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
//			exp.setDoBinary(true);
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
			List<RDFFeatureVectorKernel> kernels = new ArrayList<RDFFeatureVectorKernel>();
			kernels.add(new RDFIntersectionTreeEdgeVertexPathKernel(d, false, inference, false));
			kernels.add(new RDFSimpleTextKernel(d, inference, false));

			RDFFeatureVectorKernel kernel = new RDFCombinedKernel(kernels, true);


			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(kernel, seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

			//RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
			exp.setDoTFIDF(true);

			System.out.println("Running Edge Vertex Path with Simple Text: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);

		for (int d : depths) {
			resTable.newRow("");

			RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFSimpleTextKernel(d, inference, false), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);

			//RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, true), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
			exp.setDoCV(true);
			exp.setDoTFIDF(true);

			System.out.println("Running Simple Text Kernel: " + d);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}

		}
		System.out.println(resTable);


		for (int d : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, d, inference, false), seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
				exp.setDoCV(true);
				exp.setDoTFIDF(true);

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
				List<RDFFeatureVectorKernel> kernels = new ArrayList<RDFFeatureVectorKernel>();
				kernels.add(new RDFWLSubTreeKernel(it,d, inference, false));
				kernels.add(new RDFSimpleTextKernel(d, inference, false));

				RDFFeatureVectorKernel kernel = new RDFCombinedKernel(kernels, true);

				RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(kernel, seeds, linParms, dataset, instances, targets, blackList, evalFuncs);
				exp.setDoCV(true);
				exp.setDoTFIDF(true);

				System.out.println("Running Text + WL RDF: " + d + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}

		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);


	}



	private static void createTask2DataSet(double fraction, long seed) {
		RDFFileDataSet d = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\Task2\\LDMC_Task2_train.ttl", RDFFormat.TURTLE);

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
