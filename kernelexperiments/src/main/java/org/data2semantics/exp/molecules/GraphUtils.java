package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.nodes.MapUTGraph;
import org.nodes.UGraph;
import org.nodes.UNode;

/**
 * Statics for dealing with Lilian graphs
 * 
 * @author Gerben
 *
 */
public class GraphUtils {

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
