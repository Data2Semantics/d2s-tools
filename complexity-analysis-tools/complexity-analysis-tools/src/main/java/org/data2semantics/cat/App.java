package org.data2semantics.cat;

import java.io.File;
import java.io.IOException;

import org.lilian.experiment.Run;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	File dir = new File("/Users/Peter/Experiments/graphs/commit/");
    	
    	Run.run(dir);
    }
}
