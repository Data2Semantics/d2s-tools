package org.data2semantics.exp.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.exp.experiments.DataSetFactory;
import org.data2semantics.exp.experiments.LinkPredictionDataSet;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class LinkPredictionDataSetTest {

	@Test
	public void test() {
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
		
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\datasets\\aifb\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		LinkPredictionDataSet set = DataSetFactory.createLinkPredictonDataSet(testSet, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false);
		System.out.println(set.getLabel());
		System.out.println(set.getLabels().size());
	}

}
