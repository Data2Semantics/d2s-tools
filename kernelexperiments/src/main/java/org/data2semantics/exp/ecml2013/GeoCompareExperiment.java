package org.data2semantics.exp.ecml2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.old.utils.Experimenter;
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
import org.data2semantics.proppred.kernels.graphkernels.IntersectionGraphPathKernel;
import org.data2semantics.proppred.kernels.graphkernels.IntersectionGraphWalkKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionPartialSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class GeoCompareExperiment extends RDFMLExperiment {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		lithogenesisExperiments();
		lithogenesisRunningTimeExperiments();
		themeExperiments(0.1, 50);
	} 

	private static void lithogenesisRunningTimeExperiments() {
		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);


		double[] fractions = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};

		double[] cs = {1};	// dummy, we don't care about the prediction scores
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		//long[] seeds = {11};

		int depth = 3;
		int iteration = 6;
		boolean inference = true;

		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		ResultsTable resTable = new ResultsTable();

		resTable.newRow("");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {

				createGeoDataSet(seed,frac,1,"http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");

				KernelExperiment<RDFGraphKernel> exp = new RDFKernelRunTimeExperiment(new ECML2013RDFWLSubTreeKernel(iteration, depth, inference, true, false), seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running WL RDF: " + frac);
				exp.run();
				res.addResult(exp.getResults().get(0));
			}
			resTable.addResult(res);
		}

		resTable.newRow("");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {
				createGeoDataSet(seed,frac,1,"http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");

				KernelExperiment<RDFGraphKernel> exp = new RDFKernelRunTimeExperiment(new RDFIntersectionSubTreeKernel(depth, 1, inference, true, false), seeds, parms, dataset, instances, labels, blackList);

				System.out.println("Running IST: " + frac);
				exp.run();
				res.addResult(exp.getResults().get(0));
			}
			resTable.addResult(res);
		}


		long tic, toc;



		resTable.newRow("");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {
				createGeoDataSet(seed,frac,1,"http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");
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
			createGeoDataSet(11,frac,"http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
			toc = System.currentTimeMillis();


			KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new IntersectionGraphPathKernel(2,1), seeds, parms, ds.getGraphs(), labels);

			System.out.println("Running IGP: " + frac);
			exp.run();

			double[] comps =  {0,0};
			comps[0] = 2*(toc-tic) + exp.getResults().get(exp.getResults().size()-1).getScore();
			comps[1] = 2*(toc-tic) + exp.getResults().get(exp.getResults().size()-1).getScore();
			Result resC = new Result(comps,"comp time 2");	
			exp.getResults().get(exp.getResults().size()-1).addResult(resC);


			resTable.addResult(exp.getResults().get(exp.getResults().size()-1));	
		}
		 */

		resTable.newRow("");
		for (double frac : fractions) {

			Result res = new Result();
			res.setLabel("runtime");
			for (long seed : seeds) {
				createGeoDataSet(seed,frac,1,"http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");
				tic = System.currentTimeMillis();
				PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
				toc = System.currentTimeMillis();


				KernelExperiment<GraphKernel> exp = new GraphKernelRunTimeExperiment(new IntersectionGraphWalkKernel(2,1), seeds, parms, ds.getGraphs(), labels);

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
		saveResults(resTable.toString(), "lithogenesis_runningtime.txt");


	}


	private static void themeExperiments(double fraction, int minSize) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 2, 4, 6};

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);

		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);

		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				Experimenter experimenter = new Experimenter(2);
				Thread expT = new Thread(experimenter);
				expT.setDaemon(true);
				expT.start();				

				List<List<Result>> res = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] s2 = new long[1];
					s2[0] = seed;
					createGeoDataSet(seed, fraction, minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
					KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new ECML2013RDFWLSubTreeKernel(it, i, inference, true, false), s2, parms, dataset, instances, labels, blackList);
					res.add(exp.getResults());

					System.out.println("Running WL RDF: " + i + " " + it);
					if (experimenter.hasSpace()) {
						experimenter.addExperiment(exp);
					}


				}

				experimenter.stop();
				try {
					while (expT.isAlive()) {
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (Result res2 : Result.mergeResultLists(res)) {
					resTable.addResult(res2);
				}
			}
		}
		saveResults(resTable, "geo_theme.ser");


		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				Experimenter experimenter = new Experimenter(2);
				Thread expT = new Thread(experimenter);
				expT.setDaemon(true);
				expT.start();


				List<List<Result>> res = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] s2 = new long[1];
					s2[0] = seed;
					createGeoDataSet(seed, fraction, minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
					KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new ECML2013RDFWLSubTreeKernel(it, i, inference, true, false), s2, parms, dataset, instances, labels, blackList);
					res.add(exp.getResults());

					System.out.println("Running WL RDF: " + i + " " + it);
					if (experimenter.hasSpace()) {
						experimenter.addExperiment(exp);
					}


				}

				experimenter.stop();

				while (expT.isAlive()) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				for (Result res2 : Result.mergeResultLists(res)) {
					resTable.addResult(res2);
				}
			}
		}
		saveResults(resTable, "geo_theme.ser");


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");

			Experimenter experimenter = new Experimenter(2);
			Thread expT = new Thread(experimenter);
			expT.setDaemon(true);
			expT.start();

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] s2 = new long[1];
				s2[0] = seed;
				createGeoDataSet(seed, fraction,  minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, false), s2, parms, dataset, instances, labels, blackList);
				res.add(exp.getResults());

				System.out.println("Running IST: " + i);
				if (experimenter.hasSpace()) {
					experimenter.addExperiment(exp);
				}
			}

			experimenter.stop();

			while (expT.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		saveResults(resTable, "geo_theme.ser");


		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");

			Experimenter experimenter = new Experimenter(2);
			Thread expT = new Thread(experimenter);
			expT.setDaemon(true);
			expT.start();

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] s2 = new long[1];
				s2[0] = seed;
				createGeoDataSet(seed, fraction,  minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, false), s2, parms, dataset, instances, labels, blackList);
				res.add(exp.getResults());


				System.out.println("Running IST: " + i);
				if (experimenter.hasSpace()) {
					experimenter.addExperiment(exp);
				}

			}

			experimenter.stop();

			while (expT.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		saveResults(resTable, "geo_theme.ser");


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");

			Experimenter experimenter = new Experimenter(2);
			Thread expT = new Thread(experimenter);
			expT.setDaemon(true);
			expT.start();

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] s2 = new long[1];
				s2[0] = seed;
				createGeoDataSet(seed, fraction,  minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, false), s2, parms, dataset, instances, labels, blackList);
				res.add(exp.getResults());

				System.out.println("Running IPST: " + i);
				if (experimenter.hasSpace()) {
					experimenter.addExperiment(exp);
				}
			}

			experimenter.stop();

			while (expT.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		saveResults(resTable, "geo_theme.ser");



		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");

			Experimenter experimenter = new Experimenter(2);
			Thread expT = new Thread(experimenter);
			expT.setDaemon(true);
			expT.start();

			List<List<Result>> res = new ArrayList<List<Result>>();
			for (long seed : seeds) {
				long[] s2 = new long[1];
				s2[0] = seed;
				createGeoDataSet(seed, fraction,  minSize, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, false), s2, parms, dataset, instances, labels, blackList);
				res.add(exp.getResults());

				System.out.println("Running IPST: " + i);
				if (experimenter.hasSpace()) {
					experimenter.addExperiment(exp);
				}
			}

			experimenter.stop();

			while (expT.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Result res2 : Result.mergeResultLists(res)) {
				resTable.addResult(res2);
			}
		}
		saveResults(resTable, "geo_theme.ser");


		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "geo_theme" + fraction + ".txt");
	}


	private static void lithogenesisExperiments() {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 2, 4, 6};

		boolean blankLabels = false;

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		createGeoDataSet(1, 1, "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");


		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		//parms.setEvalFunction(LibSVM.F1);

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);

		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
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
		saveResults(resTable, "geo_litho.ser");



		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
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
		saveResults(resTable, "geo_litho.ser");




		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "geo_litho.ser");

		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "geo_litho.ser");


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IPST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "geo_litho.ser");

		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);

			System.out.println("Running IPST: " + i + " ");
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		saveResults(resTable, "geo_litho.ser");



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

			resTable.newRow("");
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
		saveResults(resTable, "geo_litho.ser");


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

			resTable.newRow("");
			for (int it : iterationsIG) {
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new IntersectionGraphPathKernel(it,1), seeds, parms, ds.getGraphs(), labels);

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
		saveResults(resTable, "geo_litho.ser");


		for (GeneralPredictionDataSetParameters params : dataSetsParams) {
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(params);
			toc = System.currentTimeMillis();

			if (blankLabels) {
				ds.removeVertexAndEdgeLabels();
			}

			resTable.newRow("");
			for (int it : iterationsIG) {
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new IntersectionGraphWalkKernel(it,1), seeds, parms, ds.getGraphs(), labels);

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
		saveResults(resTable, "geo_litho.ser");




		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "geo_litho.txt");

	}

	private static void createGeoDataSet(long seed, double fraction, String property) {
		createGeoDataSet(seed, fraction, 10, property);
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
					/*
				if (stmt2.getObject().toString().equals(majorityClass)) {
					labels.add(ds.createLiteral("pos"));
				} else {
					labels.add(ds.createLiteral("neg"));
				}
					 */
				}
			}
		}


		//capClassSize(50, seed);
		removeSmallClasses(minSize);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
		System.out.println(labelMap);
	}

}
