package org.data2semantics.exp.modules;

import java.util.List;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.kernels.Pair;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFPairKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

@Module(name="RDFWLSubTreePairKernel")
public class RDFWLSubTreePairKernelModule extends RDFPairKernel {
	private RDFDataSet dataset;
	private List<Pair<Resource>> instances;
	private List<Statement> blacklist;
	private SparseVector[] fv;
	
	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean inference;
	private boolean reverse;
	
	public RDFWLSubTreePairKernelModule(
			@In(name="iterations") int iterations, 
			@In(name="depth") int depth,
			@In(name="inference") boolean inference, 
			@In(name="normalize") boolean normalize,
			@In(name="reverse") boolean reverse,
			@In(name="dataset") RDFDataSet dataset,
			@In(name="instances") List<Pair<Resource>> instances,
			@In(name="blacklist") List<Statement> blacklist) {
		super();
		this.depth = depth;
		this.iterations = iterations;
		this.normalize = normalize;
		this.inference = inference;
		this.reverse = reverse;		
		this.dataset = dataset;
		this.instances = instances;
		this.blacklist = blacklist;
	}
	
	@Main
	public SparseVector[] computeFeatureVectors() {
		fv = super.computeFeatureVectors(dataset, instances, blacklist, 
				new RDFWLSubTreeKernel(iterations, depth, inference, normalize, false, reverse), 
				new RDFWLSubTreeKernel(iterations, depth, inference, normalize, false, reverse)
		      );
		return fv;
	}
	
	@Out(name="featureVectors")
	public SparseVector[] getFeatureVectors() {
		return fv;
	}	
}
