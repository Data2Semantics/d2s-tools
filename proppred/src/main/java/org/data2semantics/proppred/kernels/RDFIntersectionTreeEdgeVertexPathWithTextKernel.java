package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.proppred.libsvm.text.TextUtils;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class RDFIntersectionTreeEdgeVertexPathWithTextKernel implements RDFGraphKernel, RDFFeatureVectorKernel {
	private int depth;
	private boolean inference;
	protected Map<Value, Integer> uri2int;
	private Map<List<Integer>, Integer> path2index;
	private Map<Integer, List<Integer>> index2path;
	
	private Map<List<Integer>, Integer> path2textIndex;
	private Map<Integer, TreeMap<Integer,String>> textIndex2index2text; // We use a TreeMap, so that the ordering is preserved and matches the instance ordering.
	
	private RDFDataSet dataset;
	private Set<Statement> blackList;
	private boolean normalize;
	private String label;
	protected int pathLen;
	private Value rootValue;
	private Value blankVertex;
	
	
	public RDFIntersectionTreeEdgeVertexPathWithTextKernel(int depth, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.inference = inference;

		uri2int = new HashMap<Value, Integer>();
		path2index = new HashMap<List<Integer>, Integer>();
		index2path = new HashMap<Integer, List<Integer>>();
		blackList = new HashSet<Statement>();
		
		path2textIndex = new HashMap<List<Integer>, Integer>();
		textIndex2index2text = new HashMap<Integer, TreeMap<Integer,String>>();
		
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
		blankVertex = dataset.createLiteral("blank");
		
		SparseVector[] ret = new SparseVector[instances.size()];

		for (int i = 0; i < instances.size(); i++) {
			ret[i] = processVertex(instances.get(i), i);
		}
		for (SparseVector fv : ret) {
			fv.setLastIndex(path2index.size());
		}
		
		SparseVector[] text = processTextVertices(instances);
		
		for (int i = 0; i < instances.size(); i++) {
			ret[i].addVector(text[i]);
		}

		return ret;
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
	
	
	private SparseVector processVertex(Resource root, int fvIndex) {
		SparseVector features = new SparseVector();
		processVertexRec(root, new ArrayList<Integer>(), features, depth, root, fvIndex);
		if (normalize) {
			features = normalizeFeatures(features);
		}
		return features;
	}
	
	private void processVertexRec(Value v1, List<Integer> path, SparseVector vec, int maxDepth, Resource instance, int fvIndex) {

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
					List<Integer> newPath = createPath(stmt, path, instance, fvIndex);
					processVertexRec(stmt.getObject(), newPath, vec, maxDepth-1, instance, fvIndex);
				}
			}		
		}
	}
	
	protected List<Integer> createPath(Statement stmt, List<Integer> path, Resource instance, int fvIndex) {
		
		Integer key = uri2int.get(stmt.getPredicate());
		if (key == null) {
			key = new Integer(uri2int.size());
			uri2int.put(stmt.getPredicate(), key);
		}
		
		Value obj = stmt.getObject();
		
		// Save the text string, and set the node to the blankVertex
		// so that we essentially consider the path up to the blankVertex
		String text = null;
		if (obj instanceof Literal) {
			text = ((Literal) obj).stringValue();
			obj = blankVertex;
		}
		
		// Set the instance nodes to one identical rootValue node
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
		
		if (text != null) {
			saveStringVertex(text, newPath, fvIndex);
		}

		return newPath;
	}
	
	
	private void saveStringVertex(String text, List<Integer> path, int fvIndex) {
		Integer key = path2textIndex.get(path); 
		
		if (key == null) {
			key = new Integer(path2textIndex.size());
			path2textIndex.put(path, key);
			textIndex2index2text.put(key, new TreeMap<Integer,String>());
		}
		textIndex2index2text.get(key).put(fvIndex, text);
	}
	
	private SparseVector[] processTextVertices(List<Resource> instances) {
		SparseVector[] ret = new SparseVector[instances.size()];
		for (int i = 0 ; i < ret.length; i++) {
			ret[i] = new SparseVector();
		}
		
		for (int key : textIndex2index2text.keySet()) {
			int lastIdx = ret[0].getLastIndex();
			
			if (textIndex2index2text.get(key).size() > 1) { // only do this is if we compare more than 1 node
				List<SparseVector> temp = TextUtils.computeTFIDF(new ArrayList<String>(textIndex2index2text.get(key).values()));
				
				if (normalize) {
					KernelUtils.normalize(temp.toArray(new SparseVector[1]));
				}
				
				// Add the computed feature vectors
				int i = 0;		
				for (int key2 : textIndex2index2text.get(key).keySet()) {
					ret[key2].addVector(temp.get(i));
					lastIdx = ret[key2].getLastIndex();
					i++;
				}
				
				// Set all feature vectors to the new lastIdx
				for (SparseVector fv : ret) {
					fv.setLastIndex(lastIdx);
				}			
			}
		}
		return ret;
	}
	
	
	public double[][] compute(RDFDataSet dataset, List<Resource> instances,
			List<Statement> blackList) {
		// TODO Auto-generated method stub
		return null;
	}

}
