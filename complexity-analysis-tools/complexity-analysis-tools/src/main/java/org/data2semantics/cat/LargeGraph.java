package org.data2semantics.cat;

import static java.util.Collections.reverseOrder;

import java.awt.image.BufferedImage;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import org.apache.commons.collections15.Transformer;
import org.lilian.experiment.Result;
import org.lilian.experiment.State;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.graphs.Subgraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.algorithms.FloydWarshall;
import org.lilian.graphs.jung.Graphs;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;

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
	private static final double PL_ACCURACY = 0.2;
	private static final int PL_DATASAMPLE = 1000;
	
	public @State BufferedImage image;
	public @State List<Pair> pairs;  
	public @State List<Integer> degrees;

	public @State Graph<N> largestComponent;

	protected double plExponent;
	protected int plMin;
	protected double plSignificance;

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
				
		// * Rendering visualization
		logger.info("Rendering visualization.");
		if(graph instanceof UTGraph)
		{
			image = Graphs.image(
				Graphs.toJUNG((UTGraph<?,?>)graph), 500, 300);	
		} else if(graph instanceof DTGraph)
		{
			image = Graphs.image(
				Graphs.toJUNG((DTGraph<?,?>)graph), 500, 300);	
		}
		logger.info("Visualization done.");
		
		
		// * Collect degrees 
		for(Node<N> node : graph.nodes())
			degrees.add(node.degree());
		
		Collections.sort(degrees, reverseOrder());

		List<Integer> degreesPL = new ArrayList<Integer>(graph.size());
		for(Node<N> node : graph.nodes())
			degreesPL.add(node.degree());
		
		Discrete dist = Discrete.fit(degreesPL).fit();
		plExponent = dist.exponent();
		plMin = dist.xMin();
		// * TODO: This is too slow to include now, but it is an important
		//         statistic.
		// plSignificance = dist.significance(degreesPL, PL_ACCURACY);
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
	
	@Result(name="Visualization")
	public BufferedImage visualization()
	{
		return image;
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
