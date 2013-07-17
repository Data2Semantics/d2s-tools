package org.data2semantics.exp.ecml2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.old.utils.datasets.DataSetFactory;
import org.data2semantics.exp.old.utils.datasets.GeneralPredictionDataSetParameters;
import org.data2semantics.exp.old.utils.datasets.PropertyPredictionDataSet;
import org.data2semantics.exp.utils.GraphKernelExperiment;
import org.data2semantics.exp.utils.GraphKernelRunTimeExperiment;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFKernelRunTimeExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.graphkernels.GraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionPartialSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;


/**
 * Class for running the three experiments in the ECML 2013 paper: de Vries, G.K.D. "A Fast Approximation of the Weisfeiler-Lehman Graph Kernel for RDF Data" on the AIFB dataset.
 * The class contains a main method with the optional -file parameter to provide the location of the AIFB dataset (including the file name).
 * 
 * @author Gerben
 *
 */
public class AffiliationCompareExperiment extends RDFMLExperiment {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";

	/**
	 * Main method that runs the three experiments with the affiliation (AIFB) prediction dataset
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Parse input, to allow for specification of the location of the datafile, via "-file"
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataFile = args[i];
			}
		}
		
		// Run the regular affiliation prediction experiment
		affiliationExperiment(false);
		// Run the affiliation prediction experiment with the labels removed
		affiliationExperiment(true);
		// Run the run time experiment for the affiliation prediction dataset
		affiliationRunningTimeExperiment();
	}


	private static void affiliationRunningTimeExperiment() {
		double[] fractions = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};

		double[] cs = {1};	// dummy, we don't care about the prediction scores
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};;

		int depth = 3;
		int iteration = 6;
		boolean inference = true;

		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		ResultsTable resTable = new ResultsTable();

		resTable.newRow("WL RDF");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {

				createAffiliationPredictionDataSet(frac, seed);

				KernelExperiment<RDFGraphKernel> exp = new RDFKernelRunTimeExperiment(new ECML2013RDFWLSubTreeKernel(iteration, depth, inference, true, false), seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running WL RDF: " + frac);
				exp.run();
				res.addResult(exp.getResults().get(0));
			}
			resTable.addResult(res);
		}

		resTable.newRow("IST");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {

				createAffiliationPredictionDataSet(frac, seed);

				KernelExperiment<RDFGraphKernel> exp = new RDFKernelRunTimeExperiment(new RDFIntersectionSubTreeKernel(depth, 1, inference, true, false), seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running IST: " + frac);
				exp.run();
				res.addResult(exp.getResults().get(0));
			}

			resTable.addResult(res);
		}


		long tic, toc;



		resTable.newRow("WL");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {

				createAffiliationPredictionDataSet(frac,seed);
				tic = System.currentTimeMillis();
				PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
				toc = System.currentTimeMillis();

				KernelExperiment<GraphKernel> exp = new GraphKernelRunTimeExperiment(new ECML2013WLSubTreeKernel(iteration), seeds, parms, ds.getGraphs(), labels);

				System.out.println("Running WL: " + frac);
				exp.run();
				res.addResult(exp.getResults().get(0));

				double[] comps = {2 * (toc-tic) + res.getScore()};
				Result resC = new Result(comps,"comp time 2");	
				res.addResult(resC);
			}

			resTable.addResult(res);
		}
		/*
		resTable.newRow("");
		for (double frac : fractions) {
			createAffiliationPredictionDataSet(frac);
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
			toc = System.currentTimeMillis();


			KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new ECML2013IntersectionGraphPathKernel(2,1), seeds, parms, ds.getGraphs(), labels);

			System.out.println("Running IGP: " + frac);
			exp.run();

			double[] comps =  {0,0};
	comps[0] = 2*(toc-tic) + exp.getResults().get(exp.getResults().size()-1).getScore();
			comps[1] = 2*(toc-tic) + exp.getResults().get(exp.getResults().size()-1).getScore();
					Result resC = new Result(comps,"comp time 2");	
			exp.getResults().get(exp.getResults().size()-1).addResult(resC);

			resTable.addResult(exp.getResults().get(exp.getResults().size()-1));
		}*/


