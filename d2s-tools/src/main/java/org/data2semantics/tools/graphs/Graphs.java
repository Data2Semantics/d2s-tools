package org.data2semantics.tools.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TransformAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedGraph;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Static utility functions for dealing with graphs. 
 * @author Peter
 *
 */
public class Graphs
{
	
	/**
	 * Reads a graph from a text file with a single line per edge. Each line 
	 * contains a sequence of integers, separated by whitespace. The first 
	 * integer is the node ID, the integers following it (if any) are the nodes
	 * it connects to.
	 * 
	 * Empty lines are allowed and line starting with a hash (#) are ignored.
	 * 
	 * @param tsFile
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static UndirectedGraph<Integer, Integer> singLine(File file)
			throws IOException
	{
		UndirectedSparseGraph<Integer, Integer> graph = 
				new UndirectedSparseGraph<Integer, Integer>();
		
		readSingleLine(graph, file);
			
		return graph;
	}
	
	/**
	 * Reads a graph from a basic tab-separated value file. The file can contain 
	 * comments on lines that start with a '#', and edges in the form of two tab 
	 * separated integers. Empty lines are ignored.
	 * @param tsFile
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static UndirectedGraph<Vertex<Integer>, Edge<Integer>> intGraphFromTSV(File file)
			throws IOException
	{
		UndirectedSparseGraph<Vertex<Integer>, Edge<Integer>> graph = 
			new UndirectedSparseGraph<Vertex<Integer>, Edge<Integer>>();
		
		read(graph, file);
		
		return graph;
	}
	
	private static void readSingleLine(Graph<Integer, Integer> graph, File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
				
		String line;
		int i = 0;
		int edges = 0;
				
		do
		{
			line = reader.readLine();
			i++;
			
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			
			String[] split = line.trim().split("\\s");
			//System.out.println("!!!!!!!!!!!" + Arrays.toString( split));
			
			Integer node = parse(split[0], i);
			graph.addVertex(node);
			
			for(int v = 1; v < split.length; v++)
				graph.addEdge(edges++, node, parse(split[v], i));
		} while(line != null);
		
		System.out.println("\nFinished. Read " + edges + "edges");
		System.out.println("Encountered "+graph.getVertexCount()+" vertices");
	}	
	
	private static int parse(String in, int line)
	{
		try {
			return Integer.parseInt(in);
		} catch(NumberFormatException e)
		{
			throw new RuntimeException("Could not parse '"+in+"' to integer (line "+line+")", e);
		}
	}
	
	private static void read(Graph<Vertex<Integer>, Edge<Integer>> graph, File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
				
		String line;
		int i = 0;
		int edges = 0;
		
		HashSet<Integer> set = new HashSet<Integer>();
		
		do
		{
			line = reader.readLine();
			i++;
			
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			Integer a, b, c = null;
			try {
				a = Integer.parseInt(split[0]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = Integer.parseInt(split[1]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}

			if(split.length > 2)
				try {
					c = Integer.parseInt(split[2]);
				} catch(NumberFormatException e)
				{
					throw new RuntimeException("The third element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
				}				
			
			edges++;		
			Vertex<Integer> 
				av = new Vertex<Integer>(a),
				bv = new Vertex<Integer>(b);
			
			Edge<Integer> edge = new Edge<Integer>(c == null ? edges : c);
			
			if(((long)edges) > Integer.MAX_VALUE)
				throw new IllegalStateException("Too many edges ("+edges+") to be represented as ints (from line "+i+", max = "+Integer.MAX_VALUE+")");
					
			set.add(a); set.add(b);
			
			graph.addEdge(new Edge<Integer>(edges), av, bv);
			
			if(edges % 100000 == 0)		
				System.out.print("\rRead " + edges + " edges");
		} while(line != null);
		
		System.out.println("\nFinished. Read " + edges + "edges");
		System.out.println("Encountered "+set.size()+" vertices");
		
		HashSet<Integer> all = new HashSet<Integer>();
		for(int j = 1; j < 307; j++)
			all.add(j);
		System.out.println(all.removeAll(set));
		System.out.println(all);
	}

	/**
	 * Reads a graph from a basic tab-separated value file. The file can contain 
	 * comments on lines that start with a '#', and edges in the form of two tab 
	 * separated integers (starting with the 'from' vertex and ending with the 
	 * 'to' vertex)
	 * 
	 * Empty lines are ignored.
	 * @param tsFile
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static DirectedGraph<Vertex<Integer>, Edge<Integer>> intDirectedGraphFromTSV(File file)
			throws IOException
	{
		DirectedSparseGraph<Vertex<Integer>, Edge<Integer>> graph = 
			new DirectedSparseGraph<Vertex<Integer>, Edge<Integer>>();
		
		read(graph, file);
		
		return graph;
	}	
	
	/**
	 * Reads a graph from a basic tab-separated value file. The file can contain 
	 * comments on lines that start with a '#', and edges in the form of two 
	 * whitespace separated labels. Empty lines are ignored.
	 * @param tsFile
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static UndirectedGraph<Vertex<String>, Edge<String>> graphFromTSV(File file)
			throws IOException
	{
		UndirectedSparseGraph<Vertex<String>, Edge<String>> graph = 
			new UndirectedSparseGraph<Vertex<String>, Edge<String>>();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String line;
		int i = 0;
		int edges = 0;
		
		do
		{
			line = reader.readLine();
			i++;			
			
			if(line == null)
				continue;			
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			String a, b;
				a = split[0];
				b = split[1];
			
			Vertex<String> 
				av = new Vertex<String>(a),
				bv = new Vertex<String>(b);
			
			graph.addEdge(new Edge<String>("" + edges++), Arrays.asList(av, bv));
			
			if(edges % 10000 == 0)		
				System.out.print("\rRead " + edges + " edges");
		} while (line != null);
		
		System.out.println("Finished. Read " + edges + "edges");
	
		return graph;
	}	
	
	/**
	 * Retrieves a graph from an rdf file, interpreting resources as nodes and 
	 * predicates as labeled edges. The property of RDF that resources can occur
	 * as predicates and vice versa is not reflected in the graph in any 
	 * structured way. 
	 * 
	 * The file format is assumed to be RDF XML
	 * @param rdfFile The file (in RDF XML) from which to read the graph.
	 * @return A directed multigraph with labeled nodes and edges
	 */
	public static DirectedGraph<Vertex<String>, Edge<String>> graphFromRDF(File rdfFile)
	{
		return graphFromRDF(rdfFile, RDFFormat.RDFXML);
	}
	
