package org.data2semantics.exp.modules;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

@Module(name="RDFDataSet")
public class RDFDataSetModule {
	private RDFDataSet dataset;
	private String filename;
	private RDFFormat format;
	
	
	
	
	public RDFDataSetModule(
			@In(name="filename") String filename, 
			@In(name="mimetype") String mimetype) {
		super();
		this.filename = filename;
		this.format = RDFFormat.forMIMEType(mimetype);
	}


	@Main
	public RDFDataSet createDataSet() {		
		dataset = new RDFFileDataSet(filename, format);	
		return dataset;
	}
	
	@Out(name="dataset")
	public RDFDataSet getDataSet() {
		return dataset;
	}	
}
