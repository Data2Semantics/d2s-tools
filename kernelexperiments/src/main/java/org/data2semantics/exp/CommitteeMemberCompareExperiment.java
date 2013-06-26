package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFOldKernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearVSKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEAR;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class CommitteeMemberCompareExperiment extends RDFMLExperiment {

	public static void main(String[] args) {
		//long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		long[] seeds = {11,31,51,71,91};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	
		
		//int[] depths = {1, 2, 3};
		//int[] iterations = {0, 2, 4, 6};
		int[] depths = {1,2,3};
		int[] iterations = {0,2,4,6};
		
		createCommitteeMemberPredictionDataSet();
		

		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		resTable.setDigits(3);
		
		boolean inference = true;
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("");
				
				
				LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
				KernelExperiment<RDFGraphKernel> exp = new RDFOldKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, parms, dataset, instances, labels, blackList);
				
				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		
		saveResults(resTable, "cmp.ser");
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "cmp_full.txt");
		
	}
	
	private static void createCommitteeMemberPredictionDataSet() {
		RDFFileDataSet testSetA = new RDFFileDataSet("datasets/eswc-2010-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/eswc-2011-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/eswc-2012-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2008-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2009-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2012-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
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