	/**
	 * Retrieves a graph from an rdf file
	 * @param rdfFile The file (in RDF XML) from which to read the graph.
	 * @param format
	 * @return A directed multigraph with labeled nodes and edges
	 */
	public static DirectedGraph<Vertex<String>, Edge<String>> graphFromRDF(File rdfFile, RDFFormat format)
	{
		return graphFromRDF(rdfFile, format, null, null);
	}
	
	/**
	 * Extracts a graph from an RDF file, using whitelists of regular expressions
	 * to determine which edges and nodes to return. 
	 * 
	 * @param rdfFile
	 * @param format
	 * @param vWhiteList A list of regular expressions. If a vertex label 
	 *    matches one or more, it is used in the graph.
	 * @param eWhiteList A list of regular expressions. If an edge label 
	 *    matches one or more, it is used in the graph.
	 * 
	 * @return
	 */
	public static DirectedGraph<Vertex<String>, Edge<String>> graphFromRDF(
			File rdfFile,
			RDFFormat format,
			List<String> vWhiteList,
			List<String> eWhiteList)
	{		
		RDFDataSet testSet = new RDFFileDataSet(rdfFile, format);

		//org.openrdf.model.Graph triples = testSet.getStatements(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples = testSet.getFullGraph();
			
		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = GraphFactory.createDirectedGraph(triples, vWhiteList, eWhiteList);
				
		return jungGraph;
	}
	
