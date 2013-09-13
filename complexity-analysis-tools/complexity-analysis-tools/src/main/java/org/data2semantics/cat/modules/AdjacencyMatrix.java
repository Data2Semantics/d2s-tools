package org.data2semantics.cat.modules;

import static org.lilian.util.Series.series;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.lilian.graphs.Graph;
import org.lilian.graphs.algorithms.SlashBurn;
import org.lilian.graphs.draw.Draw;

@Module(name="adjacency", description="Visualizes a graph as a density plot of the adjacency matrix")
public class AdjacencyMatrix<T>
{
	public static final int SIZE = 400;
	
	@In(name="data", print=false)
	public Graph<T> graph;
	
	@Out(name="given ordering") 
	public BufferedImage given;
	@Out(name="random ordering") 
	public BufferedImage random;
	@Out(name="degree ordering")
	public BufferedImage degree;
	@Out(name="slashburn ordering")
	public BufferedImage slashburn;

	@Out(name="Wing width ratio", description="Wing width ratio of the SlashBurn plot.")
	public double wingWidthRatio;
	
	@Main()
	public void main()
	{
		Logger logger = Global.log();
		List<Integer> order;
		
		logger.info("Starting adjacency natural.");
		given = Draw.matrix(graph, SIZE, SIZE);
		logger.info("Finished adjacency natural.");

		logger.info("Starting adjacency degree.");
		order = Draw.degreeOrdering(graph);
		degree = Draw.matrix(graph, SIZE, SIZE, order);
		logger.info("Finished adjacency degree. ");

		logger.info("Starting adjacency random.");
		Collections.shuffle(order);	
		random = Draw.matrix(graph, SIZE, SIZE, order);
		logger.info("Finished adjacency random. ");		
		
		logger.info("Starting adjacency slashburn.");
		int k = (int)(0.005 * graph.size());
		k = Math.max(k, 1);

		SlashBurn<T> sb = new SlashBurn<T>(graph, k);
		sb.finish();		
		slashburn = Draw.matrix(graph, SIZE, SIZE, sb.order());
		order = sb.order();

		wingWidthRatio = sb.wingWidthRatio();
		logger.info("-- Slashburn finished in "+sb.iterations()+" iterations (k at "+k+")");
		logger.info("Finished adjacency slashburn. ");	
		
	}
}
