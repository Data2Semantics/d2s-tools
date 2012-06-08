package org.data2semantics.tools.graphs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import org.data2semantics.tools.rdf.*;

public class GraphFactoryTest {

	@Test
	public void test() {
		RDFFileDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		GraphFactory graphFac = new GraphFactory(testSet);

		List<Statement> triples = testSet.getInstanceURIs("http://swrc.ontoware.org/ontology#affiliation", null);
		List<Resource> queryNodes = new ArrayList<Resource>();
		queryNodes.add(triples.get(0).getSubject());

		List<Graph> graphs = graphFac.getGraphsFromNodes(queryNodes, 2);

		System.out.println("# of Graphs: " + graphs.size());

		System.out.println(graphs.get(0));

		System.out.println("Dictionary ----");
		for (String key : graphFac.getLabelDict().keySet()) {
			if (graphs.get(0).getEdges().keySet().contains(graphFac.getLabelDict().get(key))) {
				System.out.println(key + " -> " + graphFac.getLabelDict().get(key));
			}
		}


	}

}
