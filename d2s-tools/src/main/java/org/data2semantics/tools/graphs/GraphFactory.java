package org.data2semantics.tools.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.data2semantics.tools.rdf.*;


public class GraphFactory {
	private Map<String, String> labelDict;
	private RDFFileDataSet rdfDataSet;

	public GraphFactory(RDFFileDataSet rdfDataSet) {
		labelDict = new HashMap<String, String>();
		this.rdfDataSet = rdfDataSet;
	}


	public List<Graph> getGraphsFromNodes(List<Resource> startNodes, int depth) {
		List<Graph> graphs = new ArrayList<Graph>();

		for (Resource startNode : startNodes) {
			graphs.add(getGraphFromNode(startNode, depth));
		}

		return graphs;
	}

	/*
	 * TODO change to use other getStatements method
	 * 
	 */
	private Graph getGraphFromNode(Resource startNode, int depth) {
		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		Graph graph = new Graph();

		queryNodes.add(startNode);

		for (int i = 0; i < depth; i++) {
			newQueryNodes = new ArrayList<Resource>();

			for (Resource queryNode : queryNodes) {
				newQueryNodes.addAll(addStatements2Graph(graph, rdfDataSet.getStatements(queryNode, false), false));
				newQueryNodes.addAll(addStatements2Graph(graph, rdfDataSet.getStatements(queryNode, true), true));
			}
			newQueryNodes.remove(startNode);
			queryNodes = newQueryNodes;
		}

		return graph;
	}

	private List<Resource> addStatements2Graph(Graph graph, List<Statement> statements, boolean fromObject) {
		String edgeLabel, nodeLabel, newEdgeLabel, newNodeLabel, startNodeLabel, newStartNodeLabel = null;
		List<Resource> newNodes = new ArrayList<Resource>();
		
		for (Statement statement : statements) {
			startNodeLabel = (fromObject) ? statement.getObject().toString() : statement.getSubject().toString();
			nodeLabel = (fromObject) ? statement.getSubject().toString() : statement.getObject().toString();
			edgeLabel = statement.getSubject().toString() + " " + statement.getPredicate().toString() + " " + statement.getObject().toString();
										
			newStartNodeLabel = labelDict.get(startNodeLabel);
			if (newStartNodeLabel == null) {
				newStartNodeLabel = Integer.toString(labelDict.size() + 1);
				labelDict.put(startNodeLabel, newStartNodeLabel);
			}			
			
			newNodeLabel = labelDict.get(nodeLabel);
			if (newNodeLabel == null) {
				newNodeLabel = Integer.toString(labelDict.size() + 1);
				labelDict.put(nodeLabel, newNodeLabel);
			}

			newEdgeLabel = labelDict.get(edgeLabel);
			if (newEdgeLabel == null) {
				newEdgeLabel = Integer.toString(labelDict.size() + 1);
				labelDict.put(edgeLabel, newEdgeLabel);
			}
			
			graph.addNode(newStartNodeLabel);
			graph.addNode(newNodeLabel);
						
			if (fromObject) {
				graph.addEdge(newEdgeLabel, newNodeLabel, newStartNodeLabel);
			} else {
				graph.addEdge(newEdgeLabel, newStartNodeLabel, newNodeLabel);
			}
			
			if (fromObject) {
				newNodes.add(statement.getSubject());
			} else {
				if (statement.getObject() instanceof Resource) {
					newNodes.add((Resource) statement.getObject());
				}
			}
		}
		
		return newNodes;
	}

	public Map<String, String> getLabelDict() {
		return labelDict;
	}

}
