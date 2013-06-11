package org.data2semantics.cat;

import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lilian.experiment.AbstractExperiment;
import org.lilian.experiment.Parameter;
import org.lilian.experiment.Result;
import org.lilian.graphs.Graph;
import org.lilian.graphs.boxing.Boxing;
import org.lilian.graphs.boxing.BoxingAlgorithm;
import org.lilian.graphs.boxing.CBBBoxer;
import org.lilian.graphs.boxing.GreedyBoxer;
import org.lilian.util.Series;


public class GraphDimension<L> extends AbstractExperiment
{
	
	private Graph<L> graph;
	private int minLB;
	private int maxLB;
	private String boxerType;
	
	public double slope;
	public List<Double> logLB = new ArrayList<Double>();
	public List<Double> logNB = new ArrayList<Double>();
	
	public List<Double> lbs = new ArrayList<Double>();
	public List<Double> nbs = new ArrayList<Double>();
	
	public List<Double> boxes = new ArrayList<Double>();
	
	public GraphDimension(
			@Parameter(name="data") Graph<L> graph, 
			@Parameter(name="min lb") int minLB, 
			@Parameter(name="max lb") int maxLB,
			@Parameter(name="boxer", description="One of: cbb, greedy") String boxer)
	{

		this.graph = graph;
		this.minLB = minLB;
		this.maxLB = maxLB;
		this.boxerType = boxer;
		
	}

	@Override
	protected void setup()
	{
	}

	@Override
	protected void body()
	{	
//		GraphMeasures<V, E> gm = new GraphMeasures<V, E>(graph);
//		Environment.current().child(gm);
		
		BoxingAlgorithm<L> boxer = null;
		
		if(boxerType.trim().toLowerCase().equals("cbb"))
			boxer = new CBBBoxer<L>(graph);
		else if(boxerType.trim().toLowerCase().equals("greedy"))
			boxer = new GreedyBoxer<L>(graph, maxLB);
		else
			throw new RuntimeException("Boxer type not recognized ("+boxerType+")");
		
		for(int lb : Series.series(minLB, maxLB + 1))
		{
			Boxing<L> boxing =  boxer.box(lb);
			
			int numBoxes = boxing.size();
			double propBoxes = numBoxes/(double)graph.size();
			double meanMass = graph.size()/(double)numBoxes;
			
			double nb = propBoxes;
			
			lbs.add((double)lb);
			nbs.add(nb);		
			
			logLB.add(log(lb));
			logNB.add(log(nb));
			
			boxes.add((double)numBoxes);
			
			logger.info("Finished lb = " + lb);
		}
		
		slope = slope(logLB, logNB);
	}

	@Result(name="slope")
	public double slope()
	{
		return slope;
	}
	
	@Result(name="log values")
	public List<List<Double>> logValues()
	{
		ArrayList<List<Double>> values = new ArrayList<List<Double>>();
		for(int i : Series.series(logLB.size()))
			values.add(Arrays.asList(logLB.get(i), logNB.get(i)));
		
		return values;
	}
	
	@Result(name="values")
	public List<List<Double>> values()
	{
		ArrayList<List<Double>> values = new ArrayList<List<Double>>();
		for(int i : Series.series(lbs.size()))
			values.add(Arrays.asList(lbs.get(i), nbs.get(i)));
		
		return values;
	}
	
	@Result(name="num boxes")
	public List<List<Double>> numBoxes()
	{
		ArrayList<List<Double>> values = new ArrayList<List<Double>>();
		for(int i : Series.series(lbs.size()))
			values.add(Arrays.asList(lbs.get(i), boxes.get(i)));
		
		return values;
	}	
	
	public static double slope(List<Double> xs, List<Double> ys)
	{
		// * Averages
		double xx = 0.0, yy = 0.0, xy = 0.0, x = 0.0, y = 0.0;
		int n = xs.size();
		for( int i : Series.series(n))
		{
			double cx = xs.get(i);
			double cy = ys.get(i);
			
			x += cx;
			y += cy;
			xx += cx * cx;
			xy += cx * cy;
			yy += cy * cy;
		}
		
		x /= (double)n;
		y /= (double)n;
		xx /= (double)n;
		xy /= (double)n;
		yy /= (double)n;
		
		return (xy - x*y)/(xx - x*x);
	}
	
}
