package org.data2semantics.cat;

import java.util.ArrayList;
import java.util.List;

import org.lilian.experiment.AbstractExperiment;
import org.lilian.experiment.BasicResults;
import org.lilian.experiment.Environment;
import org.lilian.experiment.Experiment;
import org.lilian.experiment.Parameter;
import org.lilian.experiment.Result;
import org.lilian.experiment.Results;
import org.lilian.graphs.DGraph;
import org.lilian.graphs.Graph;


public class GraphMeasures<N> extends AbstractExperiment
{
	protected Graph<N> graph;
	protected String size;
	
	private boolean directed;
	public List<Experiment> experiments = new ArrayList<Experiment>();
	
	public GraphMeasures(
			@Parameter(name="data") Graph<N> graph)
	{
		this(
			graph,
			graph.size() < 5000 ? "small" : 
				graph.size() < 100000 ? "large" : "huge");
	}
	
	public GraphMeasures(
			@Parameter(name="data") Graph<N> graph, 
			@Parameter(name="size") String size)
	{
		this.graph = graph;
		this.size = size;
	}

	@Override
	protected void setup()
	{
		directed = graph instanceof DGraph<?>;
		
		if(size.equals("huge"))
			experiments.add(new HugeGraph<N>(graph));
		else if(size.equals("large"))
			experiments.add(new LargeGraph<N>(graph));
		else if(size.equals("small"))
			experiments.add(new SmallGraph<N>(graph));
		else
			throw new RuntimeException("Size parameter ("+size+") not understood.");
		
//		if(directed)
//		{
//			if(size.equals("huge"))
//				experiments.add(new HugeDirectedGraph<V, E>(graph));
//			else if(size.equals("large"))
//				experiments.add(new LargeDirectedGraph<V, E>(graph));
//			else if(size.equals("small"))
//				experiments.add(new SmallDirectedGraph<V, E>(graph));
//			else
//				throw new RuntimeException("Size parameter ("+size+") not understood.");
//		}
	}

	@Override
	protected void body()
	{
		for(Experiment experiment : experiments)
			Environment.current().child(experiment);
	}
	
	@Result(name="directed")
	public boolean directed()
	{
		return directed;
	}
	
	@Result(name="result")
	public Results allResults()
	{
		BasicResults results = new BasicResults();
		for(Experiment experiment : experiments)
			results.addAll(experiment);
		return results;
	}
}