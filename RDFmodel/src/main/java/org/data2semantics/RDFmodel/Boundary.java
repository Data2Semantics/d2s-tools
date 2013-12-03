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
	
	public int [] get_uri_map(List<String> uris, StringTree root) {
		IndexMap<StringTree> node_map = new IndexMap<StringTree>();
		int [] map = new int[uris.size()];
		for (int i=0; i<uris.size(); i++) {
			String path = uris.get(i);
			StringTree pos = root;
			while (!contains(pos) && !path.isEmpty()) {
				pos = pos.lookup(path);
				if (pos==null) break;
				path = path.substring(pos.getEdgeLabelFromParent().length());
			}
			assert pos!=null : "URI "+uris.get(i)+" not encountered in the boundary";
			map[i] = node_map.map(pos);
		}
		return map;
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> items = new ArrayList<String>();
		for (StringTree node : this) items.add(node.pathFromRoot());
		Collections.sort(items);
		for (String p : items) { sb.append(p); sb.append('\n'); }
		return sb.toString();
	}
	
	
}
