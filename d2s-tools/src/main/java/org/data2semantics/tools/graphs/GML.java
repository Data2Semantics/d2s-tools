package org.data2semantics.tools.graphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * Methods for reading graph in GML format.
 * 
 * 
 * Works only if all elements (brackets, etc) are separated by whitespace.
 *
 */
public class GML
{
	public static Graph<LVertex, Edge<String>> read(File file)
		throws IOException
	{
		Reader reader = new Reader(file);

		return reader.graph();
	}
	
	private static class Reader
	{
		public static final int BUFFER_SIZE = 10;
		public Pattern tokenizer = Pattern.compile("\"([^\"]*)\"|(\\S+)");
		
		private BufferedReader reader;
		private LinkedList<String> buffer = new LinkedList<String>();
		
		private Graph<LVertex, Edge<String>> graph;
		
		public Reader(File file) throws IOException
		{
			reader = new BufferedReader(new FileReader(file));
			graph = new UndirectedSparseGraph<GML.LVertex, Edge<String>>();
			
			start();
		}
		
		public Graph<LVertex, Edge<String>> graph()
		{
			return graph;
		}
		
		private void read()
		{
			if(buffer.size() > 10)
				return;
			// * Read until whitespace 
			StringBuffer bf = new StringBuffer();
			
			int cr = -1;
			boolean inQuotes = false;
			
			do {
				try
				{
					cr = reader.read();
				} catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				if(cr >= 0)
				{
					bf.append((char)cr);
					
					if((char)cr == '"')
						inQuotes = ! inQuotes;
				}
				
				if(inQuotes && cr == -1)
					throw new IllegalStateException("Stream ended inside quotes.");
				
			} while (
					inQuotes ||
					(
						cr != -1 && 
						(! Character.isWhitespace(bf.charAt(bf.length()-1)) 
									|| bf.length() < BUFFER_SIZE)
					));
			
			// * Tokenize

		    Matcher m = tokenizer.matcher(bf);
		    while(m.find())
				buffer.add(m.group());

		}
		
		private String pop()
		{
			read();
			if(buffer.isEmpty())
				return null;
			return buffer.pop();
		}
		
		
		private void start() 
		{
			String next = pop();
			if(next == null)
				return;
			
			if(next.toLowerCase().equals("creator"))
				readCreator();
			
			if(next.toLowerCase().equals("graph"))
				readGraph();
		}
		
		private void readCreator()
		{
			pop();
			start();
		}
		
		private void readGraph()
		{
			if(pop().equals("["))
				inGraph();
			else 
				throw new IllegalStateException("Keyword 'graph' should be followed by a [");
		}
		
		private void inGraph()
		{
			while(true)
			{
				String next = pop();
				
				if(next.toLowerCase().equals("node"))
					readNode();
				
				if(next.toLowerCase().equals("edge"))
					readEdge();
			
				if(next.equals("]"))
					break;
			}
		}
		
		private String label;
		private Integer id;
		
		private void readNode()
		{
			label = null;
			id = null;
			
			if(! pop().equals("["))
				throw new IllegalStateException("Node definition should start with {");
			
			String next = pop();
			while(! next.equals("]"))
			{
					
				if(next.toLowerCase().equals("id"))
					id = Integer.parseInt(pop());
				
				if(next.toLowerCase().equals("label"))
					label = pop();
				
				next = pop();	
			}

			if(id == null)
				throw new RuntimeException("Graph description did not contain id.");
			
			LVertex vert = new LVertex(id);
			vert.setLabel(label);
			
			graph.addVertex(vert);
		}
		
		int edges = 0;
		private Integer to, from;	
		private void readEdge()
		{
			to = null;
			from = null;
			if(! pop().equals("["))
				throw new IllegalStateException("Node definition should start with {");
			
			String next = pop();
			while(! next.equals("]") )
			{
				if(next.toLowerCase().equals("source"))
					from = Integer.parseInt(pop());
				
				if(next.toLowerCase().equals("target"))
					to = Integer.parseInt(pop());
				
				next = pop();
			}
			
			if(to == null)
				throw new RuntimeException("Target id missing from node definition.");
				
			if(from == null)
				throw new RuntimeException("Source id missing from node definition.");
			
			graph.addEdge(new Edge<String>(""+edges), new LVertex(from), new LVertex(to));
			edges++;
		}
	}
	
	public static class LVertex extends Vertex<Integer>
	{
		public String label;

		public LVertex(Integer id)
		{
			super(id);
		}
		
		public void setLabel(String label)
		{
			this.label = label;
		}
		
		public int id()
		{
			return super.getLabel();
		}
		
		public String label()
		{
			return label;
		}

		@Override
		public int hashCode()
		{
			return id();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			LVertex other = (LVertex) obj;
			if(other.id() != id())
				return false;
			
			return true;
		}
	}
}
