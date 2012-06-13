package org.data2semantics.tools.graphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class Graphs
{
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
			
		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = GraphFactory.createJUNGGraph(triples, vWhiteList, eWhiteList);
				
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
		    new VisualizationImageServer<V, E>(new CircleLayout<V, E>(graph), new Dimension(width, height));

		// Configure the VisualizationImageServer the same way
		// you did your VisualizationViewer. In my case e.g.

		vis.setBackground(Color.WHITE);
		vis.getRenderContext()
		.setEdgeLabelTransformer(new ToStringLabeller<E>());
		vis.getRenderContext()
			.setEdgeShapeTransformer(new EdgeShape.Line<V, E>());
		vis.getRenderContext()
			.setVertexLabelTransformer(new ToStringLabeller<V>());
		vis.getRenderer().getVertexLabelRenderer()
		    .setPosition(Position.CNTR);

		// Create the buffered image
		BufferedImage image = (BufferedImage) vis.getImage(
		    new Point2D.Double(width/2, height/2),
		    new Dimension(width, height));
		
		return image;
	}
}
