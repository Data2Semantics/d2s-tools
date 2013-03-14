package org.data2semantics.proppred;

import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


public interface PropertyPredictor {

	public void train(RDFDataSet dataset, List<Resource> instances, List<Value> labels);
	public List<Value> predict(RDFDataSet dataset, List<Resource> instances);

}

