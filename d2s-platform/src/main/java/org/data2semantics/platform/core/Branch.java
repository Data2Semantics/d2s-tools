package org.data2semantics.platform.core;

import java.util.Collection;
import java.util.List;

/**
 * A branch is a node in a lattice which identifies a specific execution 
 * 'universe' within the workflow. Each sweep introduces new sub branches under 
 * the current branch  
 *
 */

public interface Branch
{
	/**
	 * The branches directly upstream of this branch.
	 * @return
	 */
	public List<Branch> parents();
	
	/**
	 * The branches directly downstream of this branch.
	 * @return
	 */
	public List<Branch> children();

	/**
	 * All branches upstream of this branch.
	 * @return
	 */
	public Collection<Branch> ancestors();
	
	/**
	 * All branches downstream of this branch.
	 * @return
	 */
	public Collection<Branch> descendants();
	
	/**
	 * The point of divergence for this branch. Ie. the first moduleinstance 
	 * that was created specifically for this branch. 
	 * @return
	 */
	public ModuleInstance point();

	public Collection< Branch> siblings();
}
