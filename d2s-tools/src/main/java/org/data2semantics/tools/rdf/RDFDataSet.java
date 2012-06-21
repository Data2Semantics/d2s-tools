package org.data2semantics.tools.rdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

public class RDFDataSet
{
	private Repository rdfRep;
	private String label;

	public RDFDataSet(Repository rdfRep, String label) {
		this.rdfRep = rdfRep;
		this.label = label;
	}

	/*
	 * 
	 * @param file
	 * @param fileFormat
	 * @param edgeWhiteList A list of regular expressions. Only edges that match one or more of these are included
	 * @param vertexWhiteList A list of regular expressions. Only vertices that one or more of these are included
	 */
	//	public RDFDataSet(Repository rdfRep, List<String> vertexWhiteList, List<String> edgeWhiteList)
	//	{
	//		this(rdfRep);
	//	}	
	//

	public List<Statement> getStatements(Resource subject, URI predicate, Value object) {
		return getStatements(subject, predicate, object, false);
	}


	/**
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

	public List<Statement> getFullGraph() 
	{	
		return getStatements(null, null, null, true);
	}



	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference) 
	{	
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
		return getSubGraph(rdfRep.getValueFactory().createURI(startNode), depth, includeInverse, false);
	}

	public List<Statement> getSubGraph(String startNode, int depth, boolean includeInverse, boolean allowInference) {
		return getSubGraph(rdfRep.getValueFactory().createURI(startNode), depth, includeInverse, allowInference);
	}

	public List<Statement> getSubGraph(URI startNode, int depth, boolean includeInverse) {
		return getSubGraph(startNode, depth, includeInverse, false);	
	}

	public List<Statement> getSubGraph(URI startNode, int depth, boolean includeInverse, boolean allowInference) {
		Set<Statement> graph = new HashSet<Statement>();
		List<Statement> result;
		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;

		queryNodes.add(startNode);

		for (int i = 0; i < depth; i++) {
			newQueryNodes = new ArrayList<Resource>();

			for (Resource queryNode : queryNodes) {
				result = getStatements(queryNode, null, null, allowInference);
				graph.addAll(result);
				newQueryNodes.addAll(getEndNodes(result, false));

				if (includeInverse) {
					result = getStatements(null, null, queryNode, allowInference);
					graph.addAll(result);
					newQueryNodes.addAll(getEndNodes(result, true));
				}
			}

			newQueryNodes.remove(startNode);
			queryNodes = newQueryNodes;
		}
	
		List<Statement> graphRet = new ArrayList<Statement>();
		graphRet.addAll(graph);
		
		return graphRet;
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

	public String getLabel() {
		return this.label;
	}
	
}
