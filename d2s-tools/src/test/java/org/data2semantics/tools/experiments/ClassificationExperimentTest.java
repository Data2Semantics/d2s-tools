package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.kernels.IntersectionGraphKernel;
import org.data2semantics.tools.kernels.IntersectionGraphPathKernel;
import org.data2semantics.tools.kernels.IntersectionGraphWalkKernel;
import org.data2semantics.tools.kernels.IntersectionPartialSubTreeKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class ClassificationExperimentTest {

	@Test
	public void test() {
		
		List<DataSetParameters> dataSetsParams = new ArrayList<DataSetParameters>();
		
		RDFDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\datasets\\aifb\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		RDFDataSet testSetB = new RDFFileDataSet("D:\\workspaces\\datasets\\aifb\\aifb-fixed_no_schema.n3", RDFFormat.N3);
		
		
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
		
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		//double[] cs = {0.01, 0.1, 1, 10, 100};	
		double[] cs = {1};	
		
		
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));
		
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));
		
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		
		GraphClassificationDataSet dataset;
		ClassificationExperiment exp;
		
		double[][] results = new double[dataSetsParams.size()][3];
		
		int j = 0;
		for (DataSetParameters params : dataSetsParams) {
			for (int i = 0; i < 3; i++) {
				dataset = DataSetFactory.createClassificationDataSet(params);
				dataset.removeSmallClasses(5);
				//exp = new ClassificationExperiment(dataset, new WLSubTreeKernel(dataset.getGraphs(), i), seeds, cs);
				//exp = new ClassificationExperiment(dataset, new IntersectionSubTreeKernel(dataset.getGraphs(), dataset.getRootVertices(), i, 1), seeds, cs);
				exp = new ClassificationExperiment(dataset, new IntersectionGraphPathKernel(dataset.getGraphs(), i, 1), seeds, cs);
				
				exp.run();
				results[j][i] = exp.getAccuracy();
			}
			j++;
		}
		
		for (int i = 0; i < results.length; i++) {
			for (int k = 0; k < results[i].length; k++) {
				System.out.print(Math.round(results[i][k] * 100) + " ");
			}
			System.out.println("");
		}
	}

}
