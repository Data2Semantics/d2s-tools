package org.data2semantics.tools.graphs;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphsTest
{

	@Test
	public void test()
	{
		File file = new File("/Users/Peter/Documents/datasets/rdf/aifb/aifb-fixed_complete.owl");

		DirectedGraph<Node<String>, Edge<String>> jungGraph = Graphs.graphFromRDF(file);	
	}

}
