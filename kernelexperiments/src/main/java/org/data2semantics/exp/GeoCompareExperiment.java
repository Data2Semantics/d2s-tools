package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.DataSetFactory;
import org.data2semantics.exp.experiments.GeneralPredictionDataSetParameters;
import org.data2semantics.exp.experiments.GraphKernelExperiment;
import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.PropertyPredictionDataSet;
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.GraphKernel;
import org.data2semantics.proppred.kernels.IntersectionGraphPathKernel;
import org.data2semantics.proppred.kernels.IntersectionGraphWalkKernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionPartialSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.WLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class GeoCompareExperiment extends CompareExperiment {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		lithogenesisExperiments();
	}

	
	private static void lithogenesisExperiments() {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 1, 2, 3, 4, 5, 6};
		
		boolean blankLabels = false;
		
		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		createGeoDataSet(1);

			
		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		//parms.setEvalFunction(LibSVMParameters.F1);

		ResultsTable resTable = new ResultsTable();

		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
				
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
				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
				
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
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
			
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
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionSubTreeKernel(i, 1, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
			
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
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
			
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
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionPartialSubTreeKernel(i, 0.01, inference, true, blankLabels), seeds, parms, dataset, instances, labels, blackList);
			
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
				KernelExperiment<GraphKernel> exp = new GraphKernelExperiment(new WLSubTreeKernel(it), seeds, parms, ds.getGraphs(), labels);
				
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
	
	

	private static void createGeoDataSet(long seed) {
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
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis", null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() >= 0) {
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
		removeSmallClasses(10);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
		System.out.println(labelMap);
	}
	
}