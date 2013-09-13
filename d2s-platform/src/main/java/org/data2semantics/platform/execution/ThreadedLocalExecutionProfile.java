package org.data2semantics.platform.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.reporting.Reporter;

public class ThreadedLocalExecutionProfile extends ExecutionProfile {

	private int threadPoolSize = 10;
	
	public void setThreadPoolSize(int threadPoolSize){
		this.threadPoolSize = threadPoolSize;
	}
	
	
	@Override
	public void executeModules(List<Module> modules, List<Reporter> reporters) {
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		 
		for(Module m : modules){
			
			for(Reporter reporter : reporters){
				
				try {
					reporter.report();
			
				} catch (IOException e) {
					
				}
			}
			
			if(m.ready()){
				
				// Instances of this module will be created
				// Outputs from previous dependency are also provided here.
				m.instantiate();

				List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
				
				for(ModuleInstance mi : m.instances()){
	
					Callable<Boolean> moduleInstanceWorker = new ModuleInstanceWorker(mi);
					results.add(executor.submit(moduleInstanceWorker));
				}
				
				//Make sure all the instance finished before continue to next batch of modules.
				for(Future<Boolean> res : results ){
					try {
						res.get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			} else 
				throw new IllegalStateException("Module not ready: " + m.name());
			
			
		}		
		
		
	}

}
