package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Implementation of the IntersectionGraphKernel from the ESWC 2012 by Loetsch et al. 
 * Not necessarily the most efficient implementation.
 * 
 * @author Gerben
 *
 */
public class IntersectionGraphKernel extends GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> {
	private int maxLength;
	private double discountFactor;
	
	public IntersectionGraphKernel(int maxLength, double discountFactor) {
		this(maxLength, discountFactor, true);
	}
	
	public IntersectionGraphKernel(int maxLength, double discountFactor, boolean normalize) {
		super(normalize);
		this.label = "Intersection Graph Kernel, maxLength=" + maxLength + ", lambda=" + discountFactor;
		this.maxLength = maxLength;
		this.discountFactor = discountFactor;
	}

	@Override
	public double[][] compute(
			List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {
		double[][] kernel = initMatrix(trainGraphs.size(), trainGraphs.size());
		DirectedGraph<Vertex<String>, Edge<String>> graph;
		
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graphT : trainGraphs) {
			graphT.getRootVertex().setLabel(ROOTID);
		}
		
		for (int i = 0; i < trainGraphs.size(); i++) {
			for (int j = i; j < trainGraphs.size(); j++) {				
				graph = computeIntersectionGraph(trainGraphs.get(i), trainGraphs.get(j));
				kernel[i][j] = subGraphCount(graph, maxLength, discountFactor);
				kernel[j][i] = kernel[i][j];
			}
		}
		
		if (normalize) {
			return normalize(kernel);
		} else {
			return kernel;
		}
	}

	@Override
	public double[][] compute(
			List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs,
			List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs) {
		
		double[][] kernel = initMatrix(testGraphs.size(), trainGraphs.size());
		DirectedGraph<Vertex<String>, Edge<String>> graph;
		double[] ssTest = new double[testGraphs.size()];
		double[] ssTrain = new double[trainGraphs.size()];
		
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graphT : trainGraphs) {
			graphT.getRootVertex().setLabel(ROOTID);
		}
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graphT : testGraphs) {
			graphT.getRootVertex().setLabel(ROOTID);
		}
		
		for (int i = 0; i < testGraphs.size(); i++) {
			for (int j = 0; j < trainGraphs.size(); j++) {				
				graph = computeIntersectionGraph(testGraphs.get(i), trainGraphs.get(j));
				kernel[i][j] = subGraphCount(graph, maxLength, discountFactor);
			}
		}
		
		for (int i = 0; i < testGraphs.size(); i++) {
			graph = computeIntersectionGraph(testGraphs.get(i), testGraphs.get(i));
			ssTest[i] = subGraphCount(graph, maxLength, discountFactor);
		}
		
		for (int i = 0; i < trainGraphs.size(); i++) {
			graph = computeIntersectionGraph(trainGraphs.get(i), trainGraphs.get(i));
			ssTrain[i] = subGraphCount(graph, maxLength, discountFactor);
		}
			
		if (normalize) {	
			return normalize(kernel, ssTrain, ssTest);		
		} else {		
			return kernel;
		}
	}

	public DirectedGraph<Vertex<String>, Edge<String>> computeIntersectionGraph(DirectedGraph<Vertex<String>, Edge<String>> graphA, DirectedGraph<Vertex<String>, Edge<String>> graphB) {	
		DirectedGraph<Vertex<String>, Edge<String>> iGraph = new DirectedSparseMultigraph<Vertex<String>, Edge<String>>();
		List<String> evA = new ArrayList<String>();
		List<String> evB = new ArrayList<String>();		
		Map<String, Edge<String>> eMap = new HashMap<String, Edge<String>>();
		Map<Vertex<String>, Vertex<String>> vMap = new HashMap<Vertex<String>, Vertex<String>>();


		for (Edge<String> edgeA : graphA.getEdges()) {
			evA.add(graphA.getSource(edgeA).getLabel() + edgeA.getLabel() + graphA.getDest(edgeA).getLabel());
			eMap.put(graphA.getSource(edgeA).getLabel() + edgeA.getLabel() + graphA.getDest(edgeA).getLabel(), edgeA);
		}

		for (Edge<String> edgeB : graphB.getEdges()) {
			evB.add(graphB.getSource(edgeB).getLabel() + edgeB.getLabel() + graphB.getDest(edgeB).getLabel());
		}

		Collections.sort(evA);
		Collections.sort(evB);	

		Iterator<String> itA = evA.iterator();
		Iterator<String> itB = evB.iterator();

		int comparison = 0;
		String edgeA = null;
		String edgeB = null;
		boolean stop = false;
		while (!stop) {
			if (comparison == 0 && itA.hasNext() && itB.hasNext()) {
				edgeA = itA.next();
				edgeB = itB.next();
			} else if (comparison < 0 && itA.hasNext()) {
				edgeA = itA.next();
			} else if (comparison > 0 && itB.hasNext()){
				edgeB = itB.next();
			} else {
				stop = true;
			}

			if (!stop) {
				comparison = edgeA.compareTo(edgeB);
				if (comparison == 0) {
					Edge<String> edge = eMap.get(edgeA);
					Vertex<String> v1 = graphA.getSource(edge);
					Vertex<String> v2 = graphA.getDest(edge);
					Vertex<String> v1n = vMap.get(v1);
					Vertex<String> v2n = vMap.get(v2);

					if (v1n == null) {
						v1n = new Vertex<String>(v1);
						vMap.put(v1, v1n);
					}
					if (v2n == null) {
						v2n = new Vertex<String>(v2);
						vMap.put(v2, v2n);
					}
					iGraph.addEdge(new Edge<String>(edge), v1n, v2n, EdgeType.DIRECTED);
				} 
			}
		}
		return iGraph;
	}

	protected double subGraphCount(DirectedGraph<Vertex<String>, Edge<String>> graph, int maxLength, double discountFactor) {
		double n = graph.getEdgeCount();
		double score = 0;
		double prevScore = 1;
		
		for (int i = 0; i < n && i < maxLength; i++) {
			prevScore = prevScore * (n - i) / (i + 1);
			score += Math.pow(discountFactor, i+1) * prevScore;
		}
		return score;
	}
}
