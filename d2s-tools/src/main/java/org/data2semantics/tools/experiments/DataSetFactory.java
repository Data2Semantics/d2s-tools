package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Graphs;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;


// TODO add random seed to dataset initialization
public class DataSetFactory {

	public static GraphClassificationDataSet createClassificationDataSet(DataSetParameters params) {
		return createClassificationDataSet(params.getRdfDataSet(), params.getProperty(), params.getBlackList(), params.getDepth(), params.isIncludeInverse(), params.isIncludeInference());
	}


	public static GraphClassificationDataSet createClassificationDataSet(RDFDataSet rdfDataSet, String property, List<String> blackList, int depth, boolean includeInverse, boolean includeInference) {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();
		List<String> labels = new ArrayList<String>();
		List<Vertex<String>> rootVertices = new ArrayList<Vertex<String>>();
		StringBuffer label = new StringBuffer();

		label.append(rdfDataSet.getLabel());
		label.append(", ");
		label.append(property);
		label.append(", depth=");
		label.append(depth);
		label.append(", Inverse=");
		label.append(includeInverse);
		label.append(", Inference=");
		label.append(includeInference);

		List<Statement> triples = rdfDataSet.getStatementsFromStrings(null, property, null, false);	

		for (Statement triple : triples) {
			if (triple.getSubject() instanceof URI) {
				DirectedGraph<Vertex<String>, Edge<String>> graph = GraphFactory.createDirectedGraph(rdfDataSet.getSubGraph((URI) triple.getSubject(), depth, includeInverse, includeInference));
				graphs.add(graph);
				labels.add(triple.getObject().toString());
				rootVertices.add(findVertex(graph, triple.getSubject().toString()));
				Graphs.removeVerticesAndEdges(graph, null, blackList);
			}
		}

		return new GraphClassificationDataSet(label.toString(), graphs, labels, rootVertices);
	}

	
	public static LinkPredictionDataSet createLinkPredictonDataSet(DataSetParameters params) {
		return createLinkPredictonDataSet(params.getRdfDataSet(), params.getClassA(), params.getClassB(), params.getProperty(), params.getBlackList(), params.getDepth(), params.isIncludeInverse(), params.isIncludeInference());
	}
	

	public static LinkPredictionDataSet createLinkPredictonDataSet(RDFDataSet rdfDataSet, String classA, String classB, String property, List<String> blackList, int depth, boolean includeInverse, boolean includeInference) {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphsA = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphsB = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();
		List<Vertex<String>> rootVerticesA = new ArrayList<Vertex<String>>();
		List<Vertex<String>> rootVerticesB = new ArrayList<Vertex<String>>();
		Map<Pair<DirectedGraph<Vertex<String>,Edge<String>>>, Boolean> labels = new HashMap<Pair<DirectedGraph<Vertex<String>,Edge<String>>>, Boolean>();
		
		StringBuffer label = new StringBuffer();
		label.append(rdfDataSet.getLabel());
		label.append(", ");
		label.append(property);
		label.append(", depth=");
		label.append(depth);
		label.append(", Inverse=");
		label.append(includeInverse);
		label.append(", Inference=");
		label.append(includeInference);
		

		List<Statement> triples = rdfDataSet.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", classA, false);	
		for (Statement triple : triples) {
			DirectedGraph<Vertex<String>, Edge<String>> graph = GraphFactory.createDirectedGraph(rdfDataSet.getSubGraph((URI) triple.getSubject(), depth, includeInverse, includeInference));
			graphsA.add(graph);
			rootVerticesA.add(findVertex(graph, triple.getSubject().toString()));
			Graphs.removeVerticesAndEdges(graph, null, blackList);
		}
		
		triples = rdfDataSet.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", classB, false);	
		for (Statement triple : triples) {
			DirectedGraph<Vertex<String>, Edge<String>> graph = GraphFactory.createDirectedGraph(rdfDataSet.getSubGraph((URI) triple.getSubject(), depth, includeInverse, includeInference));
			graphsB.add(graph);
			rootVerticesB.add(findVertex(graph, triple.getSubject().toString()));
			Graphs.removeVerticesAndEdges(graph, null, blackList);
		}
		
		for (int i = 0; i < rootVerticesA.size(); i++) {
			for (int j = 0; j < rootVerticesB.size(); j++) {
				List<Statement> triples3 = rdfDataSet.getStatementsFromStrings(rootVerticesA.get(i).getLabel(), null, rootVerticesB.get(j).getLabel(), false);
				labels.put(new Pair<DirectedGraph<Vertex<String>, Edge<String>>>(graphsA.get(i), graphsB.get(j)), false);
				for (Statement triple : triples3) {
					if (triple.getPredicate().toString().equals(property)) {
						labels.put(new Pair<DirectedGraph<Vertex<String>, Edge<String>>>(graphsA.get(i), graphsB.get(j)), true);
					}
				}
			}
		}

		return new LinkPredictionDataSet(label.toString(), graphsA, graphsB, rootVerticesA, rootVerticesB, labels);
	}




	private static Vertex<String> findVertex(DirectedGraph<Vertex<String>, Edge<String>> graph, String vertexLabel) {	
		for (Vertex<String> vertex : graph.getVertices()) {
			if (vertex.getLabel().equals(vertexLabel)) {
				return vertex;
			}
		}
		return null;
	}

}
