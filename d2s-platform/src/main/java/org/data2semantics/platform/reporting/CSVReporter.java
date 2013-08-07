package org.data2semantics.platform.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;

import au.com.bytecode.opencsv.CSVWriter;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
			
			
			// * Write the title row
			int n = module.inputs().size() + module.outputs().size();
			
			String[] line = new String[n];
			
			int i = 0;
			for(Input input : module.inputs())
				line[i++] = input.name();
			
			for(Output output : module.outputs())
				line[i++] = output.name(); 
			
			writer.writeNext(line);
			
			// * Write the outputs
			
			for(ModuleInstance instance : module.instances())
			{
				i = 0;
				for(InstanceInput input : instance.inputs())
					line[i++] = input.value().toString();
				
				for(InstanceOutput output : instance.outputs())
					line[i++] = output.value().toString(); 
				
				writer.writeNext(line);
			}
			
			writer.close();
		}
		
	}

}
