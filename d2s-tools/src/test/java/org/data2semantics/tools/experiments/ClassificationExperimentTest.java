package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class ClassificationExperimentTest {

	@Test
	public void test() {
		
		List<DataSetParameters> dataSetsParams = new ArrayList<DataSetParameters>();
		
		RDFDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		RDFDataSet testSetB = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_no_schema.n3", RDFFormat.N3);
		
		
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
		
		long[] seeds = {11}; //,21,31,41,51,61,71,81,91,101};
		
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));
		
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));
		
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		
		GraphClassificationDataSet dataset;
		ClassificationExperiment exp;
		
		for (DataSetParameters params : dataSetsParams) {
			for (int i = 0; i < 3; i++) {
				dataset = DataSetFactory.createClassificationDataSet(params);
				exp = new ClassificationExperiment(dataset, new WLSubTreeKernel(dataset.getGraphs(), i), seeds);
				exp.run();
			}			
		}					
	}

}
