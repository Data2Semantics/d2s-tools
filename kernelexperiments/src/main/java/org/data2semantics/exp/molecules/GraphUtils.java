package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.DegreeComparator;
import org.nodes.MapDTGraph;
import org.nodes.MapUTGraph;
import org.nodes.Node;
import org.nodes.UGraph;
import org.nodes.ULink;
import org.nodes.UNode;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.Functions.Dir;
import org.nodes.util.MaxObserver;
import org.nodes.util.Pair;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Statics for dealing with nodes graphs & RDF
 * 
 * @author Gerben
 *
 */
public class GraphUtils {
	public static final String NAMESPACE = "http://www.data2semantics.org/molecule/";

	public static List<Statement> getStatements4Depth(RDFDataSet ts, List<Resource> instances, int depth, boolean inference) {
		Set<Statement> stmts = new HashSet<Statement>();
		List<Resource> searchFront = new ArrayList<Resource>(instances);
		List<Resource> newSearchFront;

		for (int i = 0; i < depth; i++) {
			newSearchFront = new ArrayList<Resource>();
			for (Resource r : searchFront) {
				List<Statement> res = ts.getStatements(r, null, null, inference);
				stmts.addAll(res);
				for (Statement stmt : res) {
					if (stmt.getObject() instanceof Resource) {
						newSearchFront.add((Resource) stmt.getObject()); 
					}
				}
			}
			searchFront = newSearchFront;
		}
		return new ArrayList<Statement>(stmts);
	}


