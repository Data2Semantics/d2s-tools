package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFCombinedKernel implements RDFFeatureVectorKernel {
	private boolean normalize;
	private String label;
	private List<RDFFeatureVectorKernel> kernels;

	public RDFCombinedKernel(List<RDFFeatureVectorKernel> kernels, boolean normalize) {
		this.kernels = kernels;
		this.normalize = normalize;
		this.label = "RDF Combined Kernel";
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(RDFDataSet dataset,
			List<Resource> instances, List<Statement> blackList) {

		SparseVector[] featureVectors = new SparseVector[instances.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		for (RDFFeatureVectorKernel k : kernels) {
			SparseVector[] fv = k.computeFeatureVectors(dataset, instances, blackList);

			for (int i = 0; i < featureVectors.length; i++) {
				featureVectors[i].addVector(fv[i]);
			}
		}
		
		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		return featureVectors;
	}

}
