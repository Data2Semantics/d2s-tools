package org.data2semantics.exp.molecules;

import java.util.List;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.nodes.UGraph;

/**
 * Kernel interface, used as a marker interface, since this does not define on what we compute a kernel.
 * 
 * @author Gerben
 */
public interface LinearMoleculeKernel<G> extends Kernel {
	public SparseVector[] computeFeatureVectors(List<G> trainGraphs);
}
