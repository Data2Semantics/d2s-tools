package org.data2semantics.platform.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.util.WorkflowParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Run
{	
	private static WorkflowParser wfParser = new WorkflowParser();
	
    @Option(name="--classpath", usage="A colon or semicolon separatedlist of jar files and directories to be included in the classpath. (default: none)")
	private static String classPath = "";

    @Option(name="--output", usage="The output directory (default: working directory)")    
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
    	
    	if(run.arguments.size() < 1)
    		usageExit("No workflow file specified", parser);
    	
    	if(run.arguments.size() > 1)
    		usageExit("Too many non-option arguments: " + run.arguments, parser);
    	
    	File file = new File(run.arguments.get(0));
    	if(! file.exists())
    		usageExit("Workflow file ("+run.arguments.get(0)+") does not exist.", parser);
    	
    	// -- Beyond this point, errors are not the user's fault, and should not
    	//    cause a usage print. 
    	
    	
    	// * Scan the classpath for any Domains and add them dynamically to the 
    	//   global Domain store.
    	
    	// * Read the workflow description from a yaml file into a map
    	
    	Workflow wf = WorkflowParser.parseYAML(file);
    	
    	// -- The workflow object will check the consistency of the inputs and 
    	//    outputs and make sure that    
    	
    	// * Pass the sorted Workflow to a workflow executor
    	
    	
    	
 	
    }
    
    public static void usageExit(String message, CmdLineParser parser)
    {
        System.err.println("java -jar Platform.jar [options...] workflow.yaml");
        parser.printUsage(System.err);
        
        System.exit(1);
    }

}
