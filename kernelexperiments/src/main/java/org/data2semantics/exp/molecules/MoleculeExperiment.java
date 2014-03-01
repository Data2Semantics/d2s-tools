package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFGraphKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLBiSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFSingleDataSet;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.DegreeComparator;
import org.nodes.Node;
import org.nodes.UGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.MaxObserver;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class MoleculeExperiment {
	public static String MUTAG_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\mutag\\";
	public static String ENZYMES_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\enzymes\\";
	public static final String NCI1_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\nci1\\";
	public static final String NCI109_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\nci109\\";
	public static final String DD_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\DD\\";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<UGraph<String>> graphs = new ArrayList<UGraph<String>>();
		List<Double> labels = new ArrayList<Double>();

		// Regular graph dataset
		createDataSet(MUTAG_DIR, graphs, labels);
		//createDataSet(ENZYMES_DIR, graphs, labels);
		//createDataSet(NCI109_DIR, graphs, labels);
		//createDataSet(DD_DIR, graphs, labels);


		// Separate RDF graphs dataset
		List<List<Statement>> rdfTriples = createMoleculeRDFGraphs(graphs, true);
		List<DTGraph<String,String>> rdfGraphs = new ArrayList<DTGraph<String,String>>();
		for (List<Statement> rdfTrips : rdfTriples) {
			DTGraph<String,String> g = org.nodes.data.RDF.createDirectedGraph(rdfTrips, null, null);
			g = GraphUtils.removeRDFType(g, new ArrayList<String>());
			rdfGraphs.add(g);		
		}
		//*/


		// As one RDF graph dataset
		List<List<Statement>> rdfTriples2 = createMoleculeRDFGraphs(graphs, false);
		RDFDataSet ts = new RDFSingleDataSet();
		for (List<Statement> trips : rdfTriples2) {
			ts.addStatements(trips);
		}

		List<Resource> instances = new ArrayList<Resource>();

		List<Statement> is = ts.getStatementsFromStrings(null, RDF.TYPE.stringValue(), GraphUtils.NAMESPACE + "Molecule");
		for (Statement stmt : is) {
			instances.add(stmt.getSubject());
		}


		DTGraph<String,String> sGraph = org.nodes.data.RDF.createDirectedGraph(ts.getStatements(null, null, null, false), null, null);
		List<DTNode<String,String>> hubs = SlashBurn.getHubs(sGraph, 2, true);
		// (int) Math.round(0.05 * sGraph.nodes().size())
		
	
		Comparator<Node<String>> comp2 = new DegreeComparator<String>();
		MaxObserver<Node<String>> obs2 = new MaxObserver<Node<String>>(hubs.size() + instances.size(), comp2);
		obs2.observe(sGraph.nodes());
		List<DTNode<String,String>> nonSigDegreeHubs = new ArrayList<DTNode<String,String>>();
		for (Node<String> n : obs2.elements()) {
			nonSigDegreeHubs.add((DTNode<String,String>) n);
		}
		
		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is2 = new HashSet<String>();
		for (Resource r : instances) {
			is2.add(r.toString());
		}
		for (DTNode<String,String> n : sGraph.nodes()) {
			if (is2.contains(n.label())) {
				rn.add(n);
			}
		}
		hubs.removeAll(rn);
		nonSigDegreeHubs.removeAll(rn);
		
		System.out.println(hubs.size() + ": " + hubs);
		System.out.println(nonSigDegreeHubs.size() + ": " + nonSigDegreeHubs);


		/*
		List<Statement> all = new ArrayList<Statement>();
		for (List<Statement> s : rdfTriples2) {
			all.addAll(s);
		}		
		saveRDF(all, "enzymes.ttl");
		 */


		//long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		long[] seeds = {11,31,51,71,91};
		//double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	
		double[] cs = {1,10,100,1000};	


		int[] depths = {1,2,3};


		//int[] iterations  = {0,1,2,3,4, 5, 6, 7, 8, 9,10};
		//int[] iterations2 = {0,2,4,6,8,10,12,14,16,18,20};
		int[] iterations  = {0,1,2,3,4,5};
		int[] iterations2 = {0,2,4,6,8,10};


		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setNumFolds(5);
		linParms.setSplitFraction((float) 0.7);
		linParms.setDoCrossValidation(false);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());


		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		//resTable.setManWU(0.05);
		
		
		///*
		for (int it : iterations) {
			resTable.newRow("WL, it: " + it);
			MoleculeGraphExperiment<UGraph<String>> exp = new MoleculeGraphExperiment<UGraph<String>>(new WLUSubTreeKernel(it, true), seeds, svmParms, graphs, labels, evalFuncs);

			System.out.println("Running WL, it: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		//*/

		/*
		for (int it : iterations2) {
			resTable.newRow("WL separate RDF, it: " + it);
			MoleculeLinearGraphExperiment<DTGraph<String,String>> exp = new MoleculeLinearGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, true), seeds, linParms, rdfGraphs, labels, evalFuncs);

			System.out.println("Running WL separate RDF, it: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		//*/

		
		for (int d = 1; d < 4; d++) {
			for (int it : iterations2) {		
				resTable.newRow("WL RDF, " + d + ", " + it);
				
				RDFWLSubTreeKernel k = new RDFWLSubTreeKernel(it, d, false, true, true, false);
				
				RDFGraphKernelExperiment exp = new RDFGraphKernelExperiment(k, seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);

				System.out.println("Running WL RDF, it: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
			System.out.println(resTable);
		}


		for (int d = 1; d < 4; d++) {
			resTable.newRow("WL RDF type, d " + d);
			for (int it : iterations2) {		

				RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, d, false, true, true);
				k.setHubMap(GraphUtils.createRDFTypeHubMap(ts, false));

				RDFGraphKernelExperiment exp = new RDFGraphKernelExperiment(k, seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);

				System.out.println("Running WL RDF, type it: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
			System.out.println(resTable);
		}
		//*/

		int[] hf = {0,1,2,3,4,5,6,7};

		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF WL NonSig " + h);
				for (int it : iterations2) {
					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, false, true, true);
					k.setHubMap(GraphUtils.createNonSigHubMap(nonSigDegreeHubs, h));

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);


					System.out.println("Running WL RDF NonSig: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
			System.out.println(resTable);
		}
		
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF WL SB " + h);
				for (int it : iterations2) {
					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, false, true, true);
					k.setHubMap(GraphUtils.createHubMap(hubs, h));

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);


					System.out.println("Running WL RDF SB: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
			System.out.println(resTable);
		}


		/*
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF IST SB " + h);
					RDFIntersectionSubTreeSlashBurnKernel k = new RDFIntersectionSubTreeSlashBurnKernel(i, 1, false, true);
					k.setHubThreshold(h);

					//KernelExperiment<RDFFeatureVectorKernel> exp = new RDFLinearKernelExperiment(k, seeds, linParms, dataset, instances, target, blackList, evalFuncs);
					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);


					System.out.println("Running RDF IST SB: " + i + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
		}
		System.out.println(resTable);
		 */



		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}

	protected static void createDataSet(String dirName, List<UGraph<String>> graphs, List<Double> labels) {
		try {
			File dir = new File(dirName);
			for (String fileName : dir.list()) {
				graphs.add(GraphUtils.readMoleculeGraph(dirName + fileName));
				labels.add(getGraphLabel(dirName + fileName));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	protected static List<List<Statement>> createMoleculeRDFGraphs(List<UGraph<String>> graphs, boolean blankRoot) {
		List<List<Statement>> rdfGraphs = new ArrayList<List<Statement>>();

		int idx = 1;
		for (UGraph<String> graph : graphs) {
			rdfGraphs.add(GraphUtils.moleculeGraph2RDF(graph, "moleculeInstance_" + idx, blankRoot));
			idx++;
		}

		return rdfGraphs;
	}


	protected static Double getGraphLabel(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();
			while (!line.startsWith("#c")) {
				line = reader.readLine();
			}
			String ret = reader.readLine(); // next line is class
			reader.close();

			return Double.parseDouble(ret);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	protected static void saveRDF(List<Statement> stmts, String filename) {
		try {
			File file = new File(filename);
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileWriter(file));


			writer.startRDF();
			for (Statement stmt : stmts) {
				writer.handleStatement(stmt);
			}
			writer.endRDF();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}


