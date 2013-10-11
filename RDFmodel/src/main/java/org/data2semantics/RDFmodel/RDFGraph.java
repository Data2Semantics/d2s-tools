package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


public class RDFGraph {
	public int _nnamed, _nbnodes, _nlits, _npreds, _ntriples;
	
	public List<Term> _b_subj2pred2obj;
	public List<Term> _n_subj2pred2obj;
		
	public RDFGraph(RDFLoader L) {

		_ntriples        = L._ntriples;
		_n_subj2pred2obj = L._n_subj2pred2obj;
		_b_subj2pred2obj = L._b_subj2pred2obj;
		_npreds          = L._pred_ixs.size();
		
		// initialise null entries in the data structure
		for (int ix=0; ix<L._named2ix.size(); ix++) 
			if (_n_subj2pred2obj.get(ix)==null) _n_subj2pred2obj.set(ix,  new Term());
		
		for (int ix=0; ix<L._bnodes2ix.size(); ix++) 
			if (_b_subj2pred2obj.get(ix)==null) _b_subj2pred2obj.set(ix,  new Term());
		
		_nnamed  = L._named2ix.size();
		_nbnodes = L._bnodes2ix.size();
		_nlits   = L._ix2lit.size();
	}
	
	

	public void printSomeStats() {
		System.out.println("#triples     :"+ _ntriples);
		System.out.println("#uris        :" + _nnamed);
		System.out.println("#blank nodes :" + _nbnodes);
		System.out.println("#predicates  :" + _npreds);
		System.out.println("#subjects    :" + (_n_subj2pred2obj.size()+_b_subj2pred2obj.size()));
		System.out.println("#literals    :" + _nlits);
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
}


