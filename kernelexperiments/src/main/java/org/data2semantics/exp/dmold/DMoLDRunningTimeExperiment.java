package org.data2semantics.exp.dmold;

import org.data2semantics.exp.FullThemeExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class DMoLDRunningTimeExperiment extends FullThemeExperiment {
	private static String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataDir = args[i];
			}
		}
		
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		long[] seeds = {11,21,31};
		long tic, toc;

		double[] fractions = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
	
		ResultsTable resTable = new ResultsTable();

		for (double frac : fractions) {
			resTable.newRow("Fraction: " + frac);

			double[] comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFFeatureVectorKernel k = new RDFWLSubTreeKernel(6,3,false, true);

				System.out.println("RDF WL FV: " + frac);
				tic = System.currentTimeMillis();
				k.computeFeatureVectors(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			Result res = new Result(comp, "RDF WL FV");
			resTable.addResult(res);
	
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFGraphKernel k = new RDFWLSubTreeKernel(6,3,false, true);

				System.out.println("RDF WL Kernel: " + frac);
				tic = System.currentTimeMillis();
				k.compute(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "RDF WL Kernel");
			resTable.addResult(res);
	
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFFeatureVectorKernel k = new RDFIntersectionTreeEdgeVertexPathKernel(3,false, false, true);

				System.out.println("RDF ITP FV: " + frac);
				tic = System.currentTimeMillis();
				k.computeFeatureVectors(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "RDF ITP FV");
			resTable.addResult(res);
	
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFGraphKernel k = new RDFIntersectionTreeEdgeVertexPathKernel(3,false, false, true);

				System.out.println("RDF ITP Kernel: " + frac);
				tic = System.currentTimeMillis();
				k.compute(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "RDF ITP Kernel");
			resTable.addResult(res);
		
			
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
				RDFGraphKernel k = new RDFIntersectionSubTreeKernel(3,1, false, true);


				System.out.println("RDF IST: " + frac);
				tic = System.currentTimeMillis();
				k.compute(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "RDF IST");
			resTable.addResult(res);
	
			System.out.println(resTable);
		}
		System.out.println(resTable);
	}
}
