package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;


public class RDFGraph {
	
	// mappings of value types to integer identifiers
	public List<URI>      _named;
	public List<Literal>  _literals;
	public int            _nbnodes, _npreds;
	
	public List<Term> _b_subj2pred2obj;
	public List<Term> _n_subj2pred2obj;
		
	public RDFGraph(RDFLoader L) {

		// ------------------------------ construct the new graph structure ---------------------------
		
		_n_subj2pred2obj = L._n_subj2pred2obj;
		_b_subj2pred2obj = L._b_subj2pred2obj;
		_npreds          = L._pred_ixs.size();
		
		// initialise null entries in the data structure
		for (int ix=0; ix<L._named2ix.size(); ix++) 
			if (_n_subj2pred2obj.get(ix)==null) _n_subj2pred2obj.set(ix,  new Term());
		
		for (int ix=0; ix<L._bnodes2ix.size(); ix++) 
			if (_b_subj2pred2obj.get(ix)==null) _b_subj2pred2obj.set(ix,  new Term());
		
		
		_named    = L._named2ix.invert();
		_nbnodes  = L._bnodes2ix.size();
		_literals = L._ix2lit;
	}
	
	

	public void printSomeStats() {
		System.out.println("#named terms :" + _named.size());
		System.out.println("#blank nodes :" + _nbnodes);
		System.out.println("#predicates  :" + _npreds);
		System.out.println("#subjects    :" + (_n_subj2pred2obj.size()+_b_subj2pred2obj.size()));
		System.out.println("#literals    :" + _literals.size());
	}
	
	public List<SortedMap<Integer, List<Integer>>> all_subjects_in_order() {
		List<SortedMap<Integer, List<Integer>>> all_subjects = new ArrayList<SortedMap<Integer, List<Integer>>>();
		all_subjects.addAll(_n_subj2pred2obj);
		all_subjects.addAll(_b_subj2pred2obj);
		return all_subjects;
	}
	
	// returns the number of incoming edges for each resource id
	public Map<Integer,Integer> num_incoming() {
		HashMap<Integer,Integer> count_links = new HashMap<Integer,Integer>();
		for (SortedMap<Integer, List<Integer>> links : all_subjects_in_order()) {
			for (List<Integer> objs : links.values()) {
				for (int obj : objs) {
					Integer num = count_links.get(obj);
					count_links.put(obj, num==null ? 1 : num+1);
				}
			}
		}
		return count_links;
	}

	public static CoderFactory<RDFGraph> getFactory(URIDistinguisher D) {
		return new RDFCoderFactory(D);
	}
	
	private static class RDFCoderFactory implements CoderFactory<RDFGraph> {
		private URIDistinguisher _D;
		public RDFCoderFactory(URIDistinguisher D) { _D = D; }
		@Override public Coder<RDFGraph> build() {
			return new GraphCoderSigBased(_D);
		};
	}
	
}


class ByString<T> implements Comparator<T> {
	@Override
	public int compare(T o1, T o2) {
		return o1.toString().compareTo(o2.toString()); 
	}
}

class ByLitText implements Comparator<Integer> {
	private List<Literal> _ix2lit;
	public ByLitText(List<Literal> ix2lit) { _ix2lit = ix2lit; }
	@Override
	public int compare(Integer ix1, Integer ix2) {
		return _ix2lit.get(ix1).toString().compareTo(_ix2lit.get(ix2).toString()); 
	}
}

class ByKey<T> implements Comparator<Entry<Integer, T>> {
	@Override
	public int compare(Entry<Integer, T> e1, Entry<Integer, T> e2) {
		return e1.getKey() - e2.getKey();
	}
}

class ByValue<T> implements Comparator<Entry<T, Integer>> {
	@Override
	public int compare(Entry<T,Integer> e1, Entry<T,Integer> e2) {
		return e1.getValue() - e2.getValue();
	}
}

