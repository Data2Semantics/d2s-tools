package org.data2semantics.platform.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.util.Functions;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVReporter implements Reporter
{
	private Workflow workflow;
	private File root;
	
	public CSVReporter(Workflow workflow, File root)
	{
		this.workflow = workflow;
		this.root = root;
		
		root.mkdirs();
	}

	@Override
	public void report() throws IOException
	{
		new ReportWriter();
	}

	@Override
	public Workflow workflow()
	{
		return workflow;
	}
	
	/**
	 * A singular environment for the purpose of writing a report.
	 * 
	 * @author Peter
	 *
	 */
	private class ReportWriter
	{
		public ReportWriter() throws IOException
		{
			for(Module module : workflow.modules())
			{
				// * Output module information
				moduleOutput(module);
					
			}
		}

		private void moduleOutput(Module module) throws IOException
		{
			File file = new File(root, ReporterTools.safe(module.name()) + ".csv");
			
			CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(file)));
			
			Set<Input> allUniverse = new HashSet<Input>();
			
			for(ModuleInstance instance : module.instances()){
				allUniverse.addAll(instance.universe().keySet());
			}
			
			List<Input> universeKeys = new ArrayList<Input>(allUniverse);
			Comparator<Input> inputNameComparator = new Comparator<Input>() {
				@Override
				public int compare(Input o1, Input o2) {
					return (o1.module().name()+o1.name()).compareTo(o2.module().name()+o2.name());
				}
			};
			
			Collections.sort(universeKeys, inputNameComparator);
					
			// * Write the title row
			int n = universeKeys.size() + module.inputs().size() + module.outputs().size() +2;
			
			String[] line = new String[n];
			
			int i = 0;
			for(Input universeKey : universeKeys){
				line[i++] = universeKey.module().name()+"."+universeKey.name();
			}
			
			line[i++]=""; // Blank to separate universe with inputs
			
			for(Input input : module.inputs())
				line[i++] = input.name();
			
			line[i++]=""; // Blank to separate inputs with outputs
					
			for(Output output : module.outputs())
				line[i++] = output.name(); 
					
			
			writer.writeNext(line);
			
			// * Write the outputs
			
			for(ModuleInstance instance : module.instances())
			{
				i = 0;
				
				for(Input universeKey : universeKeys){
					if(instance.universe().containsKey(universeKey))
						line[i++] = instance.universe().get(universeKey).value().toString();
					else
						line[i++] = "-";
				}
				
				line[i++]=""; // Blank to separate universe with inputs
				
				for(InstanceInput input : instance.inputs())
					line[i++] = input.value().toString();
				
				line[i++]=""; // Blank to separate inputs with outputs
									
				for(InstanceOutput output : instance.outputs())
					line[i++] = Functions.toString(output.value()); 
				
						
			
				writer.writeNext(line);
			}
			
			writer.close();
		}
		
	}

}
