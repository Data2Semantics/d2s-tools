package org.data2semantics.cat;

import java.awt.image.BufferedImage;

import org.lilian.experiment.Result;
import org.lilian.experiment.State;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Measures;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.jung.Graphs;


public class SmallGraph<N> extends LargeGraph<N>
{
	public @State BufferedImage image;
	public @State double globalClusteringCoefficient;

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
		
		// * Rendering visualization
		logger.info("Rendering visualization.");
		if(graph instanceof UTGraph)
		{
			image = Graphs.image(
				Graphs.toJUNG((UTGraph<?,?>)graph), 800, 494);	
		}
		
		logger.info("Calculating global Clustering Coefficient");
		globalClusteringCoefficient = Measures.clusteringCoefficient(graph);
	}	
	
	@Result(name="Visualization")
	public BufferedImage visualization()
	{
		return image;
	}
	
	@Result(name="Global clustering coefficient")
	public double globalClusteringCoefficient()
	{
		return globalClusteringCoefficient;
	}
	
}
