package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.Pair;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Simple kernel for computing a kernel on a pair of Resources. kernel1 is computed on the first elements of the pairs and kernel2 on the second.
 * 
 * @author Gerben
 *
 */
public class RDFPairKernel {
	double w1;
	double w2;

	public RDFPairKernel() {
		this(0.5,0.5);
	}

	public RDFPairKernel(double w1, double w2) {
		this.w1 = w2;
		this.w2 = w2;
	}


	public double[][] compute(RDFDataSet dataset, List<Pair<Resource>> instances, List<Statement> blacklist, RDFGraphKernel kernel1, RDFGraphKernel kernel2) {
		Map<Resource, Integer> inst1 = new HashMap<Resource, Integer>();
		Map<Resource, Integer> inst2 = new HashMap<Resource, Integer>();

		// Make lists of the Resources in the pairs, we use Maps because we need them later
		for (Pair<Resource> p : instances) {
			inst1.put(p.getFirst(), null);
			inst2.put(p.getSecond(), null);
		}

		List<Resource> inst1L = new ArrayList<Resource>(inst1.keySet());
		List<Resource> inst2L = new ArrayList<Resource>(inst2.keySet());

		int idx = 0;
		for (Resource res : inst1L) {
			inst1.put(res, idx++);
		}
		idx = 0;
		for (Resource res : inst2L) {
			inst2.put(res, idx++);
		}

		// Compute sub-kernels
		double[][] k1 = kernel1.compute(dataset, inst1L, blacklist);					
		double[][] k2 = kernel2.compute(dataset, inst2L, blacklist);		

		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());

		int ki = 0, kj;
		for (Pair<Resource> rp : instances) {
			kj = 0;
			for (Pair<Resource> rp2 : instances) {
				kernel[ki][kj] = (w1 * k1[inst1.get(rp.getFirst())][inst1.get(rp2.getFirst())]) + (w2 * k2[inst2.get(rp.getSecond())][inst2.get(rp2.getSecond())]); 				
				kj++;
			}
			ki++;
		}			
		return kernel;
	}


	public SparseVector[] computeFeatureVectors(RDFDataSet dataset, List<Pair<Resource>> instances, List<Statement> blacklist, RDFFeatureVectorKernel kernel1, RDFFeatureVectorKernel kernel2) {
		Map<Resource, SparseVector> inst1 = new HashMap<Resource, SparseVector>();
		Map<Resource, SparseVector> inst2 = new HashMap<Resource, SparseVector>();

		// Make lists of the Resources in the pairs, we use Maps because we need them later
		for (Pair<Resource> p : instances) {
			inst1.put(p.getFirst(), null);
			inst2.put(p.getSecond(), null);
		}

		List<Resource> inst1L = new ArrayList<Resource>(inst1.keySet());
		List<Resource> inst2L = new ArrayList<Resource>(inst2.keySet());


		// Compute sub-kernels
		SparseVector[] ret = kernel1.computeFeatureVectors(dataset, inst1L, blacklist);		
		for (int i = 0; i < ret.length; i++) {
			inst1.put(inst1L.get(i), ret[i]);
		}

		ret = kernel2.computeFeatureVectors(dataset, inst2L, blacklist);		
		for (int i = 0; i < ret.length; i++) {
			inst2.put(inst2L.get(i), ret[i]);
		}

		// Compute total kernel
		ret = new SparseVector[instances.size()];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = new SparseVector();
			inst1.get(instances.get(i).getFirst()).multiplyScalar(w1);
			ret[i].addVector(inst1.get(instances.get(i).getFirst()));
			inst2.get(instances.get(i).getSecond()).multiplyScalar(w2);
			ret[i].addVector(inst2.get(instances.get(i).getSecond()));
		}			
		return ret;
	}
}
