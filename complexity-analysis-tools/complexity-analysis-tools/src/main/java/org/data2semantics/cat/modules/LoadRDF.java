package org.data2semantics.cat.modules;

import java.io.File;
import java.util.List;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.data.RDF;
import org.lilian.graphs.data.RDFDataSet;
import org.lilian.graphs.data.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

@Module(name="Load RDF")
public class LoadRDF
{

	@In(name="file")
	public String file;
	
	@Main(name="data", print=false)
	public DTGraph<String, String> load()
	{
		
		RDFFormat format = RDFFormat.forFileName(file);
		
		if(format == null)
			throw new RuntimeException("RDF file "+file+" not recognized");
			
		RDFDataSet testSet = new RDFFileDataSet(file, format);

		List<Statement> triples = testSet.getFullGraph();	
		
		return RDF.createDirectedGraph(triples, null, null);
		
	}
	
}
