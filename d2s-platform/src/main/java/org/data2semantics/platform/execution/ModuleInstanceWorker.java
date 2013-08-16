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
		System.out.println("Starting instance " + mi.module().name() + " thread "+Thread.currentThread().getName());
		Boolean result= mi.execute();
		System.out.println("Finish instance " + mi.module().name() + " thread "+Thread.currentThread().getName());
		
		
		return result;
	}

	
}
