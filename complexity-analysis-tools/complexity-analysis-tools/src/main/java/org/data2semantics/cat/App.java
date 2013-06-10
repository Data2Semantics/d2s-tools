package org.data2semantics.cat;

import java.io.File;
import java.io.IOException;

import org.lilian.experiment.Environment;
import org.lilian.experiment.Experiment;
import org.lilian.experiment.Resources;
import org.lilian.experiment.Run;
import org.lilian.graphs.DTGraph;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final File DATA = new File("/Users/Peter/Documents/datasets/graphs/aifb/aifb-fixed_complete.owl");
	private static final File ENVIRONMENT = new File("/Users/Peter/Experiments/d2s-cat/");
	
    public static void main( String[] args ) throws IOException
    {
    	DTGraph<String, String> data = Resources.rdfGraph(DATA);
    	
    	Experiment experiment = new GraphMeasures<String>(data, "large");
    	
    	Environment env = new Environment(ENVIRONMENT, 0);
    	Environment.current = env;
    	
    	experiment.run();
    }
}
