package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Compute SparseVector feature vectors, instead of a kernel matrix on an RDFDataSet.
 * 
 * TODO, add a method for computation on a test set
 * 
 * @author Gerben
 *
 */
public interface RDFFeatureVectorKernel extends Kernel {
	public SparseVector[] computeFeatureVectors(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList);	
}
