package org.data2semantics.annotate;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.lilian.experiment.Experiment;
import org.lilian.experiment.Resources;
import org.lilian.graphs.Graph;
import org.lilian.graphs.data.RDF;
import org.lilian.graphs.data.RDFDataSet;
import org.lilian.graphs.data.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

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
    	{
    		RDFDataSet testSet = new RDFFileDataSet(data, RDFFormat.RDFXML);

    		List<Statement> triples = testSet.getFullGraph();	
    		
    		graph = RDF.createDirectedGraph(triples, null, null);
    		
    	} else if(type == Type.TURTLE) 
    	{
    		RDFDataSet testSet = new RDFFileDataSet(data, RDFFormat.TURTLE);

    		List<Statement> triples = testSet.getFullGraph();	
    		
    		graph = RDF.createDirectedGraph(triples, null, null);
    	}
    	
    	
    	
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
