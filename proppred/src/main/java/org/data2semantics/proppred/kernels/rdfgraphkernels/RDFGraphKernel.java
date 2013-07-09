package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public interface RDFGraphKernel extends Kernel {
	
	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList);

}