package org.data2semantics.platform.execution;

import java.io.IOException;
import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.reporting.Reporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;


/**
 * Local execution profile will just run the module in the current VM
 * @author wibisono
 *
 */
public class LocalExecutionProfile extends ExecutionProfile {

	@Override
	public void executeModules(List<Module> modules, List<Reporter> reporters) {
		
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

				
				for(ModuleInstance mi : m.instances()){
	
					// System.out.println(" Executing instance of module  : " + mi.module().name());
					// System.out.println("    Inputs : "+mi.inputs());
					mi.execute();
					// System.out.println("    Outputs : "+mi.outputs());
					// System.out.println(mi+" "+mi.state());
							
				}
				// System.out.println(m.name()+" "+m.finished());
				
			
			} else 
				throw new IllegalStateException("Module not ready: " + m.name());
		}
		
		
	}
	
	
	

}
