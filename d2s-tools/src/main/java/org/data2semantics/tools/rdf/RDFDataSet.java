package org.data2semantics.tools.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public abstract class RDFDataSet
{
	private String label;

	public RDFDataSet() {
		this.label = "RDF dataset";
	}
	
	public RDFDataSet(String label) {
		this();
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public abstract Statement createStatement(URI subject, URI predicate, URI object);
	
	public abstract URI createURI(String uri);
	
	public abstract Literal createLiteral(String lit);
	
	public abstract void addStatements(List<Statement> stmts);
	

	public List<Statement> getFullGraph() 
	{	
		return getStatements(null, null, null, true);
	}
		
	
	public List<Statement> getStatements(Resource subject, URI predicate, Value object) {
		return getStatements(subject, predicate, object, false);
	}

	public abstract List<Statement> getStatements(Resource subject, URI predicate, Value object, boolean allowInference);

	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	public abstract List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference); 
	
	
	public abstract void removeStatementsFromStrings(String subject, String predicate, String object);	
	
	public abstract void removeStatements(Resource subject, URI predicate, Value object); 	
	

	public List<Statement> getSubGraph(String startNode, int depth, boolean includeInverse) {
		return getSubGraph(createURI(startNode), depth, includeInverse, false, null);
	}

	public List<Statement> getSubGraph(String startNode, int depth, boolean includeInverse, boolean allowInference, List<Statement> bl) {
		return getSubGraph(createURI(startNode), depth, includeInverse, allowInference, bl);
	}

	public List<Statement> getSubGraph(Resource startNode, int depth, boolean includeInverse) {
		return getSubGraph(startNode, depth, includeInverse, false, null);	
	}

	public List<Statement> getSubGraph(Resource startNode, int depth, boolean includeInverse, boolean allowInference, List<Statement> bl) {
		Set<Statement> graph = new HashSet<Statement>();
		List<Statement> result;
		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;	

		queryNodes.add(startNode);

		for (int i = 0; i < depth; i++) {
			newQueryNodes = new ArrayList<Resource>();

			for (Resource queryNode : queryNodes) {
				result = getStatements(queryNode, null, null, allowInference);
				if (bl != null) {					
					result.removeAll(bl);
				}
				graph.addAll(result);
				newQueryNodes.addAll(getEndNodes(result, false));

				if (includeInverse) {
					result = getStatements(null, null, queryNode, allowInference);
					if (bl != null) {
						result.removeAll(bl);
					}
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

			
}
