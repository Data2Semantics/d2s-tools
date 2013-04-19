package org.data2semantics.proppred.kernels;

import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public abstract class RDFGraphKernel extends Kernel {

	
	public RDFGraphKernel() {
		super();
	}

	public RDFGraphKernel(boolean normalize) {
		super(normalize);
	}
	
	public abstract double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList);

}