	/**
	 * Renders an image of a graph
	 * @param graph
	 * @return
	 */
	public static <V, E> BufferedImage image(Graph<V, E> graph, int width, int height)
	{
		// Create the VisualizationImageServer
		// vv is the VisualizationViewer containing my graph
		VisualizationImageServer<V, E> vis =
		    new VisualizationImageServer<V, E>(
		    		new ISOMLayout<V, E>(graph), 
		    		new Dimension(width, height));

		vis.setBackground(Color.WHITE);
//		vis.getRenderContext()
//			.setEdgeLabelTransformer(new ToStringLabeller<E>());
		vis.getRenderContext()
			.setEdgeShapeTransformer(new EdgeShape.Line<V, E>());
		vis.getRenderContext()
			.setEdgeDrawPaintTransformer(new Transformer<E, Paint>()
			{
				Color c = new Color(0.0f, 0.0f, 0.0f, 0.05f);
				public Paint transform(E input)
				{
					return c;
				}
			});
		vis.getRenderContext().setVertexFillPaintTransformer(new Transformer<V, Paint>()
		{
			Color c = new Color(0.0f, 0.0f, 1.0f, 0.5f);
			public Paint transform(V input)
			{
				return c;
			}
		});
		vis.getRenderContext().setVertexStrokeTransformer(new Transformer<V, Stroke>()
		{
			public Stroke transform(V input)
			{
				return new BasicStroke(0.0f);
			}
		});
		vis.getRenderContext().setVertexShapeTransformer(new Transformer<V, Shape>()
		{
			double r = 3.0;
			Shape e = new Ellipse2D.Double(0.0, 0.0, r, r);
			
			public Shape transform(V input)
			{
				return e;
			}
		});		
		
//		vis.getRenderContext()
//			.setVertexLabelTransformer(new ToStringLabeller<V>());
		vis.getRenderer().getVertexLabelRenderer()
		    .setPosition(Position.CNTR);

		// Create the buffered image
		BufferedImage image = (BufferedImage) vis.getImage(
		    new Point2D.Double(width/2, height/2),
		    new Dimension(width, height));
		
		return image;
	}
	/**
	 * Remove vertices and edges from graph that have labels on the resp. black list
	 * 
	 * @param graph the graph to remove vertices/edges from
	 * @param vBlackList list of regular expressions
	 * @param eBlackList list of regular expressions
	 */	
	public static void removeVerticesAndEdges(DirectedGraph<Vertex<String>, Edge<String>> graph, List<String> vBlackList, List<String> eBlackList) {
		
		List<Pattern> vertexBlackList = null;
		
		if(vBlackList != null) 
		{
			vertexBlackList = new ArrayList<Pattern>(vBlackList.size());
			for(String patternString : vBlackList)
				vertexBlackList.add(Pattern.compile(patternString));
		}
		
		List<Pattern> edgeBlackList = null;
		if(eBlackList != null)
		{
			edgeBlackList = new ArrayList<Pattern>(eBlackList.size());
			for(String patternString : eBlackList)
				edgeBlackList.add(Pattern.compile(patternString));
		}
		
		if (edgeBlackList != null) {
			List<Edge<String>> toRemove = new ArrayList<Edge<String>>();
						
			for (Edge<String> edge : graph.getEdges()) {		
				if(matches(edge.getLabel(), edgeBlackList)) {
					Vertex<String> v1 = graph.getSource(edge);
					Vertex<String> v2 = graph.getDest(edge);	
					toRemove.add(edge);					
					
					if (graph.degree(v1) == 0) {
						graph.removeVertex(v1);
					}
					if (graph.degree(v2) == 0) {
						graph.removeVertex(v2);
					}
				}						
			}
			
			for (Edge<String> edge : toRemove) {
				graph.removeEdge(edge);
			}		
		}
		
		if (vertexBlackList != null) {
			List<Vertex<String>> toRemove = new ArrayList<Vertex<String>>();
			
			for (Vertex<String> vertex : graph.getVertices()) {
				if(matches(vertex.getLabel(), vertexBlackList)) {
					toRemove.add(vertex);
				}
			}
			
			for (Vertex<String> vertex : toRemove) {
				graph.removeVertex(vertex);
			}
		}
		
	}
	
	/** TODO move this to a proper Utility class/package
	 * Returns true if the String matches one or more of the patterns in the list.
	 * @param string
	 * @param patterns
	 * @return
	 */
	public static boolean matches(String string, List<Pattern> patterns)
	{
		for(Pattern pattern : patterns)
			if(pattern.matcher(string).matches())
				return true;
		return false;
	}
	


}
