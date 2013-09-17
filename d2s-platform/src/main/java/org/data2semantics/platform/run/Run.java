package org.data2semantics.platform.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.execution.ThreadedLocalExecutionProfile;
import org.data2semantics.platform.reporting.CSVReporter;
import org.data2semantics.platform.reporting.HTMLReporter;
import org.data2semantics.platform.reporting.PROVReporter;
import org.data2semantics.platform.reporting.Reporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Run
{	
	private static WorkflowParser wfParser = new WorkflowParser();
	
	enum ExecutionProfiles {LOCAL, THREADED, HADOOP}
	@Option(name="--profile", usage="Execution profile to be used LOCAL THREADED HADOOP (default: LOCAL) ")
	private static ExecutionProfiles execProfile = ExecutionProfiles.LOCAL;
	  
    @Option(name="--classpath", usage="A directory containing source code and resources to be loaded. Each source file should be in a directory that matches the name of its controller (ie. java files should be in a directory called 'java'). (default: none)")
	private static String classPath = "";

    @Option(name="--output", usage="The output directory (default: the working directory)")    
	private static File output = new File(".");
    
    @Argument
    private List<String> arguments = new ArrayList<String>(1);
    
    public static void main(String[] args) throws IOException
    {
    	
    	// * Parse and check the command line input
    	Run run = new Run();
    	
    	CmdLineParser parser = new CmdLineParser(run);
    	try
		{
			parser.parseArgument(args);
		} catch (CmdLineException e)
		{
			usageExit(e.getMessage(), parser);
		}
    	
    	if(run.arguments.size() > 1)
    		usageExit("Too many non-option arguments: " + run.arguments, parser);
    	
    	File file;
    	if(run.arguments.isEmpty())
    		file = new File("workflow.yaml");
    	else 
    		file = new File(run.arguments.get(0));
    	
    	if(! file.exists())
    		usageExit("Workflow file ("+file+") does not exist.", parser);
    	
    	// -- Beyond this point, errors are not the user's fault, and should not
    	//    cause a usage print. 
    	
    	
    	// * Scan the classpath for any Domains and add them dynamically to the 
    	//   global Domain store.
    	
    	// * Read the workflow description from a yaml file into a map
    	
    	Workflow workflow = WorkflowParser.parseYAML(file);
    	
    	// -- The workflow object will check the consistency of the inputs and 
    	//    outputs and make sure that everything can be executed.  
    	    	
    	// Set the status file
    	File statusRunning = new File(output, "status.running");
    	statusRunning.createNewFile();
    	
		ExecutionProfile executionProfile;
		
		switch(execProfile){
			case LOCAL:
				executionProfile = new LocalExecutionProfile();
				break;
			case THREADED:
				executionProfile = new ThreadedLocalExecutionProfile();
				break;
			default:
				executionProfile = new LocalExecutionProfile();
		}
		
		ResourceSpace rp = new ResourceSpace();
		
		
		List<Reporter> reporters = Arrays.asList(
					new HTMLReporter(workflow, new File(output, "report/")),
					new CSVReporter(workflow, new File(output, "csv/")),
					new PROVReporter(workflow, new File(output, "prov/"))
				);
		
    	Orchestrator orchestrator = new Orchestrator(workflow,  executionProfile, rp, reporters);
    	
    	orchestrator.orchestrate();
    	
    	for(Reporter reporter : reporters)
    		reporter.report();
    	
    	// Set status to finished
    	File statusFinished = new File(output, "status.finished");
    	statusFinished.createNewFile();
    	
    	statusRunning.delete();
    	
    	Global.log().info("Workflow execution finished.");
    }
    
    public static void usageExit(String message, CmdLineParser parser)
    {
    	System.err.println(message);
        System.err.println("java -jar Platform.jar [options...] [input file (default:workflow.yaml)]");
        parser.printUsage(System.err);
        
        System.exit(1);
    }

}
