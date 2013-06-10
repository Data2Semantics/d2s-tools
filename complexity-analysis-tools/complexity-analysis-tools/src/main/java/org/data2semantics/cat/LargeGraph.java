package org.data2semantics.cat;

import static java.util.Collections.reverseOrder;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import org.apache.commons.collections15.Transformer;
import org.data2semantics.tools.graphs.Graphs;
import org.lilian.experiment.Result;
import org.lilian.experiment.State;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.graphs.jung.FloydWarshall;


/**
 * Graph measures for large graphs. These methods are generally a low polynomial 
 * in the number of edges or vertices
 *  
 * @author Peter
 *
 * @param <V>
 * @param <E>
 */
public class LargeGraph<N> extends HugeGraph<N>
{
	private static final double PL_ACCURACY = 0.1;
	private static final int PL_DATASAMPLE = 1000;
	
	public @State double diameter = Double.NaN;	
	public @State double largestComponentSize = Double.NaN;
	public @State double meanDistance = Double.NaN;
	
	public @State List<Pair> pairs;  
	public @State List<Integer> degrees;

	public @State List<Pair> vertexLabelFrequencies;
	public @State List<Pair> edgeLabelFrequencies;	
	
	public @State Graph<N> largestComponent;

	private double plExponent;
	private int plMin;
	private double plSignificance;

	public LargeGraph(Graph<N> graph)
	{
		super(graph);
	}
	
	@Override
	protected void setup()
	{
		super.setup();
		
		pairs = new ArrayList<Pair>(graph.size());	
		degrees = new ArrayList<Integer>(graph.size());		
	}

	@Override
	protected void body()
	{
		super.body();
		
//		logger.info("Calculating size of the largest component");
//		WeakComponentClusterer<V, E> clust = 
//				new WeakComponentClusterer<V, E>();
//		
//		Set<Set<V>> clusters = clust.transform(graph);
//		
//		largestComponentSize = Double.NEGATIVE_INFINITY;
//		Set<V> largest = null;
//
//		for(Set<V> set : clusters)
//			if(set.size() > largestComponentSize)
//			{
//				largestComponentSize = set.size();
//				largest = set;
//			}
//		
//		largestComponent = Graphs.undirectedSubgraph(graph, largest);
//		
//		logger.info("Calculating diameter");
//		diameter = DistanceStatistics.diameter(graph);	
//		
//		logger.info("Calculating mean distance");
//		FloydWarshall<V, E> fw = new FloydWarshall<V, E>(largestComponent);
//		meanDistance = 0.0;
//		int num = 0;
//		for(V vi : graph.getVertices())
//			for(V vj : graph.getVertices())
//			{
//				meanDistance += fw.distance(vi, vj);
//				num++;
//			}
//		
//		meanDistance /= (double) num;
				
		// * Collect degrees 
		for(Node<N> node : graph.nodes())
			degrees.add(node.degree());
		
		Collections.sort(degrees, reverseOrder());
//		Collections.sort(pairs, reverseOrder());
//		
//		// * Collect vertex labels
//		BasicFrequencyModel<String> vertexModel = new BasicFrequencyModel<String>();
//		for(V vertex : graph.getVertices())
//			vertexModel.add(vertex.toString());
//		
//		List<String> tokens = vertexModel.sorted();
//		vertexLabelFrequencies = new ArrayList<Pair>(tokens.size());
//		for(String token : tokens)
//			vertexLabelFrequencies.add(new Pair((int)vertexModel.frequency(token), token));	
//		
//		// * Collect edge labels
//		BasicFrequencyModel<String> edgeModel = new BasicFrequencyModel<String>();
//		for(E edge : graph.getEdges())
//			edgeModel.add(edge.toString());
//		
//		tokens = edgeModel.sorted();
//		edgeLabelFrequencies = new ArrayList<Pair>(tokens.size());
//		for(String token : tokens)
//			edgeLabelFrequencies.add(new Pair((int)edgeModel.frequency(token), token));	
		
		List<Integer> degreesPL = new ArrayList<Integer>(graph.size());
		for(Node<N> node : graph.nodes())
			degreesPL.add(node.degree());
		
		Discrete dist = Discrete.fit(degreesPL).fit();
		plExponent = dist.exponent();
		plMin = dist.xMin();
		plSignificance = dist.significance(degreesPL, PL_ACCURACY);
	}
	
	@Result(name="diameter", description="Longest shortest path (in the largest component)")
	public double diameter()
	{
		return diameter;
	}
	  
	@Result(name="mean distance", description="Mean distance between nodes (in the largest component)")
	public double meanDistance()
	{
		return meanDistance;
	}
	
	@Result(name="Size of the largest component")
	public double lCompSize()
	{
		return largestComponentSize;
	}	
	
	@Result(name="Proportion of the largest component")
	public double lCompProp()
	{
		return largestComponentSize / (double) numNodes();
	}
	
	@Result(name="Vertex labels")
	public List<Pair> vertexLabels()
	{
		return Collections.unmodifiableList(vertexLabelFrequencies);
	}
	
	@Result(name="Edge labels")
	public List<Pair> edgeLabels()
	{
		return Collections.unmodifiableList(edgeLabelFrequencies);
	}	
	
	@Result(name="Degrees")
	public List<Integer> degrees()
	{
		return Collections.unmodifiableList(degrees);
	}
	
	@Result(name="Power law exponent")
	public double plExponent()
	{
		return plExponent;
	}
	
	@Result(name="Power law min")
	public int plMin()
	{
		return plMin;
	}
	
	@Result(name="Power law significance")
	public double plSignificance()
	{
		return plSignificance;
	}
	
	private class Pair extends AbstractList<Object> implements Comparable<Pair>
	{
		int degree;
		String label;
		
		public Pair(int degree, String node)
		{
			this.degree = degree;
			this.label = node;
		}

		public int compareTo(Pair that)
		{
			return Double.compare(this.degree, that.degree);
		}
		
		public String toString()
		{
			return degree + " " + label;
		}

		@Override
		public Object get(int index)
		{
			if(index == 0)
				return degree;
			if(index == 1)
				return label;
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int size()
		{
			return 2;
		}
	}
	
}
