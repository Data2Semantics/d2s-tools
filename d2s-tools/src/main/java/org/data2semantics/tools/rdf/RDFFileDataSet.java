package org.data2semantics.tools.rdf;

import java.io.File;
import org.openrdf.rio.RDFFormat;


public class RDFFileDataSet extends RDFDataSet 
{
	
	public RDFFileDataSet(String filename, RDFFormat fileFormat) {
			super(filename);
			addFile(filename, fileFormat);
	}

	public RDFFileDataSet(File file, RDFFormat fileFormat) {
		super(file.toString());
		addFile(file, fileFormat);
	}
	
	public void addFile(String filename, RDFFormat fileFormat) {
		try {
			this.rdfRep.getConnection().add(new File(filename), null, fileFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addFile(File file, RDFFormat fileFormat) {
		try {
			this.rdfRep.getConnection().add(file, null, fileFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
