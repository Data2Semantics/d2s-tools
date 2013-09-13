package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class Boundary extends HashSet<StringTree> {

	static final long serialVersionUID = 1910746224041087621L;

	public Boundary() {}
	public Boundary(Boundary other) { addAll(other); }
	
	public Boundary expand(StringTree node) {
		assert !node.isLeaf() : "Cannot expand a leaf node in the boundary";
		Boundary nw = new Boundary(this);
		nw.remove(node);
		nw.addAll(node.getChildren());
		return nw;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> items = new ArrayList<String>();
		for (StringTree node : this) items.add(node.pathFromRoot());
		Collections.sort(items);
		for (String p : items) { sb.append(p); sb.append('\n'); }
		return sb.toString();
	}
	
	
}
