package org.data2semantics.exp.utils;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;

public abstract class KernelExperiment<K extends Kernel> implements Runnable {
	protected K kernel;
	protected long[] seeds;
	protected List<Result> results;
	
	
	public KernelExperiment(K kernel, long[] seeds) {
		super();
		this.kernel = kernel;
		this.seeds = seeds;
		results = new ArrayList<Result>();
	}

	public abstract void run();

	
	public List<Result> getResults() {
		return results;
	}
	
	

}
