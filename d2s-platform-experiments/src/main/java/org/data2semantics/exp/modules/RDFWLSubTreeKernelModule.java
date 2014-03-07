 package org.data2semantics.exp.modules;

import java.util.List;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

@Module(name="RDFWLSubTreeKernel")
public class RDFWLSubTreeKernelModule extends RDFWLSubTreeKernel {
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blacklist;
	private SparseVector[] fv;
	private int depth;
	
	public RDFWLSubTreeKernelModule(
			@In(name="iterations") int iterations, 
			@In(name="depth") int depth,
			@In(name="inference") boolean inference, 
			@In(name="normalize") boolean normalize,
			@In(name="reverse") boolean reverse,
			@In(name="dataset") RDFDataSet dataset,
			@In(name="instances") List<Resource> instances,
			@In(name="blacklist") List<Statement> blacklist) {
		super(iterations, depth, inference, normalize, false, reverse);
		this.dataset = dataset;
		this.instances = instances;
		this.blacklist = blacklist;
		this.depth = depth;
	}
	
	@Main
	public SparseVector[] computeFeatureVectors() {
		fv = super.computeFeatureVectors(dataset, instances, blacklist);
		return fv;
	}
	
	@Out(name="featureVectors")
	public SparseVector[] getFeatureVectors() {
		return fv;
	}

	@Out(name="depth")
	public int getDepth() {
		return depth;
	}
	
	
	
	
}
