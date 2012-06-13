package org.data2semantics.tools.rdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class RDFFileDataSet extends RDFDataSet 
{
	
	public RDFFileDataSet(String filename, RDFFormat fileFormat) {					
			super(createRepository(new File(filename), fileFormat));	
	}

	public RDFFileDataSet(File file, RDFFormat fileFormat) {					
		super(createRepository(file, fileFormat));	
	}
	

	
	private static Repository createRepository(File file, RDFFormat fileFormat) {	
		Repository rdfRep = null;
		
		try {
			rdfRep = new SailRepository(new MemoryStore());
			rdfRep.initialize();
			RepositoryConnection swrcRepCon = rdfRep.getConnection();
			
			try {
				swrcRepCon.add(file, null, fileFormat);

			} finally {
				swrcRepCon.close();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return rdfRep;
	}
	



}
