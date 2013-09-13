package org.data2semantics.cat.modules;

import java.io.File;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.data.RDF;

@Module(name="Load RDF")
public class LoadRDF
{

	@In(name="file")
	public String file;
	
	@In(name="type")
	public String type;
	
	@Main(name="data", print=false)
	public DTGraph<String, String> load()
	{
		if(type.toLowerCase().equals("turtle"))
			return RDF.readTurtle(new File(file));
		
		if(type.toLowerCase().equals("xml"))
			return RDF.read(new File(file));
		
		throw new RuntimeException("RDF type "+type+" not recognized");
	}
	
}
