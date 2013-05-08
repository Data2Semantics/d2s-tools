package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.Experimenter;
import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFLinearVSKernelExperiment;
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class FullThemeExperiment extends CompareExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = { 1, 10, 100, 1000};	
		// 0.001, 0.01, 0.1,
		int depth = 3;
		int[] iterations = {0, 2, 4};

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		createGeoDataSet(50, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");

	
		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		
		boolean inference = false;
		for (int i = 1; i <= depth; i++) {			
			for (int it : iterations) {
				resTable.newRow("");
				
				LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
				LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
				KernelExperiment<RDFWLSubTreeKernel> exp = new RDFLinearVSKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true), seeds, parms, linParms, dataset, instances, labels, blackList);
				
				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		
		saveResults(resTable, "geo_theme.ser");
		
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		saveResults(resTable.toString(), "geo_theme_full.txt");
	}
	
	

	private static void createGeoDataSet(int minSize, String property) {
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
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
				if (Math.random() < 0.05) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
			}
		}

		removeSmallClasses(minSize);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
	}

}
