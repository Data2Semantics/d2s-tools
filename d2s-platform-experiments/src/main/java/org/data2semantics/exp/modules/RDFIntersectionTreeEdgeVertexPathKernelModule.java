package org.data2semantics.exp.modules;

import java.util.List;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

@Module(name="RDFIntersectionTreeEdgeVertexPathKernel")
public class RDFIntersectionTreeEdgeVertexPathKernelModule extends RDFIntersectionTreeEdgeVertexPathKernel {
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blacklist;
	private SparseVector[] fv;
	private int depth;
	
	public RDFIntersectionTreeEdgeVertexPathKernelModule(
			@In(name="depth") int depth,
			@In(name="probabilities") boolean probabilities, 
			@In(name="inference") boolean inference, 
			@In(name="normalize") boolean normalize,
			@In(name="dataset") RDFDataSet dataset,
			@In(name="instances") List<Resource> instances,
			@In(name="blacklist") List<Statement> blacklist) {
		super(depth, probabilities, inference, normalize);
		this.depth = depth;
		this.dataset = dataset;
		this.instances = instances;
		this.blacklist = blacklist;
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
