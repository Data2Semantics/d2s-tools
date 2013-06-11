package org.data2semantics.cat;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.lilian.Global;
import org.lilian.experiment.Environment;
import org.lilian.experiment.Experiment;
import org.lilian.experiment.Resources;
import org.lilian.experiment.Run;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.Graph;

/**
 * Hello world!
 *
 */
public class App 
{
	public enum Type {RDFXML, TURTLE, GML};
	
    @Option(name="--type", usage="Selects the type of input file: RDFXML, TURTLE or GML")
	private static Type type = Type.TURTLE;

    @Option(name="--data", usage="The file containing the data.")    
	private static File data;
    
    @Option(name="--size", usage="The size of the graph: huge, large, small. The smaller the graph, the more measures will be run.")
    private static String size = "huge";
    
    @Option(name="--out", usage="Output directory.")
    private static String out;
    
	private static File environment;
	
    public static void main( String[] args ) throws IOException
    {
    	// * Parse the command line arguments
    	readArguments(args);
    }
    
    public void run() throws IOException
    {
    	environment = new File(out);
    	
    	Graph<String> graph = null;
    	if(type == Type.GML)
    		graph = Resources.gmlGraph(data);
    	else if(type == Type.RDFXML)
    		graph = Resources.rdfGraph(data);
    	else if(type == Type.TURTLE)
    		graph = Resources.turtleGraph(data);
    		
    	Experiment experiment = new GraphMeasures<String>(graph, size);
    	
    	Environment env = new Environment(environment, 0);
    	Environment.current = env;
    	
    	experiment.run(); 	
    	
    	Global.log().info("App Finished");
    }

	private static void readArguments(String[] args) throws IOException
	{		
	    App bean = new App();
        CmdLineParser parser = new CmdLineParser(bean);
        
		if(args.length == 0)
		{
		    parser.printUsage(System.err);
		    System.exit(1);
		}
        
        try 
        {
        	parser.parseArgument(args);
        	bean.run();
        } catch (CmdLineException e) 
        {
        	// * Handling of wrong arguments
        	System.err.println(e.getMessage());
        	parser.printUsage(System.err);
        }
	}
}
