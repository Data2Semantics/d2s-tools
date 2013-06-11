package org.data2semantics.annotate;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.lilian.experiment.Experiment;
import org.lilian.experiment.Resources;
import org.lilian.graphs.Graph;

public class App
{

	public enum Type {RDFXML, TURTLE};
	
    @Option(name="--type", usage="Selects the type of input file: RDFXML, TURTLE")
	private static Type type = Type.TURTLE;

    @Option(name="--data", usage="The file containing the data.")    
	private static File data;
    
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
    	if(type == Type.RDFXML)
    		graph = Resources.rdfGraph(data);
    	else if(type == Type.TURTLE)
    		graph = Resources.turtleGraph(data);
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
