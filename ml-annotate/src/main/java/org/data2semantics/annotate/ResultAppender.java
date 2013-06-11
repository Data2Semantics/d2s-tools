package org.data2semantics.annotate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.lilian.Global;
import org.lilian.experiment.AbstractExperiment;
import org.lilian.util.ResultTurtleWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;


/**
 * This appender will accept a finished abstract experiment and original RDF file.
 
 * @author wibisono
 *
 */
public class ResultAppender {

	AbstractExperiment exp;
	File originalRDF;
	File tempFile;
	RDFFormat format;
	File targetFile;
	
	final static String baseURI = "http://data2semantics.org/experiment/annotation";
	public ResultAppender(AbstractExperiment e, File src, RDFFormat f, File tgt) {
		exp = e;
		originalRDF = src;
		format = f;
		targetFile = tgt;
		try {
			tempFile = File.createTempFile("annotation", "ttl");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void appendResult(){
		Writer turtleOut = null;
		try {
			turtleOut = new BufferedWriter( new FileWriter(tempFile));
			ResultTurtleWriter writer = new ResultTurtleWriter(exp, turtleOut);
			writer.writeExperiment();
			turtleOut.close();
		} catch (IOException e) {
			Global.log().warning("Failed to write result to temporary file");
			return;
		}
		
		Repository tempRepository = new SailRepository(new MemoryStore());
		RepositoryConnection conn = null;
		try {
			tempRepository.initialize();
			conn = tempRepository.getConnection();
		} catch (RepositoryException e1) {
			Global.log().warning("Failed to create repository for merging");
			return;
		}
	
		try {
			conn.add(originalRDF, baseURI, format);
		} catch (Exception e) {
			Global.log().warning("Failed to parse original RDF");
		} 
		
		try {
			conn.add(tempFile, baseURI, format);
		} catch (Exception e) {
			Global.log().warning("Failed to parse temporary file");
		} 
		
		FileWriter writer;
		try {
			writer = new FileWriter(targetFile);
			RDFWriter rdfWriter = Rio.createWriter(format, writer);
			conn.export(rdfWriter);
			conn.close();
		} catch (Exception e) {
			Global.log().warning("Failed to write the end results");
		} 		
		
		
	}
	
}
