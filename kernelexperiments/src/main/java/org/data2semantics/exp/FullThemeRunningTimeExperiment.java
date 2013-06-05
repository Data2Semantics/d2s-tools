package org.data2semantics.exp;

import org.data2semantics.exp.experiments.DataSetFactory;
import org.data2semantics.exp.experiments.GeneralPredictionDataSetParameters;
import org.data2semantics.exp.experiments.PropertyPredictionDataSet;
import org.data2semantics.exp.experiments.Result;
import org.data2semantics.exp.experiments.ResultsTable;
import org.data2semantics.proppred.kernels.FeatureVectorKernel;
import org.data2semantics.proppred.kernels.GraphKernel;
import org.data2semantics.proppred.kernels.IntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernelString;
import org.data2semantics.proppred.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class FullThemeRunningTimeExperiment extends FullThemeExperiment {
	
	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		long seed = 11;
		long tic, toc;

		double[] fractions = {0.1, 0.15, 0.2, 0.25, 0.3};
		double[] fractionsSlow = {0.1, 0.15, 0.2};

		
		ResultsTable resTable = new ResultsTable();
		
		resTable.newRow("WLRDF FV");
		for (double frac : fractions) {
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
			RDFGraphKernel k = new RDFIntersectionSubTreeKernel(1,3, false, true);
	
			
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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
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
			createGeoDataSet((int)(1000 * frac), frac, seed, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
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
