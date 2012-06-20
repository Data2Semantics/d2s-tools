package org.data2semantics.tools.graphs;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class EdgeIterableTest
{

	@Test
	public void test()
	{
		File file = new File("/Users/Peter/Documents/datasets/graphs/twitter/twitter_rv.net");
		
		long i = 0;
		for(EdgeIterable.Line<Integer, Integer> line : EdgeIterable.integers(file))
		{	// System.out.println(line);
			i++;
			if(i % 1000000 == 0)
				System.out.println(i);
		}
	}

}
