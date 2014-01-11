package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.learners.evaluation.Error;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFSingleDataSet;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.nodes.DegreeComparator;
import org.nodes.Node;
import org.nodes.UGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.classification.Classification;
import org.nodes.classification.Classified;
import org.nodes.rdf.InformedAvoidance;
import org.nodes.util.MaxObserver;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

public class MoleculePreprocessingExperiment extends MoleculeExperiment {

	public static void main(String[] args) {
		List<UGraph<String>> graphs = new ArrayList<UGraph<String>>();
		List<Double> labels = new ArrayList<Double>();

		// Regular graph dataset
		createDataSet(MUTAG_DIR, graphs, labels);
		//createDataSet(ENZYMES_DIR, graphs, labels);


		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};

		// --------------
		// Learning Algorithm settings
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Error());
		evalFuncs.add(new F1());

		List<Double> target = EvaluationUtils.createTarget(labels);

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		//svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		//svmParms.setWeights(EvaluationUtils.computeWeights(target));
		//---------


		// As one RDF graph dataset
		List<List<Statement>> rdfTriples2 = createMoleculeRDFGraphs(graphs, false);
		RDFDataSet ts = new RDFSingleDataSet();
		for (List<Statement> trips : rdfTriples2) {
			ts.addStatements(trips);
		}

		List<Resource> instances = new ArrayList<Resource>();

		List<Statement> stmts = ts.getStatementsFromStrings(null, RDF.TYPE.stringValue(), GraphUtils.NAMESPACE + "Molecule");
		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
		}

		// Data graph
		DTGraph<String,String> sGraph = org.nodes.data.RDF.createDirectedGraph(ts.getStatements(null, null, null, false), null, null);

		//---------
		// Results Table
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		//---------

		//--- instance nodes
		List<DTNode<String,String>> instanceNodes = new ArrayList<DTNode<String,String>>();
		for (Resource i : instances) {
			instanceNodes.add(sGraph.node(i.toString()));
		}
		//--------


		//--------
		// Get the different hub lists
		int maxHubs = 100;

		// RDF.Type hubs
		List<DTNode<String,String>> RDFTypeHubs = GraphUtils.getTypeHubs(sGraph);

		// Regular Degree
		Comparator<Node<String>> compRegDeg = new DegreeComparator<String>();
		MaxObserver<Node<String>> obsRegDeg = new MaxObserver<Node<String>>(maxHubs + instances.size(), compRegDeg);
		obsRegDeg.observe(sGraph.nodes());
		List<DTNode<String,String>> regDegreeHubs = new ArrayList<DTNode<String,String>>();
		for (Node<String> n : obsRegDeg.elements()) {
			regDegreeHubs.add((DTNode<String,String>) n);
		}

		// Signature Degree
		Comparator<DTNode<String,String>> compSigDeg = new SlashBurn.SignatureComparator<String,String>();
		MaxObserver<DTNode<String,String>> obsSigDeg = new MaxObserver<DTNode<String,String>>(maxHubs + instances.size(), compSigDeg);				
		obsSigDeg.observe(sGraph.nodes());
		List<DTNode<String,String>> sigDegreeHubs = new ArrayList<DTNode<String,String>>(obsSigDeg.elements());

		// Informed Degree
		List<Integer> classes = new ArrayList<Integer>();
		for (double d : target) {
			classes.add((int) d);
		}
		Classified<DTNode<String, String>> classified = Classification.combine(instanceNodes, classes);

		InformedAvoidance ia = new InformedAvoidance(sGraph, classified, 3);		 
		Comparator<DTNode<String, String>> compInformed = ia.informedComparator(3);
		MaxObserver<DTNode<String,String>> obsInformed = new MaxObserver<DTNode<String,String>>(maxHubs + instances.size(), compInformed);
		obsInformed.observe(sGraph.nodes());
		List<DTNode<String,String>> informedDegreeHubs = new ArrayList<DTNode<String,String>>(obsInformed.elements());



		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is = new HashSet<String>();
		for (Resource r : instances) {
			is.add(r.toString());
		}
		for (DTNode<String,String> n : sGraph.nodes()) {
			if (is.contains(n.label())) {
				rn.add(n);
			}
		}
		RDFTypeHubs.removeAll(rn);
		regDegreeHubs.removeAll(rn);
		sigDegreeHubs.removeAll(rn);
		informedDegreeHubs.removeAll(rn);

		List<List<DTNode<String,String>>> hubLists = new ArrayList<List<DTNode<String,String>>>();
		hubLists.add(RDFTypeHubs);
		hubLists.add(regDegreeHubs);
		hubLists.add(sigDegreeHubs);
		//hubLists.add(informedDegreeHubs);
		
		boolean forward = true;
		int it = 6;
		int depth = 3;
		int[] hubThs = {0,8};
	//	int[] hubThs = {100};

		int[] iterations = {1,2,3,4,5,6};
		
		for (int i : iterations) {
			resTable.newRow("WL, it: " + it);
			MoleculeGraphExperiment<UGraph<String>> exp = new MoleculeGraphExperiment<UGraph<String>>(new WLUSubTreeKernel(i, true), seeds, svmParms, graphs, labels, evalFuncs);

			System.out.println("Running WL, it: " + i);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		
		
		MoleculeGraphExperiment<DTGraph<String,String>> exp;
		for (int th : hubThs) {
			resTable.newRow("Hub Threshold: " + th);

			for (List<DTNode<String,String>> hubList : hubLists) {
				
				///*
				List<DTNode<String,String>> newIN = new ArrayList<DTNode<String,String>>(instanceNodes);
				DTGraph<String,String> newG = GraphUtils.simplifyGraph(sGraph, GraphUtils.createHubMap(hubList, th), newIN, false, true);
				
				exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, forward), 
						seeds, svmParms, GraphUtils.getSubGraphs(newG, newIN, depth), target, evalFuncs);

				System.out.println("running, remove hubs, th: " + th);
				exp.run();
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
				
				newIN = new ArrayList<DTNode<String,String>>(instanceNodes);
				newG = GraphUtils.simplifyGraph(sGraph, GraphUtils.createHubMap(hubList, th), newIN, true, false);
				
				exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, forward), 
						seeds, svmParms, GraphUtils.getSubGraphs(newG, newIN, depth), target, evalFuncs);

				System.out.println("running, relabel hubs, th: " + th);
				exp.run();
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}

				newIN = new ArrayList<DTNode<String,String>>(instanceNodes);
				newG = GraphUtils.simplifyGraph(sGraph, GraphUtils.createHubMap(hubList, th), newIN, true, true);
				
				exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, forward), 
						seeds, svmParms, GraphUtils.getSubGraphs(newG, newIN, depth), target, evalFuncs);

				System.out.println("running, relabel+remove hubs, th: " + th);
				exp.run();
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
				//*/

			}
			System.out.println(resTable);
		}
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);		
		System.out.println(resTable.allScoresToString());

	}


}
