package org.data2semantics.tools.rdf;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class RDFFileDataSet extends RDFDataSet {
	
	public RDFFileDataSet(String filename, RDFFormat fileFormat) {					
			super(createRepository(filename, fileFormat));	
	}


	private static Repository createRepository(String filename, RDFFormat fileFormat) {	
		Repository rdfRep = null;
		
		try {
			File file = new File(filename);
			rdfRep = new SailRepository(new MemoryStore());
			rdfRep.initialize();
			RepositoryConnection swrcRepCon = rdfRep.getConnection();
			
			try {
				swrcRepCon.add(file, null, fileFormat);

			} finally {
				swrcRepCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rdfRep;
	}
	



}
