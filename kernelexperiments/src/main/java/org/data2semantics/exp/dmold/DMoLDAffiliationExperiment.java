package org.data2semantics.exp.dmold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
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
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class DMoLDAffiliationExperiment extends RDFMLExperiment {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";
	
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataFile = args[i];
			}
		}		
		
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};
		boolean inference = false;


		createAffiliationPredictionDataSet(1);

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);
		
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);

		for (int depth : depths) {
			resTable.newRow("WL RDF Bi, depth="+depth);
			for (int it : iterations) {
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFWLBiSubTreeKernel(it, depth, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

				
				System.out.println("Running WL RDF Bi: " + depth + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

		
		
		for (int depth : depths) {
			resTable.newRow("WL RDF forward, depth="+depth);
			for (int it : iterations) {
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFWLSubTreeKernel(it, depth, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

				
				System.out.println("Running WL RDF Fwd: " + depth + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

		for (int depth : depths) {
			resTable.newRow("WL RDF reverse, depth="+depth);
			for (int it : iterations) {
				RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFWLSubTreeKernel(it, depth, inference, true, true, false), seeds, svmParms, dataset, instances, labels, blackList);

				
				System.out.println("Running WL RDF Rev: " + depth + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		System.out.println(resTable);

		
		/*

		for (int depth : depths) {
			resTable.newRow("WL RDF BoW, depth="+depth);
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
		
		*/
		
		/*
		ResultsTable table2 = new ResultsTable();
		
		for (int depth : depths) {
			resTable.newRow("ITP, depth="+depth);
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
			resTable.newRow("ITP with ST, depth="+depth);
			table2.newRow("");
			
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel(depth, false, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running EVP with ST: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
				table2.addResult(res);
			}
		}
		System.out.println(resTable);
		
		
		for (int depth : depths) {
			resTable.newRow("ITP + BoL, depth="+depth);
			table2.newRow("");
			
			List<RDFFeatureVectorKernel> kernels = new ArrayList<RDFFeatureVectorKernel>();
			kernels.add(new RDFIntersectionTreeEdgeVertexPathKernel(depth, false, inference, false));
			kernels.add(new RDFWLSubTreeKernel(0, depth, inference, false));
						
			RDFGraphKernel k = new RDFCombinedKernel(kernels, true);
			
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(k, seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running EVP + BoL: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
				table2.addResult(res);
			}
		}
		System.out.println(resTable);
		
		
		for (int depth : depths) {
			resTable.newRow("ITP BoW, depth="+depth);
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
		
		*/
		
		/*
		
		for (int depth : depths) {
			resTable.newRow("IST, depth="+depth);
			RDFOldKernelExperiment exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(depth, 1, inference, true), seeds, svmParms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + depth);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		
		*/
		
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
