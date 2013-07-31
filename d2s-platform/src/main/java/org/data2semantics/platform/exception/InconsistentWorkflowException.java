package org.data2semantics.platform.exception;

import java.util.Arrays;
import java.util.List;

public class InconsistentWorkflowException extends RuntimeException
{
	private static final long serialVersionUID = 7430562656018863109L;

	public InconsistentWorkflowException(String error)
	{
		this(Arrays.asList(error));
	}
	
	public InconsistentWorkflowException(List<String> errors)
	{
		super("Workflow could not be compiled due to inconsistencies. The following problems were found: " + errors);
	}

}
