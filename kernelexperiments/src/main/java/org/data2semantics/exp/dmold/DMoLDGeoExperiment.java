package org.data2semantics.exp.dmold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
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

public class DMoLDGeoExperiment extends RDFMLExperiment {

	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};
		boolean inference = true;

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		createGeoDataSet(1, 1, 10, "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);
		
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);

		for (int depth : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFWLSubTreeKernel(it, depth, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

				
				System.out.println("Running WL RDF: " + depth + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

		for (int depth : depths) {
			resTable.newRow("");
			for (int it : iterations) {
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFWLSubTreeWithTextKernel(it, depth, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

				
				System.out.println("Running WL RDF with Text: " + depth + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);
		
		
		ResultsTable table2 = new ResultsTable();
		
		for (int depth : depths) {
			resTable.newRow("");
			table2.newRow("");
			
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionTreeEdgeVertexPathKernel(depth, false, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running EVP: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
				table2.addResult(res);
			}
		}
		System.out.println(resTable);
		
		for (int depth : depths) {
			resTable.newRow("");
			table2.newRow("");
			
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionTreeEdgeVertexPathWithTextKernel(depth, false, inference, false), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running EVP with Text: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
				table2.addResult(res);
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
		resTable.addCompResults(table2.getBestResults());
		System.out.println(resTable);

	}



	private static void createGeoDataSet(long seed, double fraction, int minSize, String property) {
		String majorityClass = "http://data.bgs.ac.uk/id/Lexicon/Class/LS";
		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		System.out.println(dataset.getLabel());

		System.out.println("Component Rock statements: " + stmts.size());
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() <= fraction) {
					instances.add(stmt2.getSubject());

					labels.add(stmt2.getObject());
				}
			}
		}

		removeSmallClasses(minSize);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
		System.out.println(labelMap);
	}
}
