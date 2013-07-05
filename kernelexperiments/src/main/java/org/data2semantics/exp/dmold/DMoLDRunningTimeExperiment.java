package org.data2semantics.exp.dmold;

import java.util.Arrays;

import org.data2semantics.exp.FullThemeExperiment;
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
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernelString;
import org.data2semantics.proppred.kernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.kernels.WLSubTreeKernel;
import org.data2semantics.proppred.libsvm.text.TextUtils;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class DMoLDRunningTimeExperiment extends FullThemeExperiment {

	public static void main(String[] args) {
		String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
		dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		long[] seeds = {11,21,31};
		long tic, toc;

		double[] fractions = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
	
		ResultsTable resTable = new ResultsTable();

		for (double frac : fractions) {
			resTable.newRow("");

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
	
			System.out.println(resTable);
		}
		System.out.println(resTable);
	}
}