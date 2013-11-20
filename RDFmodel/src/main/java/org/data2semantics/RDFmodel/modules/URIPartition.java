package org.data2semantics.RDFmodel.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.data2semantics.RDFmodel.Boundary;
import org.data2semantics.RDFmodel.IndexMap;
import org.data2semantics.RDFmodel.RDFGraph;
import org.data2semantics.RDFmodel.RDFhelper;
import org.data2semantics.RDFmodel.StringTree;
import org.data2semantics.RDFmodel.TermType;
import org.data2semantics.RDFmodel.URIDistinguisher;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;
import org.openrdf.model.Literal;

public class URIPartition extends RDFhelper {

	private String _fn;
	private RDFGraph _G;
	private List<String> _uris;
	private List<Literal> _lits;
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
		for (String uristr : _uris) {
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
	
	@Out(name        = "Concepts",
		 description = "A list of resources that appear to represent important concepts")
	public List<String> concepts() {
		List<String> res = new ArrayList<String>();
		for (int id : _tbox) {
			int type = TermType.id2type(id);
			int ix   = TermType.id2ix(id);
			switch (type) {
			case TermType.NAMED: res.add(_uris.get(ix)); break;
			case TermType.BNODE: res.add("bnode(#"+ix+")"); break;
			case TermType.LITERAL: res.add(_lits.get(ix).stringValue()); break;
			default: assert false: "Unknown data type ("+type+").";
			}
		}
		return res;
	}
	
	@Main public void main() {
		_uris = new ArrayList<String>();
		_lits = new ArrayList<Literal>();
		_G = new RDFGraph(new RDFGraph.TripleFile(_fn)); // FIXME: URIs uninitialized!
		_ST = new StringTree(_uris);
		Pair<Boundary,Set<Integer>> pair = findBoundaryAndTBox(_G, _uris, _ST);
		_boundary = pair.getLeft();
		_tbox = pair.getRight();
	}
	

}
