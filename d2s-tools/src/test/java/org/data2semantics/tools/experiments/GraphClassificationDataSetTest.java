package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class GraphClassificationDataSetTest {

	@Test
	public void test() {
		
		RDFDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		RDFDataSet testSetB = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_no_schema.n3", RDFFormat.N3);
		
		
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
				
		System.out.println(DataSetFactory.createClassificationDataSet(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false).getLabel());
	}

}
