package org.data2semantics.platform.reporting;

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

	public void report();
	
	/**
	 * The workflow to which this reporter is connected.
	 * 
	 * @return
	 */
	public Workflow workflow();
}
