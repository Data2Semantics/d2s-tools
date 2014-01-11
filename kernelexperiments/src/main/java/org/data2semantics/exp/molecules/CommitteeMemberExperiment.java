package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFGraphKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.MaxObserver;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class CommitteeMemberExperiment extends RDFMLExperiment {

	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	
		
		//int[] depths = {1, 2, 3};
		//int[] iterations = {0, 2, 4, 6};
		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};
		boolean inference = false;	
		boolean forward = true;
		
		createCommitteeMemberPredictionDataSet();

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(3);
		
		
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

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));

	
		
		DTGraph<String,String> sGraph = org.nodes.data.RDF.createDirectedGraph(dataset.getStatements(null, null, null, inference), null, null);
		List<DTNode<String,String>> hubs = SlashBurn.getHubs(sGraph, 1, true);
		
		Comparator<DTNode<String,String>> comp = new SlashBurn.SignatureComparator<String,String>();
		MaxObserver<DTNode<String,String>> obs = new MaxObserver<DTNode<String,String>>(hubs.size(), comp);		
		obs.observe(sGraph.nodes());
		
		List<DTNode<String,String>> degreeHubs = new ArrayList<DTNode<String,String>>(obs.elements());
		
		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is = new HashSet<String>();
		for (Resource r : instances) {
			is.add(r.toString());
		}
		for (DTNode<String,String> hub : hubs) {
			if (is.contains(hub.label())) {
				rn.add(hub);
			}
		}
		hubs.removeAll(rn);				
		degreeHubs.removeAll(rn);
		
		System.out.println("Total SB hubs: " + hubs.size());
		System.out.println(hubs);	
		System.out.println(degreeHubs);
		
		for (int i = 0; i < degreeHubs.size() && i < hubs.size(); i++) {
			if (!hubs.get(i).equals(degreeHubs.get(i))) {
				System.out.println(i + " " + hubs.get(i).label() + " " + degreeHubs.get(i).label());
			}
		}
		
		
		/*
		Map<String,Integer> dMap  = GraphUtils.createDegreeHubMap(degreeHubs, 300);
		Map<String,Integer> sbMap = GraphUtils.createHubMap(hubs, 300);
		
		for (String k : dMap.keySet()) {
			int l = dMap.get(k);
			if (sbMap.get(k) != l) {
				System.out.println("fail in level: " + l + " " + sbMap.get(k));
			}
			
		}
		*/
		
		
		//int[] hf = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		
		int[] hf = {1,2,3,4,5,10,20,30,40,50,60,70,80,90,100};

		
		
		
		///*
		for (int i : depths) {			
			resTable.newRow("RDF WL forward");
			for (int it : iterations) {
				RDFWLSubTreeKernel k = new RDFWLSubTreeKernel(it, i, inference, true, forward, false);
				
				//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
				KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);


				System.out.println("Running WL RDF fwd: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		//*/

		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF WL forward Degree " + h);
				for (int it : iterations) {
					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, inference, true, forward);
					k.setHubMap(GraphUtils.createHubMap(degreeHubs, h));

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);


					System.out.println("Running WL RDF fwd Degree: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
		}
		System.out.println(resTable);
		

		///*
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF WL forward SB " + h);
				for (int it : iterations) {
					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, inference, true, forward);
					k.setHubMap(GraphUtils.createHubMap(hubs, h));

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);


					System.out.println("Running WL RDF fwd SB: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
		}
		System.out.println(resTable);
		//*/


		/*
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF IST SB " + h);
					RDFIntersectionSubTreeSlashBurnKernel k = new RDFIntersectionSubTreeSlashBurnKernel(i, 1, inference, true);
					k.setHubThreshold(h);

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);


					System.out.println("Running RDF IST SB: " + i + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
		}
		System.out.println(resTable);
		//*/

		
		
		/*
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
	//*/



		resTable.addCompResults(resTable.getBestResults());
		//resTable.addCompResults(table2.getBestResults());
		System.out.println(resTable);
		
	}
	
	private static void createCommitteeMemberPredictionDataSet() {
		RDFFileDataSet testSetA = new RDFFileDataSet("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2011-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2012-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2008-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2009-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2012-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/iswc-2010-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2009-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2008-complete.rdf", RDFFormat.RDFXML);

		RDFFileDataSet testSetB = new RDFFileDataSet("datasets/iswc-2012-complete.rdf", RDFFormat.RDFXML);

		instances = new ArrayList<Resource>();
		List<Resource> instancesB = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		List<Statement> stmts = testSetA.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person");
		for (Statement stmt : stmts) {
			instancesB.add(stmt.getSubject()); 
		}	

		int pos = 0, neg = 0;
		for (Resource instance : instancesB) {
			if (!testSetB.getStatements(instance, null, null).isEmpty()) {
				instances.add(instance);
				if (testSetB.getStatementsFromStrings(instance.toString(), "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/conference/iswc/2012/pc-member/research", false).size() > 0) {
					labels.add(testSetA.createLiteral("true"));
					pos++;
				} else {
					labels.add(testSetA.createLiteral("false"));
					neg++;
				}
			}
		}

		dataset = testSetA;		
		blackList = new ArrayList<Statement>();

		System.out.println("Pos and Neg: " + pos + " " + neg);
		System.out.println("Baseline acc: " + Math.max(pos, neg) / ((double)pos+neg));

	}
	
}
