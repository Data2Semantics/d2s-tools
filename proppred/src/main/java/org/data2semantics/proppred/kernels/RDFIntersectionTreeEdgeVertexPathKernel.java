package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;

public class RDFIntersectionTreeEdgeVertexPathKernel extends
		RDFIntersectionTreeEdgePathKernel {
	
	public RDFIntersectionTreeEdgeVertexPathKernel(int depth, boolean inference, boolean normalize) {
		super(depth, inference, normalize);
		this.pathLen = 2;
	}

	
	protected List<Integer> createPath(Statement stmt, List<Integer> path) {
		
		Integer key = uri2int.get(stmt.getPredicate());
		if (key == null) {
			key = new Integer(uri2int.size());
			uri2int.put(stmt.getPredicate(), key);
		}
		
		Integer key2 = uri2int.get(stmt.getObject());
		if (key2 == null) {
			key2 = new Integer(uri2int.size());
			uri2int.put(stmt.getObject(), key2);
		}
		
		List<Integer> newPath = new ArrayList<Integer>(path);
		newPath.add(key);
		newPath.add(key2);

		return newPath;
	}

}
