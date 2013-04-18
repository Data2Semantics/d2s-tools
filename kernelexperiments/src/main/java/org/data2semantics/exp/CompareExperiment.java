package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.data2semantics.exp.experiments.DataSetFactory;
import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.PropertyPredictionDataSet;
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionPartialSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.WLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.LibSVMPrediction;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

import cern.colt.Arrays;

public class CompareExperiment {
	private static RDFDataSet dataset;
	private static List<Resource> instances;
	private static List<Value> labels;
	private static List<Statement> blackList;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		//affiliationExperiments();
		geoExperiments();

		//createGeoDataSet(2);
		//createCommitteeMemberPredictionDataSet();
		//createAffiliationPredictionDataSet();


		
	}

	private static void affiliationExperiments() {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 1, 2, 3, 4, 5, 6};
		
		createAffiliationPredictionDataSet();

			
		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		//parms.setEvalFunction(LibSVMParameters.F1);

		ResultsTable resTable = new ResultsTable();

		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, parms, dataset, instances, labels, blackList);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}

		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, parms, dataset, instances, labels, blackList);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}


		inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionSubTreeKernel(i, 0.01, inference, true), seeds, parms, dataset, instances, labels, blackList);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		
		inference = true;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionSubTreeKernel(i, 0.01, inference, true), seeds, parms, dataset, instances, labels, blackList);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}

		
		System.out.println(resTable);
	}



	private static void geoExperiments() {
		RDFFileDataSet ds = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		//createGeoDataSet(1, ds);

		long[] seeds = {11,21,31,41,51};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 4;
		int[] iterations = {0, 2, 4, 6, 8};

		boolean inference = false;	
		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		//parms.setEvalFunction(LibSVMParameters.F1);

		ResultsTable resTable = new ResultsTable();


		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");

			for (int it : iterations) {

				List<List<Result>> allRes = new ArrayList<List<Result>>();
				for (long seed : seeds) {
					long[] seedsTemp = new long[1];
					seedsTemp[0] = seed;

					createGeoDataSet(seed, ds);
					KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seedsTemp, parms, dataset, instances, labels, blackList);
					exp.run();
					allRes.add(exp.getResults());
				}

				for (Result res : Result.mergeResultLists(allRes)) {
					resTable.addResult(res);
				}

			}
		}
		//*/


		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");


			List<List<Result>> allRes = new ArrayList<List<Result>>();

			for (long seed : seeds) {
				long[] seedsTemp = new long[1];
				seedsTemp[0] = seed;

				createGeoDataSet(seed, ds);

				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFIntersectionSubTreeKernel(i, 0.01, inference, true), seedsTemp, parms, dataset, instances, labels, blackList);

				exp.run();
				allRes.add(exp.getResults());
			}

			for (Result res : Result.mergeResultLists(allRes)) {
				resTable.addResult(res);
			}

		}



		//*/

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

		//capClassSize(50);
		removeSmallClasses(5);

		// Shuffle them, just to be sure
		//Collections.shuffle(instances, new Random(1));
		//Collections.shuffle(labels, new Random(1));

		// the blackLists data structure
		blackList = new ArrayList<Statement>();
		Map<Resource, List<Statement>> blackLists = new HashMap<Resource, List<Statement>>();

		// For each instance we add the triples that give the label of the instance (i.e. the URI of the affiliation)
		// In this case this is the affiliation triple and the reverse relation triple, which is the employs relation.
		for (Resource instance : instances) {
			blackLists.put(instance, new ArrayList<Statement>());
			blackLists.get(instance).addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			blackLists.get(instance).addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
			blackList.addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			blackList.addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
		}
	}


	private static void createCommitteeMemberPredictionDataSet() {
		RDFFileDataSet testSetA = new RDFFileDataSet("datasets/eswc-2011-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/eswc-2010-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/iswc-2010-complete.rdf", RDFFormat.RDFXML);

		RDFFileDataSet testSetB = new RDFFileDataSet("datasets/eswc-2012-complete.rdf", RDFFormat.RDFXML);

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
				if (testSetB.getStatementsFromStrings(instance.toString(), "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", false).size() > 0) {
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

	private static void createGeoDataSet(long seed, RDFDataSet ds) {
		String majorityClass = "http://data.bgs.ac.uk/id/Lexicon/Class/LS";
		Random rand = new Random(seed);

		List<Statement> stmts = ds.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		System.out.println(ds.getLabel());

		System.out.println("Component Rock statements: " + stmts.size());
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		dataset = ds;
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = ds.getStatementsFromStrings(stmt.getSubject().toString(), "http://data.bgs.ac.uk/ref/Lexicon/hasTheme", null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() >= 0.9) {
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
		removeSmallClasses(5);
		blackList = createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
		System.out.println(labelMap);

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


	private static List<Statement> createBlackList() {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i), null, labels.get(i)));
			if (labels.get(i) instanceof Resource) {
				blackList.addAll(dataset.getStatements((Resource) labels.get(i), null, instances.get(i)));
			}
		}

		return newBL;
	}
}
