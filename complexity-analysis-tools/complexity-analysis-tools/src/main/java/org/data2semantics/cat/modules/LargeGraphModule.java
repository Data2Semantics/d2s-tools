package org.data2semantics.cat.modules;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.data2semantics.cat.LargeGraph;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.lilian.graphs.Graph;


@Module(name="Large Graph measures")
public class LargeGraphModule <N> extends LargeGraph<N>{
	
	
	
	public LargeGraphModule(@In(name="data") Graph<N> graph) {
		super(graph);
		this.logger = Logger.getLogger(this.getClass().toString());
		
	}

	@Main
	@Override
	public void body()
	{
		
		super.setup();
		super.body();
	}
	
	@Out(name="Degrees")
	public List<Integer> degrees()
	{
		return Collections.unmodifiableList(degrees);
	}
	
	@Out(name="Power law exponent")
	public double plExponent()
	{
		return plExponent;
	}
	
	@Out(name="Power law min")
	public int plMin()
	{
		return plMin;
	}
	
	@Out(name="Power law significance")
	public double plSignificance()
	{
		return plSignificance;
	}
	
	@Out(name="Visualization")
	public BufferedImage visualization()
	{
		return image;
	}
	
	
}
