package org.data2semantics.platform.execution;

import java.util.concurrent.Callable;

import org.data2semantics.platform.core.ModuleInstance;

public class ModuleInstanceWorker implements Callable<Boolean> {

	ModuleInstance mi;
	public ModuleInstanceWorker(ModuleInstance mi) {
		this.mi = mi;
	}

	@Override
	public Boolean call() throws Exception {
		System.out.println("Executing " + mi.module().name());
		return mi.execute();
	}

	
}
