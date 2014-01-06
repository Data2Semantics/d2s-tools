package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.nodes.UGraph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class MoleculeExperiment {
	public static final String MUTAG_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\mutag\\";
	public static final String ENZYMES_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\enzymes\\";
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

		/*
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

		/*
		for (int it : iterations) {
			resTable.newRow("WL, it: " + it);
			MoleculeLinearGraphExperiment<UGraph<String>> exp = new MoleculeLinearGraphExperiment<UGraph<String>>(new WLUSubTreeKernel(it, true), seeds, linParms, graphs, labels, evalFuncs);

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


		/*
		for (int it : iterations2) {
			resTable.newRow("WL RDF, it: " + it);
			for (int d = 1; d < 4; d++) {
				RDFGraphKernelExperiment exp = new RDFGraphKernelExperiment(new RDFWLSubTreeSlashBurnKernel(it, d, true, true, true), seeds, svmParms, ts, instances, labels, new ArrayList<Statement>(), evalFuncs);

				System.out.println("Running WL RDF, it: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
			System.out.println(resTable);
		}
		//*/
		
		int[] hf = {1,2,3,4,5,6,7,8,9,10};
		
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

		
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}

	private static void createDataSet(String dirName, List<UGraph<String>> graphs, List<Double> labels) {
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


	private static List<List<Statement>> createMoleculeRDFGraphs(List<UGraph<String>> graphs, boolean blankRoot) {
		List<List<Statement>> rdfGraphs = new ArrayList<List<Statement>>();

		int idx = 1;
		for (UGraph<String> graph : graphs) {
			rdfGraphs.add(GraphUtils.moleculeGraph2RDF(graph, "moleculeInstance_" + idx, blankRoot));
			idx++;
		}

		return rdfGraphs;
	}


	private static Double getGraphLabel(String fileName) {
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

	private static void saveRDF(List<Statement> stmts, String filename) {
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


