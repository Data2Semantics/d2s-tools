package org.data2semantics.tools.rdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class RDFDataSet {
	private Repository rdfRep;

	public RDFDataSet(String filename) {		
		File file = new File(filename);
		
		try {
			rdfRep = new SailRepository(new MemoryStore());
			rdfRep.initialize();
			RepositoryConnection swrcRepCon = rdfRep.getConnection();

			try {
				swrcRepCon.add(file, null, RDFFormat.RDFXML);

			} finally {
				swrcRepCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}


	public List<Statement> getInstanceURIs(String predicate, String object) {

		List<Statement> results = new ArrayList<Statement>();
		URI queryPred = null;
		URI queryObj = null;

		if (predicate != null) {
			queryPred = rdfRep.getValueFactory().createURI(predicate);
		}		

		if (object != null) {
			queryObj = rdfRep.getValueFactory().createURI(object);
		}

		try {
			RepositoryConnection swrcRepCon = rdfRep.getConnection();

			try {
				RepositoryResult<Statement> statements = swrcRepCon.getStatements(null, queryPred, queryObj, true);

				try {
					results.addAll(statements.asList());
				}
				finally {
					statements.close();
				}
			} finally {
				swrcRepCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}


	public List<Statement> getStatements(Resource resource, boolean fromObject) {
		List<Statement> results = new ArrayList<Statement>();

		try {
			RepositoryConnection swrcRepCon = rdfRep.getConnection();

			try {
				RepositoryResult<Statement> statements = null;
				if (!fromObject) {
					statements = swrcRepCon.getStatements(resource, null, null, true);
				} else {
					statements = swrcRepCon.getStatements(null, null, resource, true);
				}

				try {
					results.addAll(statements.asList());		
				}
				finally {
					statements.close();
				}
			} finally {
				swrcRepCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;		
	}







}
