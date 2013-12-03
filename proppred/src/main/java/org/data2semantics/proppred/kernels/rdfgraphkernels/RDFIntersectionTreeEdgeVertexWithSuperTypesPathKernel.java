package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * Implementation of the Intersection Tree Path kernel, as described in the DMoLD paper. This is the variant in the paper, with path's described by edge and vertex labels.
 * With the probabilities boolean in the constructor set to true, the values in the featureVectors will be computed as transition probabilities.
 * 
 * The Intersection Tree Path kernel has performance comparable to the IntersectionSubTree kernel but is much, much faster to compute.
 * 
 * @author Gerben
 *
 */
public class RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel implements RDFGraphKernel, RDFFeatureVectorKernel {
	private int depth;
	private boolean inference;
	protected Map<Value, Integer> uri2int;
	private Map<List<Integer>, Integer> path2index;
	private Map<Integer, List<Integer>> index2path;
	private RDFDataSet dataset;
	private Set<Statement> blackList;
	private boolean normalize;
	private String label;
	protected int pathLen;
	private Value rootValue;
	private boolean probabilities;
	
	
	public RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel(int depth, boolean inference, boolean normalize) {
		this(depth, true, inference, normalize);
	}
	
	public RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel(int depth, boolean probabilities, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.inference = inference;
		this.probabilities = probabilities;

		uri2int = new HashMap<Value, Integer>();
		path2index = new HashMap<List<Integer>, Integer>();
		index2path = new HashMap<Integer, List<Integer>>();
		blackList = new HashSet<Statement>();
		this.pathLen = 2;
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;		
	}
	
	
	public SparseVector[] computeFeatureVectors(RDFDataSet dataset, List<Resource> instances,
			List<Statement> blackList) {
		this.dataset = dataset;	
		this.blackList.addAll(blackList);

		rootValue = dataset.createLiteral(KernelUtils.ROOTID);
		
		SparseVector[] ret = new SparseVector[instances.size()];

		for (int i = 0; i < instances.size(); i++) {
			ret[i] = processVertex(instances.get(i));
		}
		
		for (SparseVector fv : ret) {
			fv.setLastIndex(path2index.size());
		}
		if (normalize) {
			ret = KernelUtils.normalize(ret);
		}
		return ret;
	}
	
	private SparseVector processVertex(Resource root) {
		SparseVector features = new SparseVector();
		processVertexRec(root, new ArrayList<Integer>(), features, depth, root);
		if (probabilities) {
			features = normalizeFeatures(features);
		}
		return features;
	}
	
	private SparseVector normalizeFeatures(SparseVector features) {
		SparseVector res = new SparseVector();
		for (int key : features.getIndices()) {
			List<Integer> path = index2path.get(key);
			if (path.size()==0) {
				res.setValue(key, 1.0);
			} else {
				List<Integer> parent = path.subList(0, path.size()-pathLen);
				int parentKey = path2index.get(parent);
				res.setValue(key, features.getValue(key)/features.getValue(parentKey));
			}
		}
		return res;
	}
	
	private void processVertexRec(Value v1, List<Integer> path, SparseVector vec, int maxDepth, Resource instance) {

		// Count
		Integer index = path2index.get(path);
		if (index == null) {
			index = path2index.size()+1;
			path2index.put(path, index);
			index2path.put(index, path);
		}
		vec.setValue(index, vec.getValue(index)+1);

		// Bottom out
		if (maxDepth > 0 && (v1 instanceof Resource)) {

			// Recurse
			List<Statement> result = dataset.getStatements((Resource)v1, null, null, inference);

			for (Statement stmt : result) {
				if (!blackList.contains(stmt)) {
					List<Integer> newPath = createPath(stmt, path, instance);
					processVertexRec(stmt.getObject(), newPath, vec, maxDepth-1, instance);
					
					// Use all its RDF types
					for (Statement stmt2 : dataset.getStatements((Resource) v1, RDF.TYPE, null, inference)) {
						newPath = createPath(stmt, path, (Resource) stmt2.getObject()); // objects of RDF TYPE are not literals
						processVertexRec(stmt.getObject(), newPath, vec, maxDepth-1, instance);
					}
				}
			}		
		}
	}
	
	protected List<Integer> createPath(Statement stmt, List<Integer> path, Resource instance) {
		
		Integer key = uri2int.get(stmt.getPredicate());
		if (key == null) {
			key = new Integer(uri2int.size());
			uri2int.put(stmt.getPredicate(), key);
		}
		
		// Set the instance nodes to one identical rootValue node
		Value obj = stmt.getObject();
		if (obj.toString().equals(instance.toString())) {
			obj = rootValue;
		}
		
		Integer key2 = uri2int.get(obj);
		if (key2 == null) {
			key2 = new Integer(uri2int.size());
			uri2int.put(obj, key2);
		}
		
		List<Integer> newPath = new ArrayList<Integer>(path);
		newPath.add(key);
		newPath.add(key2);

		return newPath;
	}
	
	public double[][] compute(RDFDataSet dataset, List<Resource> instances,
			List<Statement> blackList) {
		
		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());
		SparseVector[] featureVectors = computeFeatureVectors(dataset, instances, blackList);
		for (int i = 0; i < instances.size(); i++) {
			for (int j = i; j < instances.size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}		
		return kernel;
	}

}
