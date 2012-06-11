package org.data2semantics.tools.rdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
	
	
	/*
	 * Wrapper for the Sesame connection getStatements, to avoid try-catch statements. 
	 */	
	public List<Statement> getStatements(Resource subject, URI predicate, Value object, boolean allowInference) {
		List<Statement> resGraph = new ArrayList<Statement>();
		
		try {
			RepositoryConnection repCon = rdfRep.getConnection();

			try {
				RepositoryResult<Statement> statements = repCon.getStatements(subject, predicate, object, allowInference);
				
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
	
	
	
	public List<Statement> getFullGraph() {	
		return getStatements(null, null, null, true);
	}
	
	
	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference) {	
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
		
		return getStatements(querySub, queryPred, queryObj, allowInference);
	}
	
	
	public List<Statement> sparqlQuery(String sparqlQuery) {
		List<Statement> graph = new ArrayList<Statement>();
		
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
	
	
	public List<Statement> getSubGraph(String startNode, int depth, boolean includeInverse) {
		return getSubGraph(rdfRep.getValueFactory().createURI(startNode), depth, includeInverse);
	}
	
	public List<Statement> getSubGraph(URI startNode, int depth, boolean includeInverse) {
		List<Statement> graph = new ArrayList<Statement>();
		List<Statement> result;
		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		
		queryNodes.add(startNode);
		
		for (int i = 0; i < depth; i++) {
			newQueryNodes = new ArrayList<Resource>();
			
			for (Resource queryNode : queryNodes) {
				result = getStatements(queryNode, null, null, true);
				graph.addAll(result);
				newQueryNodes.addAll(getEndNodes(result, false));
				
				if (includeInverse) {
					result = getStatements(null, null, queryNode, true);
					graph.addAll(result);
					newQueryNodes.addAll(getEndNodes(result, true));
				}
			}
			
			newQueryNodes.remove(startNode);
			queryNodes = newQueryNodes;
		}
		
		return graph;
	}
	
	private List<Resource> getEndNodes(List<Statement> statements, boolean fromObject) {
		List<Resource> newNodes = new ArrayList<Resource>();
		
		for (Statement statement : statements) {
			if (fromObject) {
				newNodes.add(statement.getSubject());
			} else if (statement.getObject() instanceof Resource) {
				newNodes.add((Resource) statement.getObject());
			}
		}
		return newNodes;		
	}

	
		

}
