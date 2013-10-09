package org.data2semantics.cat.modules;

import java.util.logging.Logger;

import org.data2semantics.cat.SmallGraph;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.lilian.graphs.Graph;

@Module(name="Large Graph measures")
public class SmallGraphModule <N> extends SmallGraph<N>{

	
	public SmallGraphModule(@In(name="data")  Graph<N> graph) {
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

	
	@Out(name="diameter", description="Longest shortest path (in the largest component)")
	public double diameter()
	{
		return diameter;
	}
	  
	@Out(name="mean distance", description="Mean distance between nodes (in the largest component)")
	public double meanDistance()
	{
		return meanDistance;
	}
	
	@Out(name="Size of the largest component")
	public double lCompSize()
	{
		return largestComponentSize;
	}	
	
	@Out(name="Proportion of the largest component")
	public double lCompProp()
	{
		return largestComponentSize / (double) numNodes();
	}

	@Out(name="Global clustering coefficient")
	public double globalClusteringCoefficient()
	{
		return globalClusteringCoefficient;
	}
	
}
