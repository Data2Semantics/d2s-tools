package org.data2semantics.exp;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;


public abstract class CompareExperiment {
	protected static RDFDataSet dataset;
	protected static List<Resource> instances;
	protected static List<Value> labels;
	protected static List<Statement> blackList;
	protected static Map<Resource, List<Statement>> blackLists;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
				
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

	

	protected static void removeSmallClasses(int smallClassSize) {
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

	protected static void capClassSize(int classSizeCap, long seed) {
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


	protected static void createBlackList() {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i), null, labels.get(i)));
			if (labels.get(i) instanceof Resource) {
				blackList.addAll(dataset.getStatements((Resource) labels.get(i), null, instances.get(i)));
			}
		}

		blackList = newBL;
		blackLists = new HashMap<Resource, List<Statement>>();
		
		for (Resource instance : instances) {
			blackLists.put(instance, blackList);
		}	
	}
	
	protected static void saveResults(ResultsTable resTable, String filename) {
		try
		{
			FileOutputStream fileOut =
					new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(resTable);
			out.close();
			fileOut.close();
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}
	
	protected static void saveResults(String results, String filename) {
		try
		{
			
			FileWriter out = new FileWriter(filename);
			out.write(results);
			out.close();
			
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}
}
