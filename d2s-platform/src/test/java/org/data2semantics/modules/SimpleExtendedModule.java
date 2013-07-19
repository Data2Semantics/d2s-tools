package org.data2semantics.modules;

import org.data2semantics.platform.core.AbstractModule;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.resourcespace.ResourceSpace;

public class SimpleExtendedModule extends AbstractModule {

	public SimpleExtendedModule(Workflow parent) {
		super(parent);
	}

	@Override
	public boolean execute() {
		return false;
	}

	@Override
	public Object getReference(String referenceKey) {
		
		return null;
	}

	public void getNextInputs(ResourceSpace resourceSpace) {
		
		
	}

	public void storeResults(ResourceSpace resultSpace) {
		
		
	}

}
