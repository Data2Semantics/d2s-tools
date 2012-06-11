package org.data2semantics.tools.rdf;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
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
			RepositoryConnection repCon = rdfRep.getConnection();

			try {
				RepositoryResult<Statement> statements = repCon.getStatements(querySub, queryPred, queryObj, allowInference);
				
				try {
					resGraph.addAll(statements.asList());
				}
				finally {
					statements.close();
				}
			} finally {
				repCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resGraph;
	}
	
	
	public org.openrdf.model.Graph sparqlQuery(String sparqlQuery) {
		org.openrdf.model.Graph graph = new GraphImpl();
		
		try {
			RepositoryConnection repCon = rdfRep.getConnection();
			try {
				GraphQueryResult graphResult = repCon.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();
				
				try {
					while (graphResult.hasNext()) {
						graph.add(graphResult.next());
					}					
				} finally {
					graphResult.close();
				}							
			} finally {
				repCon.close();
			}			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}
	
		

}
