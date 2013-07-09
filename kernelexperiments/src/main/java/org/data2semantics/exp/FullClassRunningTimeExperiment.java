package org.data2semantics.exp;

import org.data2semantics.exp.old.utils.datasets.DataSetFactory;
import org.data2semantics.exp.old.utils.datasets.GeneralPredictionDataSetParameters;
import org.data2semantics.exp.old.utils.datasets.PropertyPredictionDataSet;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.proppred.kernels.graphkernels.GraphKernel;
import org.data2semantics.proppred.kernels.graphkernels.WLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernelString;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class FullClassRunningTimeExperiment extends FullClassExperiment {

	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		long seed = 11;
		long tic, toc;

		double[] fractions = {0.2, 0.4, 0.6, 0.8, 1};
		double[] fractionsSlow = {0.2, 0.4, 0.6, 0.8, 1};

		
		ResultsTable resTable = new ResultsTable();
		
		
		resTable.newRow("WLRDF FV");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		

			RDFFeatureVectorKernel k = new RDFWLSubTreeKernel(6,3,false, true);
			
			System.out.println("RDF WL FV: " + frac);
			tic = System.currentTimeMillis();
			k.computeFeatureVectors(dataset, instances, blackList);
			toc = System.currentTimeMillis();
			double[] comp = {toc-tic};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}	
		System.out.println(resTable);
		
		resTable.newRow("WLRDF Kernel");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		

			RDFGraphKernel k = new RDFWLSubTreeKernel(6,3,false, true);
			
			System.out.println("RDF WL Kernel: " + frac);
			tic = System.currentTimeMillis();
			k.compute(dataset, instances, blackList);
			toc = System.currentTimeMillis();
			double[] comp = {toc-tic};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}	
		System.out.println(resTable);
		
		resTable.newRow("WLRDF String FV");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		
			RDFFeatureVectorKernel k = new RDFWLSubTreeKernelString(6,3, false, true);
	
			
			System.out.println("RDF WL String FV: " + frac);
			tic = System.currentTimeMillis();
			k.computeFeatureVectors(dataset, instances, blackList);
			toc = System.currentTimeMillis();
			double[] comp = {toc-tic};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}
		System.out.println(resTable);
		
		resTable.newRow("WLRDF String Kernel");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		
			RDFGraphKernel k = new RDFWLSubTreeKernelString(6,3, false, true);
	
			
			System.out.println("RDF WL String: " + frac);
			tic = System.currentTimeMillis();
			k.compute(dataset, instances, blackList);
			toc = System.currentTimeMillis();
			double[] comp = {toc-tic};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}
		System.out.println(resTable);
	
		
		
		resTable.newRow("RDF IST");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		
			RDFGraphKernel k = new RDFIntersectionSubTreeKernel(3,1, false, true);
	
			
			System.out.println("RDF IST: " + frac);
			tic = System.currentTimeMillis();
			k.compute(dataset, instances, blackList);
			toc = System.currentTimeMillis();
			double[] comp = {toc-tic};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}
		System.out.println(resTable);
		
		
		
		
		resTable.newRow("WL FV");
		for (double frac : fractionsSlow) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
			toc = System.currentTimeMillis();
			double dsComp = toc-tic;
			
			FeatureVectorKernel k = new WLSubTreeKernel(6,true);
			
			System.out.println("WL: " + frac);
			tic = System.currentTimeMillis();
			k.computeFeatureVectors(ds.getGraphs());
			toc = System.currentTimeMillis();
			double[] comp = {(toc-tic) + dsComp};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}		
		System.out.println(resTable);
		
		
		resTable.newRow("WL Kernel");
		for (double frac : fractionsSlow) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasUnitClass");		
			tic = System.currentTimeMillis();
			PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
			toc = System.currentTimeMillis();
			double dsComp = toc-tic;
			
			GraphKernel k = new WLSubTreeKernel(6,true);
			
			System.out.println("WL: " + frac);
			tic = System.currentTimeMillis();
			k.compute(ds.getGraphs());
			toc = System.currentTimeMillis();
			double[] comp = {(toc-tic) + dsComp};
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
		}		
		System.out.println(resTable);
		
		
		
		
	}

	
}
