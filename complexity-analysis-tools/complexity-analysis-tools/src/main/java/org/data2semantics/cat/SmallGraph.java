package org.data2semantics.cat;

import java.awt.image.BufferedImage;

import org.lilian.experiment.Result;
import org.lilian.experiment.State;
import org.lilian.graphs.ConnectionClustering;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Measures;
import org.lilian.graphs.Subgraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.algorithms.FloydWarshall;
import org.lilian.graphs.jung.Graphs;


public class SmallGraph<N> extends LargeGraph<N>
{
	public @State double globalClusteringCoefficient;
	
	public @State double diameter = Double.NaN;	
	public @State double largestComponentSize = Double.NaN;
	public @State double meanDistance = Double.NaN;

	public SmallGraph(Graph<N> graph)
	{
		super(graph);
	}

	@Override
	protected void setup()
	{
		super.setup();
	}

	@Override
	protected void body()
	{
		super.body();
			
		logger.info("Calculating global Clustering Coefficient");
		globalClusteringCoefficient = Measures.clusteringCoefficient(graph);
		
		ConnectionClustering<N> cc = new ConnectionClustering<N>(graph);
		
		Graph<N> largestComponent = Subgraph.subgraphIndices(graph, cc.largestCluster());
		largestComponentSize = largestComponent.size();

		FloydWarshall<N> fw = new FloydWarshall<N>(largestComponent);
		logger.info("Calculating diameter");
		diameter = fw.diameter();
		
		logger.info("Calculating mean distance");
		meanDistance = fw.meanDistance();
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

	@Result(name="Global clustering coefficient")
	public double globalClusteringCoefficient()
	{
		return globalClusteringCoefficient;
	}
	
}
