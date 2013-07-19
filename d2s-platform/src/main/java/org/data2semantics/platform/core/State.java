package org.data2semantics.platform.core;

public enum State {
	INIT,
	
	BLOCKED,
	
	// All the required input for a module is available, and are ready to be executed.
	READY,
	
	// The module is running
	RUNNING,
	
	// The module is finished
	FINISHED,
	
	// Execution of module failed.
	FAILED, 
}
