package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.experiments.Experimenter;
import org.data2semantics.exp.experiments.KernelExperiment;
import org.data2semantics.exp.experiments.RDFFVvsKernelExperiment;
import org.data2semantics.exp.experiments.RDFKernelExperiment;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
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
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		int depth = 3;
		int[] iterations = {0, 2, 4, 6};

		dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		createGeoDataSet(10, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");

	
		ResultsTable resTable = new ResultsTable();
		resTable.setManWU(0.05);
		
		Experimenter experimenter = new Experimenter(2);
		Thread expT = new Thread(experimenter);
		expT.setDaemon(true);
		expT.start();	
		
		
		boolean inference = false;
		for (int i = 1; i <= depth; i++) {
			resTable.newRow("");
			for (int it : iterations) {
				LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
				KernelExperiment<RDFGraphKernel> exp = new RDFKernelExperiment(new RDFWLSubTreeKernel(it, i, inference, true, false), seeds, parms, dataset, instances, labels, blackList);
				
				System.out.println("Running WL RDF: " + i + " " + it);
				if (experimenter.hasSpace()) {
					experimenter.addExperiment(exp);
				}
				
				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
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
				if (Math.random() < 0.01) {
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