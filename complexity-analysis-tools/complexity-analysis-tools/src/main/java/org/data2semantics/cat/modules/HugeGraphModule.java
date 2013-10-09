package org.data2semantics.cat.modules;

import java.util.logging.Logger;

import org.data2semantics.cat.HugeGraph;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;
import org.lilian.graphs.Graph;

public class HugeGraphModule <N> extends HugeGraph<N>{

	public HugeGraphModule(@In(name="data")  Graph<N> graph) {
		super(graph);
		this.logger = Logger.getLogger(this.getClass().toString());
		
	}

	@Main
	@Override
	public void body()
	{

		super.setup();
		super.body();
	}
	@Out(name="Mean degree")
	public double meanDegree()
	{
		return meanDegree;
	}	
	
	@Out(name="Degree (sample) standard deviation")
	public double stdDegree()
	{
		return stdDegree;
	}
	
	@Out(name="Number of nodes (vertices)")
	public int numNodes()
	{
		return graph.size();
	}
	
	@Out(name="Number of links (edges)")
	public int numLinks()
	{
		return graph.numLinks();
	}
	
	@Out(name="Assortivity")
	public double assortivity()
	{
		return assortativity;
	}
	
	@Out(name="Label entropy")
	public double labelEntropy()
	{
		return labelEntropy;
	}
	
	@Out(name="Tag entropy")
	public double tagEntropy()
	{
		return tagEntropy;
	}
	
	@Out(name="Mean local clustering coefficient")
	public double meanLocalClusteringCoefficient()
	{
		return meanLocalClusteringCoefficient;
	}	
}

