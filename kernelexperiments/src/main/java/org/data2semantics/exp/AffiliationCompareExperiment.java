package org.data2semantics.exp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class AffiliationCompareExperiment {
	private static RDFDataSet dataset;
	private static List<Resource> instances;
	private static List<Value> labels;
	private static List<Statement> blackList;
	private static Map<Resource, List<Statement>> blackLists;
	

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 1, 2, 3, 4, 5, 6};
		
		boolean blankLabels = false;
		
		createAffiliationPredictionDataSet();

			
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
		saveResults(resTable);
		
	

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
		saveResults(resTable);
		

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
		saveResults(resTable);
		
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
		saveResults(resTable);
		

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
		saveResults(resTable);
		
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
		saveResults(resTable);
		
		
		
		List<GeneralPredictionDataSetParameters> dataSetsParams = new ArrayList<GeneralPredictionDataSetParameters>();

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, false));
		
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
		
	
		int[] iterationsIG = {1,2,3};
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
		saveResults(resTable);
		

		dataSetsParams = new ArrayList<GeneralPredictionDataSetParameters>();

		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, false));
		
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 1, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 2, false, true));

		
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
		saveResults(resTable);
		
		
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
		saveResults(resTable);
		
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}

	
	
	
	private static void createAffiliationPredictionDataSet() {
		// Read in data set
		dataset = new RDFFileDataSet("datasets/aifb-fixed_complete.n3", RDFFormat.N3);

		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		// initialize the lists of instances and labels
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
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
	
	private static void removeSmallClasses(int smallClassSize) {
		Map<Value, Integer> counts = new HashMap<Value, Integer>();

		for (int i = 0; i < labels.size(); i++) {
			if (!counts.containsKey(labels.get(i))) {
				counts.put(labels.get(i), 1);
			} else {
				counts.put(labels.get(i), counts.get(labels.get(i)) + 1);
			}
		}

		List<Value> newLabels = new ArrayList<Value>();
		List<Resource> newInstances = new ArrayList<Resource>();

		for (int i = 0; i < labels.size(); i++) {
			if (counts.get(labels.get(i)) >= smallClassSize) { 
				newInstances.add(instances.get(i));
				newLabels.add(labels.get(i));
			}
		}

		instances = newInstances;
		labels = newLabels;
	}

	private static void capClassSize(int classSizeCap, long seed) {
		Map<Value, Integer> counts = new HashMap<Value, Integer>();
		List<Value> newLabels = new ArrayList<Value>();
		List<Resource> newInstances = new ArrayList<Resource>();

		Collections.shuffle(instances, new Random(seed));
		Collections.shuffle(labels, new Random(seed));

		for (int i = 0; i < instances.size(); i++) {
			if (counts.containsKey(labels.get(i))) {
				if (counts.get(labels.get(i)) < classSizeCap) {
					newInstances.add(instances.get(i));
					newLabels.add(labels.get(i));
					counts.put(labels.get(i), counts.get(labels.get(i)) + 1);
				}

			} else {
				newInstances.add(instances.get(i));
				newLabels.add(labels.get(i));
				counts.put(labels.get(i), 1);
			}
		}

		instances = newInstances;
		labels = newLabels;	
	}
	
	private static void saveResults(ResultsTable resTable) {
		try
		{
			FileOutputStream fileOut =
					new FileOutputStream("resTable.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(resTable);
			out.close();
			fileOut.close();
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}
}
