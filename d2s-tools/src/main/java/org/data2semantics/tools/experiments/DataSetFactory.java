package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Graphs;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import edu.uci.ics.jung.graph.DirectedGraph;

public class DataSetFactory {

	public static GraphClassificationDataSet createClassificationDataSet(RDFDataSet rdfDataSet, String property, List<String> blackList, int depth, boolean includeInverse, boolean includeInference) {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();
		List<String> labels = new ArrayList<String>();
		StringBuffer label = new StringBuffer();
		
		label.append(rdfDataSet.getLabel());
		label.append(", ");
		label.append(property);
		label.append(", ");
		label.append(depth);
		label.append(", ");
		label.append(includeInverse);
		label.append(", ");
		label.append(includeInference);
		
		
		List<Statement> triples = rdfDataSet.getStatementsFromStrings(null, property, null, false);	
		for (Statement triple : triples) {
			if (triple.getSubject() instanceof URI) {
				graphs.add(GraphFactory.createDirectedGraph(rdfDataSet.getSubGraph((URI) triple.getSubject(), depth, includeInverse, includeInference)));
				labels.add(triple.getObject().toString());
			}
		}
				
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			Graphs.removeVerticesAndEdges(graph, null, blackList);
		}
		
		return new GraphClassificationDataSet(label.toString(), graphs, labels);
	}
	
}
