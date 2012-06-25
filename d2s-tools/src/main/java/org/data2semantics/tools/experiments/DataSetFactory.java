package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Graphs;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import edu.uci.ics.jung.graph.DirectedGraph;


// TODO add random seed to dataset initialization
public class DataSetFactory {

	public static GraphClassificationDataSet createClassificationDataSet(DataSetParameters params) {
		return createClassificationDataSet(params.getRdfDataSet(), params.getProperty(), params.getBlackList(), params.getDepth(), params.isIncludeInverse(), params.isIncludeInference(), params.getSeed());
	}
	
	public static GraphClassificationDataSet createClassificationDataSet(RDFDataSet rdfDataSet, String property, List<String> blackList, int depth, boolean includeInverse, boolean includeInference) {
		return createClassificationDataSet(rdfDataSet, property, blackList, depth, includeInverse, includeInference, 424242424);
	}
	
	public static GraphClassificationDataSet createClassificationDataSet(RDFDataSet rdfDataSet, String property, List<String> blackList, int depth, boolean includeInverse, boolean includeInference, long seed) {
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
		label.append(", ");
		label.append(seed);
			
		List<Statement> triples = rdfDataSet.getStatementsFromStrings(null, property, null, false);	
		for (Statement triple : triples) {
			if (triple.getSubject() instanceof URI) {
				graphs.add(GraphFactory.createDirectedGraph(rdfDataSet.getSubGraph((URI) triple.getSubject(), depth, includeInverse, includeInference)));
				labels.add(triple.getObject().toString());
			}
		}
		Collections.shuffle(graphs, new Random(seed));
		Collections.shuffle(labels, new Random(seed));
				
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			Graphs.removeVerticesAndEdges(graph, null, blackList);
		}
		
		return new GraphClassificationDataSet(label.toString(), graphs, labels);
	}
	
}
