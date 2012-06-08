package org.data2semantics.tools.rdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

public class RDFDataSet {
	private Repository rdfRep;

	public RDFDataSet(Repository rdfRep) {
		this.rdfRep = rdfRep;
	}
	
	
	public org.openrdf.model.Graph getFullGraph() {
		return getStatements(null, null, null, true);
	}
	
	
	public Graph getStatements(String subject, String predicate, String object,
			boolean allowInference) {
		
		Graph resGraph = new GraphImpl();	
		
		URI querySub = null;
		URI queryPred = null;
		URI queryObj = null;

		if (subject != null) {
			querySub = rdfRep.getValueFactory().createURI(subject);
		}
		
		if (predicate != null) {
			queryPred = rdfRep.getValueFactory().createURI(predicate);
		}		

		if (object != null) {
			queryObj = rdfRep.getValueFactory().createURI(object);
		}
				
		try {
			RepositoryConnection swrcRepCon = rdfRep.getConnection();

			try {
				RepositoryResult<Statement> statements = swrcRepCon.getStatements(querySub, queryPred, queryObj, allowInference);
				
				try {
					resGraph.addAll(statements.asList());
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
		
		return resGraph;
	}
	
	
	public org.openrdf.model.Graph sparqlQuery(String sparqlQuery) {
		// TODO implement
		return null;
	}
	
	
	// TODO remove
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


	// TODO remove
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