	public static List<DTGraph<String,String>> getSubGraphs(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<String,String>> subGraphs = new ArrayList<DTGraph<String,String>>();
		Map<DTNode<String,String>,DTNode<String,String>> nodeMap;
		Map<DTLink<String,String>,DTLink<String,String>> linkMap;
		List<DTNode<String,String>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<String,String> newGraph = new MapDTGraph<String,String>();
			searchNodes = new ArrayList<DTNode<String,String>>();
			searchNodes.add(startNode);
			nodeMap = new HashMap<DTNode<String,String>,DTNode<String,String>>();
			linkMap = new HashMap<DTLink<String,String>,DTLink<String,String>>();
			for (int i = 0; i < depth; i++) {
				newSearchNodes = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> node : searchNodes) {
					for (DTLink<String,String> link : node.linksOut()) {
						if (!nodeMap.containsKey(link.from())) {
							nodeMap.put(link.from(), newGraph.add(link.from().label()));
						}
						if (!nodeMap.containsKey(link.to())) {
							nodeMap.put(link.to(), newGraph.add(link.to().label()));
							newSearchNodes.add(link.to());
						}
						if (!linkMap.containsKey(link)) {
							linkMap.put(link, nodeMap.get(link.from()).connect(nodeMap.get(link.to()), link.tag()));
						}
					}
				}
				searchNodes = newSearchNodes;
			}
			subGraphs.add(newGraph);
		}
		return subGraphs;
	}

	public static List<DTGraph<String,String>> simplifyGraph3Way(DTGraph<String,String> graph, Map<String,Integer> hubMap, List<DTNode<String,String>> instanceNodes, List<List<DTNode<String,String>>> newInstanceNodes) {
		DTGraph<String,String> newGraphLi = new MapDTGraph<String,String>();
		DTGraph<String,String> newGraphLa = new MapDTGraph<String,String>();
		DTGraph<String,String> newGraphLiLa = new MapDTGraph<String,String>();
		List<DTNode<String,String>> ninLi = new ArrayList<DTNode<String,String>>(instanceNodes.size());
		List<DTNode<String,String>> ninLa = new ArrayList<DTNode<String,String>>(instanceNodes.size());
		List<DTNode<String,String>> ninLiLa = new ArrayList<DTNode<String,String>>(instanceNodes.size());
		newInstanceNodes.add(ninLi);
		newInstanceNodes.add(ninLa);
		newInstanceNodes.add(ninLiLa);
		for (int i = 0; i < instanceNodes.size(); i++) {
			ninLi.add(instanceNodes.get(i));
			ninLa.add(instanceNodes.get(i));
			ninLiLa.add(instanceNodes.get(i));
		}

		Set<DTLink<String,String>> toRemoveLinks = new HashSet<DTLink<String,String>>();

		Map<DTNode<String,String>,Integer> iNodeMap = new HashMap<DTNode<String,String>,Integer>();	
		for (int i = 0; i < instanceNodes.size(); i++) {
			iNodeMap.put(instanceNodes.get(i), i);
		}	

		for (DTNode<String,String> node : graph.nodes()) {
			String newLabel = null;
			int lowestDepth = 0;
			DTLink<String,String> remLink = null;;
			for (DTLink<String,String> inLink : node.linksIn()) {
				String rel = inLink.from().label() + inLink.tag();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = inLink;
				}
			}
			for (DTLink<String,String> outLink : node.linksOut()) {
				String rel = outLink.tag() + outLink.to().label();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = outLink;
				}
			}
			if (newLabel == null) {
				newLabel = node.label();
			} 
			DTNode<String,String> newLi = newGraphLi.add(node.label());
			DTNode<String,String> newLa = newGraphLa.add(newLabel);
			DTNode<String,String> newLiLa = newGraphLiLa.add(newLabel);

			if (iNodeMap.containsKey(node)) { // We also need to replace the instance nodes with new instance nodes in the simplified graph
				ninLi.set(iNodeMap.get(node), newLi);
				ninLa.set(iNodeMap.get(node), newLa);
				ninLiLa.set(iNodeMap.get(node), newLiLa);
			}

			if (remLink != null) {
				toRemoveLinks.add(remLink);
			}
		}

		for(DTLink<String,String> link : graph.links()) {
			int a = link.from().index();
			int b = link.to().index();

			if (!toRemoveLinks.contains(link)) {
				newGraphLi.nodes().get(a).connect(newGraphLi.nodes().get(b), link.tag());
				newGraphLiLa.nodes().get(a).connect(newGraphLiLa.nodes().get(b), link.tag());
			}
			newGraphLa.nodes().get(a).connect(newGraphLa.nodes().get(b), link.tag());
		}
		List<DTGraph<String,String>> ret = new ArrayList<DTGraph<String,String>>();
		ret.add(newGraphLi);
		ret.add(newGraphLa);
		ret.add(newGraphLiLa);
		return ret;
	}



	public static DTGraph<String,String> simplifyGraph(DTGraph<String,String> graph, Map<String,Integer> hubMap, List<DTNode<String,String>> instanceNodes, boolean relabel, boolean removeLinks) {
		DTGraph<String,String> newGraph = new MapDTGraph<String,String>();
		Set<DTLink<String,String>> toRemoveLinks = new HashSet<DTLink<String,String>>();

		Map<DTNode<String,String>,Integer> iNodeMap = new HashMap<DTNode<String,String>,Integer>();	
		for (int i = 0; i < instanceNodes.size(); i++) {
			iNodeMap.put(instanceNodes.get(i), i);
		}	

		for (DTNode<String,String> node : graph.nodes()) {
			String newLabel = null;
			int lowestDepth = 0;
			DTLink<String,String> remLink = null;;
			for (DTLink<String,String> inLink : node.linksIn()) {
				String rel = inLink.from().label() + inLink.tag();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = inLink;
				}
			}
			for (DTLink<String,String> outLink : node.linksOut()) {
				String rel = outLink.tag() + outLink.to().label();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = outLink;
				}
			}
			if (newLabel == null) {
				newLabel = node.label();
			} else if (!relabel) {
				newLabel = node.label();
			}
			DTNode<String,String> newN = newGraph.add(newLabel);
			if (iNodeMap.containsKey(node)) { // We also need to replace the instance nodes with new instance nodes in the simplified graph
				instanceNodes.set(iNodeMap.get(node), newN);
			}

			if (remLink != null && removeLinks) {
				toRemoveLinks.add(remLink);
			}
		}

		for(DTLink<String,String> link : graph.links()) {
			int a = link.from().index();
			int b = link.to().index();

			if (!toRemoveLinks.contains(link)) {
				newGraph.nodes().get(a).connect(newGraph.nodes().get(b), link.tag());
			}
		}
		return newGraph;
	}

	public static List<DTNode<String,String>> getTypeHubs(DTGraph<String,String> graph) {
		List<DTNode<String,String>> typeNodes = new ArrayList<DTNode<String,String>>();
		Map<DTNode<String,String>, Integer> countMap = new HashMap<DTNode<String,String>,Integer>();

		for (DTLink<String,String> link : graph.links()) {
			if (link.tag().equals(RDF.TYPE.toString())) {
				if (!countMap.containsKey(link.to())) {
					countMap.put(link.to(), 0);
					typeNodes.add(link.to());
				} else {
					countMap.put(link.to(), countMap.get(link.to())+1);
				}
			}
		}
		Collections.sort(typeNodes, new TypeNodeComparator(countMap));
		Collections.reverse(typeNodes);
		return typeNodes;
	}

	static class TypeNodeComparator implements Comparator<DTNode<String,String>> {
		private Map<DTNode<String,String>,Integer> cm;

		public TypeNodeComparator(Map<DTNode<String,String>,Integer> countMap) {
			cm = countMap;		
		}
		public int compare(DTNode<String, String> o1, DTNode<String, String> o2) {
			return cm.get(o1) - cm.get(o2);
		}	
	}

	public static Map<String,Integer> createRDFTypeHubMap(RDFDataSet ts, boolean inference) {
		Map<String,Integer> hubMap = new HashMap<String,Integer>();
		List<Statement> types = ts.getStatements(null, RDF.TYPE, null, inference);
		Map<Resource, Integer> classCounts = new HashMap<Resource, Integer>();

		// Count different classes
		for (Statement s : types) {
			Resource c = (Resource) s.getObject();
			classCounts.put(c, (classCounts.containsKey(c)) ? classCounts.get(c) + 1 : 0);
		}

		// Get the largest class size
		List<Integer> counts = new ArrayList<Integer>(classCounts.values());
		Collections.sort(counts);
		int max = counts.get(counts.size()-1);

		// Create the hubmap, with largest hubs having the lowest numbers
		for (Resource c : classCounts.keySet()) {
			hubMap.put(RDF.TYPE.toString() + c.toString(), max - classCounts.get(c));
		}
		return hubMap;
	}

	public static Map<String,Integer> createNonSigHubMap(List<DTNode<String,String>> hubs, int th) {
		Map<String,Integer> hubMap = new HashMap<String,Integer>();

		for (int i = 0; i < hubs.size() && i < th; i++) {
			for (DTLink<String,String> e : hubs.get(i).linksIn()) {
				hubMap.put(e.tag() + hubs.get(i).label(), i);
			}
			for (DTLink<String,String> e : hubs.get(i).linksOut()) {
				hubMap.put(hubs.get(i).label() + e.tag(), i);
			}			
		}
		System.out.println("Total hubs: " + hubMap.size());
		return hubMap;
	}

	public static Map<String,Integer> createHubMap(List<DTNode<String,String>> hubs, int th, boolean allLinks) {
		if (allLinks) {
			return createNonSigHubMap(hubs, th);
		} else {
			return createHubMap(hubs, th);
		}
	}

	public static Map<String,Integer> createHubMap(List<DTNode<String,String>> hubs, int th) {
		Map<String,Integer> hubMap = new HashMap<String,Integer>();		
		for (int i = 0; i < hubs.size() && i < th; i++) {
			org.nodes.util.Pair<Dir,String> sig = SlashBurn.primeSignature(hubs.get(i));

			if (sig.first() == Dir.IN) {
				hubMap.put(sig.second() + hubs.get(i).label(), i);	
				//System.out.println("Removing: " + sig.second() + " -> " + hubs.get(i).label());
			} else {
				hubMap.put(hubs.get(i).label() + sig.second(), i);
				//ystem.out.println("Removing: " + hubs.get(i).label() + " -> " + sig.second());
			}	
		}
		System.out.println("Total hubs: " + hubMap.size());
		return hubMap;
	}


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
