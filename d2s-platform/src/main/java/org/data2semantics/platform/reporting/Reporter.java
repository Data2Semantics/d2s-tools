package org.data2semantics.platform.reporting;

import java.io.IOException;

import org.data2semantics.platform.core.Workflow;

/**
 * A reporter analyzes and collates the results of an executed workflow into a 
 * specific form, for instance a set of html files, or a list of RDF triples. 
 * 
 * A reporter should be tied 
 * 
 * @author Peter
 *
 */
public interface Reporter
{

	/**
	 * Write the report. For reporters that write to disk, the IOException is 
	 * declared. Reporters that write to other resources should wrap their 
	 * exceptions in a RuntimeException.
	 * 
	 * @throws IOException
	 */
	public void report() throws IOException;
	
	/**
	 * The workflow to which this reporter is connected.
	 * 
	 * @return
	 */
	public Workflow workflow();
}
