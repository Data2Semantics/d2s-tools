package org.data2semantics.modules;

import java.util.ArrayList;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="RDFWLSubTree")
public class RDFWLSubTreeKernelModule {
	
	@Out(name="result")
	public String justCheck;
	
	@Out(name="featureVector")
	public SparseVector[] sparseVector;
	
	@Out(name="kernel")
	public RDFWLSubTreeKernel kernel ;
	
	@Main
	public SparseVector[] testMain(
						 @In(name="dataset") RDFDataSet dataset, 
						 @In(name="instances") ArrayList<Resource> instances, 
						 @In(name="labels") ArrayList<Value> labels, 
						 @In(name="blacklist")  ArrayList<Statement> blackList,
						 @In(name="iteration") int iteration,
						 @In(name="depth") int depth){
		
		kernel = new RDFWLSubTreeKernel(iteration,  depth, true, true);
		
		sparseVector = kernel.computeFeatureVectors(dataset, instances, blackList);
		
		return sparseVector;
	}
}
