package org.data2semantics.modules;

import java.util.ArrayList;

import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.OutputField;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="RDFWLSubTree")
public class RDFWLSubTreeKernelModule {
	
	@OutputField(name="result")
	public String justCheck;
	
	@OutputField(name="featureVector")
	public SparseVector[] sparseVector;
	
	@OutputField(name="kernel")
	public RDFWLSubTreeKernel kernel ;
	
	@MainMethod
	public SparseVector[] testMain(
						 @InputParameter(name="dataset") RDFDataSet dataset, 
						 @InputParameter(name="instances") ArrayList<Resource> instances, 
						 @InputParameter(name="labels") ArrayList<Value> labels, 
						 @InputParameter(name="blacklist")  ArrayList<Statement> blackList,
						 @InputParameter(name="iteration") int iteration,
						 @InputParameter(name="depth") int depth){
		
		kernel = new RDFWLSubTreeKernel(iteration,  depth, true, true);
		
		sparseVector = kernel.computeFeatureVectors(dataset, instances, blackList);
		
		return sparseVector;
	}
}
