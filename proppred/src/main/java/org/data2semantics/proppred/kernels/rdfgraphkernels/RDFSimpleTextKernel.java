package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.proppred.learners.text.TextUtils;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFSimpleTextKernel implements RDFFeatureVectorKernel,
		RDFGraphKernel {
	private boolean normalize;
	private String label;
	private boolean inference;
	private int depth;
	
	private List<Set<String>> texts;
	
	

	public RDFSimpleTextKernel(int depth, boolean inference, boolean normalize) {
		super();
		this.normalize = normalize;
		this.inference = inference;
		this.depth = depth;
		
		this.label = "RDF Simple Text Kernel, depth=" + depth;
		this.texts = new ArrayList<Set<String>>();
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances,
			List<Statement> blackList) {
		// TODO Auto-generated method stub
		return null;
	}

	public SparseVector[] computeFeatureVectors(RDFDataSet dataset,
			List<Resource> instances, List<Statement> blackList) {
		
		Set<Statement> blackListSet = new HashSet<Statement>();
		blackListSet.addAll(blackList);
		
		for (Resource instance : instances) {
			Set<String> text = new HashSet<String>();
			texts.add(text);
			processVertex(dataset, blackListSet, instance, text, depth);
		}
		
		List<String> strings = new ArrayList<String>();
		
		for (Set<String> text : texts) {
			StringBuilder str = new StringBuilder();
			for (String s : text) {
				str.append(" ");
				str.append(s);
			}
			strings.add(str.toString());
		}
		
		SparseVector[] ret = TextUtils.computeTF(strings).toArray(new SparseVector[1]);
		
		if (normalize) {
			ret = KernelUtils.normalize(ret);
		}		
		return ret;
	}
	
	private void processVertex(RDFDataSet dataset, Set<Statement> blackList, Value v1, Set<String> text, int maxDepth) {

		if (v1 instanceof Literal) {
			text.add(((Literal) v1).stringValue());
		}

		// Bottom out
		if (maxDepth > 0 && (v1 instanceof Resource)) {

			// Recurse
			List<Statement> result = dataset.getStatements((Resource) v1, null, null, inference);

			for (Statement stmt : result) {
				if (!blackList.contains(stmt)) {
					processVertex(dataset, blackList, stmt.getObject(), text, maxDepth-1);
				}
			}		
		}
	}

}
