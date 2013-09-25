package org.data2semantics.exp;

import java.util.Arrays;

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
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.kernels.text.TextUtils;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class FullThemeRunningTimeExperiment extends FullThemeExperiment {

	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		long[] seeds = {11,21,31};
		long tic, toc;

		double[] fractions = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
		double[] fractionsSlow = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};


		ResultsTable resTable = new ResultsTable();

		for (double frac : fractions) {
			resTable.newRow("");

			//resTable.newRow("WLRDF FV");
			//for (double frac : fractions) {
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
			Result res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);

			//resTable.newRow("WLRDF Kernel");
			//for (double frac : fractions) {
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
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}	
			//System.out.println(resTable);

			//resTable.newRow("WLRDF text FV");
			//for (double frac : fractions) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFFeatureVectorKernel k = new RDFWLSubTreeWithTextKernel(6,3,false, false);

				System.out.println("RDF WL text FV: " + frac);
				tic = System.currentTimeMillis();
				TextUtils.computeTFIDF(Arrays.asList(k.computeFeatureVectors(dataset, instances, blackList)));				
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);


			//resTable.newRow("EVP FV");
			//for (double frac : fractions) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFFeatureVectorKernel k = new RDFIntersectionTreeEdgeVertexPathKernel(3,false, false, true);

				System.out.println("RDF EVP FV: " + frac);
				tic = System.currentTimeMillis();
				k.computeFeatureVectors(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);

			//resTable.newRow("EVP Kernel");
			//for (double frac : fractions) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFGraphKernel k = new RDFIntersectionTreeEdgeVertexPathKernel(3,false, false, true);

				System.out.println("RDF EVP Kernel: " + frac);
				tic = System.currentTimeMillis();
				k.compute(dataset, instances, blackList);
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);

			//resTable.newRow("EVP text FV");
			//for (double frac : fractions) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		

				RDFFeatureVectorKernel k = new RDFIntersectionTreeEdgeVertexPathWithTextKernel(3,false, false, false);

				System.out.println("EVP text FV: " + frac);
				tic = System.currentTimeMillis();
				TextUtils.computeTFIDF(Arrays.asList(k.computeFeatureVectors(dataset, instances, blackList)));				
				toc = System.currentTimeMillis();
				comp[i] = toc-tic;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);




			//resTable.newRow("RDF IST");
			//for (double frac : fractions) {
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
			res = new Result(comp, "comp time");
			resTable.addResult(res);
			//}
			//System.out.println(resTable);



			
		//resTable.newRow("WL FV");
		//for (double frac : fractionsSlow) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
				tic = System.currentTimeMillis();
				PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
				toc = System.currentTimeMillis();
				double dsComp = toc-tic;

				FeatureVectorKernel k = new WLSubTreeKernel(6,true);

				System.out.println("WL: " + frac);
				tic = System.currentTimeMillis();
				k.computeFeatureVectors(ds.getGraphs());
				toc = System.currentTimeMillis();
				comp[i] = (toc-tic) + dsComp;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
		//}		
		//System.out.println(resTable);


		//resTable.newRow("WL Kernel");
		//for (double frac : fractionsSlow) {
			comp = new double[seeds.length];
			for (int i = 0; i < seeds.length; i++) {
				createGeoDataSet((int)(1000 * frac), frac, seeds[i], "http://data.bgs.ac.uk/ref/Lexicon/hasTheme");		
				tic = System.currentTimeMillis();
				PropertyPredictionDataSet ds = DataSetFactory.createPropertyPredictionDataSet(new GeneralPredictionDataSetParameters(dataset, blackLists, instances, 3, false, true));
				toc = System.currentTimeMillis();
				double dsComp = toc-tic;

				GraphKernel k = new WLSubTreeKernel(6,true);

				System.out.println("WL: " + frac);
				tic = System.currentTimeMillis();
				k.compute(ds.getGraphs());
				toc = System.currentTimeMillis();
				comp[i] = (toc-tic) + dsComp;
			}
			res = new Result(comp, "comp time");
			resTable.addResult(res);
		}		
		//}
		System.out.println(resTable);
	}
}
