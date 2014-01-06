package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;
import org.nodes.MapUTGraph;
import org.nodes.UGraph;
import org.nodes.ULink;
import org.nodes.UNode;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Statics for dealing with Lilian graphs
 * 
 * @author Gerben
 *
 */
public class GraphUtils {
	public static final String NAMESPACE = "http://www.data2semantics.org/molecule/";


	public static List<Statement> moleculeGraph2RDF(UGraph<String> graph, String moleculeID, boolean blankRoot) {
		List<Statement> moleculeRDF = new ArrayList<Statement>();
		ValueFactory factory = ValueFactoryImpl.getInstance();
		Map<Integer, BNode> nodeMap = new HashMap<Integer,BNode>();
		URI moleculeURI;
		if (blankRoot) {
			moleculeURI = factory.createURI(NAMESPACE, "moleculeInstance");
		} else {
			moleculeURI = factory.createURI(NAMESPACE, moleculeID);
		}
		URI partOfURI = factory.createURI(NAMESPACE, "partOf");
		URI hasAtomURI = factory.createURI(NAMESPACE, "hasAtom");	
		URI bondURI = factory.createURI(NAMESPACE, "hasBond");
		moleculeRDF.add(factory.createStatement(moleculeURI, RDF.TYPE, factory.createURI(NAMESPACE, "Molecule")));

		for (UNode<String> node : graph.nodes()) {
			BNode bn = factory.createBNode(moleculeID + "_" + node.index());
			nodeMap.put(node.index(), bn);
			moleculeRDF.add(factory.createStatement(moleculeURI, hasAtomURI, bn));
			moleculeRDF.add(factory.createStatement(bn, partOfURI, moleculeURI));
			moleculeRDF.add(factory.createStatement(bn, RDF.TYPE, factory.createURI(NAMESPACE + "atom/", node.label())));
		}
		for (ULink<String> link : graph.links()) {
			moleculeRDF.add(factory.createStatement(nodeMap.get(link.first().index()), bondURI, nodeMap.get(link.second().index())));
			moleculeRDF.add(factory.createStatement(nodeMap.get(link.second().index()), bondURI, nodeMap.get(link.first().index())));
		}
		return moleculeRDF;
	}


	public static DTGraph<String,String> removeRDFType(DTGraph<String,String> graph, List<String> priorities) {
		DTGraph<String,String> ng = new MapDTGraph<String,String>();
		Map<DTNode<String,String>,DTNode<String,String>> rnm = new HashMap<DTNode<String,String>,DTNode<String,String>>();
		Set<DTLink<String,String>> rls = new HashSet<DTLink<String,String>>();
		priorities.add("");
		
		for (String prio : priorities) {
			for (DTLink<String,String> link : graph.links()) {
				DTNode<String,String> n1 = link.from();
				DTNode<String,String> n2 = link.to();
				if (link.tag().equals(RDF.TYPE.stringValue()) && (prio.equals("") || n2.label().equals(prio))) { // link is an rdf:type relation and has the right class label in n2
					if (!rnm.containsKey(n1)) {
						DTNode<String, String> nn = ng.add(n2.label());
						rnm.put(n1, nn);
						rls.add(link);
					}
				}
			}
		}
		for (DTNode<String,String> node : graph.nodes()) { // add all the remaining nodes
			if (!rnm.containsKey(node)) {
				DTNode<String, String> nn = ng.add(node.label());
				rnm.put(node, nn);
			}
		}
		for (DTLink<String, String> link : graph.links()) {
			if (!rls.contains(link)) { // It should not be a link that we want to remove, for the rest, we copy the links
				rnm.get(link.from()).connect(rnm.get(link.to()), link.tag());
			}
		}
		List<DTNode<String,String>> temp = new ArrayList<DTNode<String,String>>(ng.nodes());
		for (DTNode<String,String> node : temp) { // remove unconnected nodes
			if (node.degree() == 0) {
				node.remove();
			}
		}
		return ng;
	}


	public static UGraph<String> readMoleculeGraph(String fileName) {
		UGraph<String> graph = new MapUTGraph<String,Object>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();
			if (line.startsWith("#")) { // skip line starting with #
				line = reader.readLine();
			}

			// Read till next #
			while (!line.startsWith("#")) {
				graph.add(line);
				line = reader.readLine();
			}

			if (line.startsWith("#a")) { // adjacency list
				line = reader.readLine();
				int nodeID = 0;
				while (!line.startsWith("#")) {

					String[] nodes = line.split(","); // get neighboring nodes
					for (String node : nodes) {
						if (!node.equals("") && !graph.nodes().get(nodeID).connected(graph.nodes().get(Integer.parseInt(node)-1))) { // no existing connection, NOTE, in the file, indices start at 1
							graph.nodes().get(nodeID).connect(graph.nodes().get(Integer.parseInt(node)-1)); //connect
						}
					}
					line = reader.readLine();
					nodeID++;
				}			

			} else if (line.startsWith("#e") ) { // edge list (with label)
				line = reader.readLine();

				while (!line.startsWith("#")) {
					String[] nodes = line.split(","); // nodes + label
					if (!graph.nodes().get(Integer.parseInt(nodes[0])-1).connected(graph.nodes().get(Integer.parseInt(nodes[1])-1))) { // no existing connection
						graph.nodes().get(Integer.parseInt(nodes[0])-1).connect(graph.nodes().get(Integer.parseInt(nodes[1])-1)); //connect
					}
					line = reader.readLine();
				}
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}

}
