package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class FullClassExperiment extends RDFMLExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		long[] seeds = {11,31,51,71,91};
		double[] cs = { 1, 10, 100, 1000, 10000};	
		// 0.001, 0.01, 0.1,

		//int[] depths = {1, 2, 3};
		//int[] iterations = {0, 2, 4, 6};
		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};

		boolean inference = true;

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);

		createGeoDataSet(10, 0.1, 1, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");
		List<Double> target = EvaluationUtils.createTarget(labels);


		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);




		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("");

				LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
				KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);

		for (int i : depths) {			
			resTable.newRow("");

			LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
			KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(i, false, inference, true), seeds, linParms, dataset, instances, target, blackList, evalFuncs);

			System.out.println("Running EVP: " + i);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}	
		}
		System.out.println(resTable);
		
		for (int i : depths) {			
			resTable.newRow("");

			LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + i);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}	
		}


		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
	}



	protected static void createGeoDataSet(int minSize, double frac, long seed, String property) {
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		Random rand = new Random(seed);

		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

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
