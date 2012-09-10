package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.kernels.IntersectionGraphPathKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class LinkPredictionExperimentTest {

	@Test
	public void test() {
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		//long[] seeds = {11};
		double[] cs = {0.01, 0.1, 1, 10, 100};	
		//double[] cs = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};	
		//double[] cs = {0.1};
		//double[] cs = {1};	
		
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
		
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\datasets\\aifb\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		LinkPredictionDataSet set = DataSetFactory.createLinkPredictonDataSet(testSet, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false);
		
		
		
		
		new LinkPredictionExperiment(set, new WLSubTreeKernel(2), new WLSubTreeKernel(2), 1, 0, seeds, cs).run();
		new LinkPredictionExperiment(set, new WLSubTreeKernel(2), new WLSubTreeKernel(2), 0.75, 0.25, seeds, cs).run();
		new LinkPredictionExperiment(set, new WLSubTreeKernel(2), new WLSubTreeKernel(2), 0.5, 0.5, seeds, cs).run();
		new LinkPredictionExperiment(set, new WLSubTreeKernel(2), new WLSubTreeKernel(2), 0.25, 0.75, seeds, cs).run();
		new LinkPredictionExperiment(set, new WLSubTreeKernel(2), new WLSubTreeKernel(2), 0, 1, seeds, cs).run();
		
		
		
		/*
		
		new LinkPredictionExperiment(set, new IntersectionSubTreeKernel(2, 1), new IntersectionSubTreeKernel(2, 1), 1, 0, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionSubTreeKernel(2, 1), new IntersectionSubTreeKernel(2, 1), 0.75, 0.25, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionSubTreeKernel(2, 1), new IntersectionSubTreeKernel(2, 1), 0.5, 0.5, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionSubTreeKernel(2, 1), new IntersectionSubTreeKernel(2, 1), 0.25, 0.75, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionSubTreeKernel(2, 1), new IntersectionSubTreeKernel(2, 1), 0, 1, seeds, cs).run();
		
		*/
		
		/*
		
		new LinkPredictionExperiment(set, new IntersectionGraphPathKernel(2, 1), new IntersectionGraphPathKernel(2, 1), 1, 0, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionGraphPathKernel(2, 1), new IntersectionGraphPathKernel(2, 1), 0.75, 0.25, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionGraphPathKernel(2, 1), new IntersectionGraphPathKernel(2, 1), 0.5, 0.5, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionGraphPathKernel(2, 1), new IntersectionGraphPathKernel(2, 1), 0.25, 0.75, seeds, cs).run();
		new LinkPredictionExperiment(set, new IntersectionGraphPathKernel(2, 1), new IntersectionGraphPathKernel(2, 1), 0, 1, seeds, cs).run();
		
		*/
		
		
	}

}
