package org.data2semantics.modules;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.random.RandomGraphs;

@Module(name="RandomGraph")
public class RandomGraph {
	
	@Main
	public UGraph<String> generate(
			@In(name="nodes") int  nodes, 
			@In(name="links") int links)
	{
		return RandomGraphs.random(nodes, links);
	}

}