		resTable.newRow("IGW");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {
				createAffiliationPredictionDataSet(frac,seed);
				tic = System.currentTimeMillis();
				PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
				toc = System.currentTimeMillis();

				KernelExperiment<GraphKernel> exp = new GraphKernelRunTimeExperiment(new ECML2013IntersectionGraphWalkKernel(2,1), seeds, parms, ds.getGraphs(), labels);

				System.out.println("Running IGW: " + frac);
				exp.run();

				res.addResult(exp.getResults().get(0));

				double[] comps = {2 * (toc-tic) + res.getScore()};
				Result resC = new Result(comps,"comp time 2");	
				res.addResult(resC);
			}

			resTable.addResult(res);
		}


		//resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "affiliation_runningtime.txt");


	}


	private static void affiliationExperiment(boolean blankLabels) {

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 2, 4, 6};

		createAffiliationPredictionDataSet(1);


		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		//parms.setEvalFunction(LibSVMParameters.F1);

		ResultsTable resTable = new ResultsTable();

		

		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("WL RDF, no inference, depth=" + i);
			for (int it : iterations) {
				ECML2013RDFWLSubTreeKernel k = new ECML2013RDFWLSubTreeKernel(it, i, inference, true, blankLabels);
								
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(k, seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		saveResults(resTable, "affiliation.ser");



		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("WL RDF, inference, depth=" + i);
			for (int it : iterations) {
				ECML2013RDFWLSubTreeKernel k = new ECML2013RDFWLSubTreeKernel(it, i, inference, true, blankLabels);
								
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(k, seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		saveResults(resTable, "affiliation.ser");


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("IST, no inference, depth=" + i);
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "affiliation.ser");

		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("IST, inference, depth=" + i);
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "affiliation.ser");


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("IPST, no inference, depth=" + i);
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IPST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "affiliation.ser");

		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("IPST, inference, depth=" + i);
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IPST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "affiliation.ser");




		List<GeneralPredictionDataSetParameters> dataSetsParams = new ArrayList<GeneralPredictionDataSetParameters>();

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, false));

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));


		int[] iterationsIG = {1,2};
		long tic, toc;

		for (GeneralPredictionDataSetParameters params : dataSetsParams) {
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(params);
			toc = System.currentTimeMillis();

			if (blankLabels) {
				ds.removeVertexAndEdgeLabels();
			}

			resTable.newRow("WL");
			for (int it : iterations) {
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new ECML2013WLSubTreeKernel(it), seeds, parms, ds.getGraphs(), labels);

				System.out.println("Running WL: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}

				double[] comps =  {0,0};
				comps[0] = toc-tic;
				comps[1] = toc-tic;
				Result resC = new Result(comps,"comp time 2");
				resTable.addResult(resC);

			}
		}
		saveResults(resTable, "affiliation.ser");



		/*
		dataSetsParams = new ArrayList<GeneralPredictionDataSetParameters>();

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, false));

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, true));
		 */


		for (GeneralPredictionDataSetParameters params : dataSetsParams) {
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(params);
			toc = System.currentTimeMillis();

			if (blankLabels) {
				ds.removeVertexAndEdgeLabels();
			}

			resTable.newRow("IGP");
			for (int it : iterationsIG) {
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new ECML2013IntersectionGraphPathKernel(it,1), seeds, parms, ds.getGraphs(), labels);

				System.out.println("Running IGP: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}

				double[] comps =  {0,0};
				comps[0] = toc-tic;
				comps[1] = toc-tic;
				Result resC = new Result(comps,"comp time 2");
				resTable.addResult(resC);
			}
		}
		saveResults(resTable, "affiliation.ser");


		
		for (GeneralPredictionDataSetParameters params : dataSetsParams) {
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(params);
			toc = System.currentTimeMillis();

			if (blankLabels) {
				ds.removeVertexAndEdgeLabels();
			}

			resTable.newRow("IGW");
			for (int it : iterationsIG) {
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new ECML2013IntersectionGraphWalkKernel(it,1), seeds, parms, ds.getGraphs(), labels);

				System.out.println("Running IGW: " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}

				double[] comps =  {0,0};
				comps[0] = toc-tic;
				comps[1] = toc-tic;
				Result resC = new Result(comps,"comp time 2");
				resTable.addResult(resC);

			}
		}
		
		
		saveResults(resTable, "affiliation.ser");


		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "affiliation" + blankLabels + ".txt");

	}


	private static void createAffiliationPredictionDataSet(double frac) {
		createAffiliationPredictionDataSet(frac, (long)1);
	}


	private static void createAffiliationPredictionDataSet(double frac, long seed) {
		Random rand = new Random(seed);

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

		// Shuffle them, just to be sure
		//Collections.shuffle(instances, new Random(1));
		//Collections.shuffle(labels, new Random(1));

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
