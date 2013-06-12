package org.data2semantics.annotate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.data2semantics.annotate.App.Type;
import org.lilian.Global;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

public class RDFGraphClusteringAnnotator {

	File originalDataFile;
	Type currentType;
	Repository tempRepository;
	RepositoryConnection conn;
	RDFFormat rdfType=RDFFormat.N3;
	
	public RDFGraphClusteringAnnotator(File data, Type type) {
		originalDataFile = data;
		currentType = type;
		tempRepository = new SailRepository(new MemoryStore());
		try {
			tempRepository.initialize();
			conn = tempRepository.getConnection();
			if(type == Type.RDFXML)
				rdfType = RDFFormat.RDFXML;
			else
				if(type == Type.TURTLE)
					rdfType = RDFFormat.TURTLE;
			
			conn.add(data,null,rdfType);
			
		} catch (Exception e1) {
			Global.log().warning("Failed to parse original RDF");
		}
	}

	
	public void annotate(String label, int cluster) {
		ValueFactory f = conn.getValueFactory();
		
		Resource node = null;
		if(label.startsWith("http"))
			node = f.createURI(label);
		else
			return ; // we are not annotating literals
					
		URI pred = f.createURI("http://data2semantics.org/experiments/clusterAs");
		Literal val = f.createLiteral(cluster);
		try {
			conn.add(node,pred,val);
		} catch (RepositoryException e) {
			Global.log().warning("Failed to parse original RDF");
		}
	}
	public void writeResult(File output) {
		try {
			FileWriter writer = new FileWriter(output);
			RDFWriter rdfWriter = Rio.createWriter(rdfType, writer);
			conn.export(rdfWriter);
			conn.close();
		
		} catch (RepositoryException e) {
			Global.log().warning("Failed to write annotated output RDF");
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			Global.log().warning("Failed to create output annotated rdf file ");
		}
	}

}
