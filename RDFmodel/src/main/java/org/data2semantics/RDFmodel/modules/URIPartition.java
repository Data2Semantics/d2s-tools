package org.data2semantics.RDFmodel.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.data2semantics.RDFmodel.Boundary;
import org.data2semantics.RDFmodel.IndexMap;
import org.data2semantics.RDFmodel.RDFGraph;
import org.data2semantics.RDFmodel.RDFhelper;
import org.data2semantics.RDFmodel.StringTree;
import org.data2semantics.RDFmodel.URIDistinguisher;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;
import org.openrdf.model.URI;

public class URIPartition extends RDFhelper {

	private String _fn;
	private RDFGraph _G;
	private StringTree _ST;
	private Set<Integer> _tbox;
	private Boundary _boundary;
	
	public URIPartition(@In(name="file") String filename) {
		_fn = filename;
	}
	
	@Out(name        = "URI partition",
	     description = "The partition cell sizes for a partition of URI's into groups that appear to be of similar type.")
	public List<Integer> partition() {
		URIDistinguisher D = new URIDistinguisher(_boundary, _ST);
		IndexMap<StringTree> map = new IndexMap<StringTree>();
		List<List<String>> res = new ArrayList<List<String>>();
		for (URI uri : _G._named) {
			String uristr = uri.stringValue();
			StringTree st = D.get_node(uristr);
			int cell = map.map(st);
			if (cell == res.size()) res.add(new ArrayList<String>());
			res.get(cell).add(uristr);
		}
		List<Integer> sizes = new ArrayList<Integer>();
		for (List<String> cell : res) {
			sizes.add(cell.size());
		}
		return sizes;
	}
	
	
	@Main public void main() {
		_G = load(_fn);
		_ST = new StringTree(_G._named);
		_tbox = tbox_heuristic_most_incoming(_G, 8);
		_boundary = findBestBoundary(_G, _tbox, _ST);
	}
	

}
