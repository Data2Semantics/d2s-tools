package org.data2semantics.tools.graphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class for reading graphs that are to large to be stored in memory. The 
 * graph is is read from a text file as 
 * 
 * 
 * @author Peter
 *
 * @param <V>
 * @param <E>
 */
public class EdgeIterable<V, E> implements Iterable<EdgeIterable.Line<V, E>>
{
	private File file;
	private LineParser<V, E> parser;
	
	public EdgeIterable(File file, LineParser<V, E> parser)
	{
		super();
		this.file = file;
		this.parser = parser;
	}

	public static class Line<V, E>
	{
		private V fromVertex, toVertex;
		private E edgeLabel;
		
		public Line(V fromVertex, V toVertex, E edgeLabel)
		{
			this.fromVertex = fromVertex;
			this.toVertex = toVertex;
			this.edgeLabel = edgeLabel;
		}

		public V fromVertex()
		{
			return fromVertex;
		}

		public V toVertex()
		{
			return toVertex;
		}

		public E edgeLabel()
		{
			return edgeLabel;
		}

		@Override
		public String toString()
		{
			return fromVertex + " " + toVertex
					+ " (" + edgeLabel + ")";
		}
		
		
	}
	
	public static interface LineParser<V, E>
	{
		public boolean skip(String line);
		
		public Line<V, E> parse(String line);
	}

	public Iterator<Line<V, E>> iterator()
	{
		return new EIterator();
	}
	
	private class EIterator implements Iterator<Line<V, E>>
	{
		private BufferedReader reader;
		private String line;
		
		public EIterator()
		{
			try
			{
				reader = new BufferedReader(new FileReader(file));
			} catch (IOException e)
			{ 
				throw new RuntimeException(e); 
			}
			
			read();
			
		}
		
		private void read()
		{
			do {
				try
				{
					line = reader.readLine();
				} catch (IOException e)
				{
					throw new RuntimeException(e); 
				}
			} while(parser.skip(line));
		}

		public boolean hasNext()
		{
			return line != null;
		}

		public Line next()
		{
			if(!hasNext())
				throw new NoSuchElementException();
			
			String r = line;
			read();
			
			return parser.parse(r);
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Returns an edge iterable that reads vertices labeld with unique integers
	 * and edges (possibly) labeled with non-unique integers.
	 * 
	 * If the input file specifies two integers on a line, this is seens as an 
	 * unlabeled edge (null) and three integers are seens as a labeled edge 
	 * (with the middle being the label).
	 * @return
	 */
	public static EdgeIterable<Integer, Integer> integers(File file)
	{
		return new EdgeIterable<Integer, Integer>(file, new LineParser<Integer, Integer>()
		{
			public EdgeIterable.Line<Integer, Integer> parse(String line)
			{
				String split[] = line.split("\\s");
				
				String fromRaw, toRaw, labelRaw;
				if(split.length == 2)
				{
					fromRaw = split[0];
					toRaw = split[1];
					labelRaw = null;
				} else if(split.length == 3)
				{
					fromRaw = split[0];
					toRaw = split[2];
					labelRaw = split[1];
				} else
					throw new IllegalStateException("Line (" + line + ") does not split into two or three parts.");
				
				Integer from = prs(fromRaw),
						to = prs(toRaw), 
						label = prs(labelRaw);
				
				return new Line<Integer, Integer>(from, to, label);
			}

			private Integer prs(String raw)
			{
				if(raw == null)
					return null;
					
				return Integer.parseInt(raw);
			}

			public boolean skip(String line)
			{
				if(line == null)
					return false;
				
				if(line.trim().length() == 0)
					return true;
				
				if(line.startsWith("#"))
					return true;
				
				return false;
			}

		});
	}
}
