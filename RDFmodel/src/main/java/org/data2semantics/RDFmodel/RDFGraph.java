package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/*
 * There are four types of objects, each of which will be associated with integer identifiers:
 * - predicates
 * - named concepts that occur in either the subject or object positions (can be properties)
 * - blank nodes
 * - literals
 * 
 * Code:
 * 
 * 1. Separate textual from structural information.
 *    Textual information consists of URI's of properties and named concepts (and possibly the types used for 
 *    the typed literals, but that has not been decided yet), and any text in the literals. 
 *    URIs are transformed into a tree stucture and then encoded using a general purpose text compressor, eg gzip.
 *    This representation makes it impossible to retain the ordering of the URIs, so they are sorted in 
 *    alphabetical order and are subsequently identified by their index in the sorted list.
 *    The literal text is concatenated and also compressed using a general purpose compressor; however here
 *    we can use any ordering; we will encode the literals in the same ordering as in which they appear in the
 *    code for the graph structure. This way we avoid having to encode which literal appears where in the graph 
 *    structure: we simply encode "a literal" and the decoder knows to pick the next one from the list.

 * 2. Structural information
 *    Here the idea is to associate every resource that appears in the subject position with a 'linkset',
 *    which is the set of predicate,object pairs associated with the subject.
 *    For all object in the ABox, the object itself should be replaced by the object type in the
 *    linkset. This can be one of the four resource types listed at the top. This is because conceptually,
 *    linksets should correspond to the various 'kinds' of concepts in the data.
 *    We decide which resources are replaced by their type using the heuristic that all objects in the ABox
 *    have only a small number of incoming edges compared to objects in the TBox. (I'd like to have a more
 *    sophisticated heuristic in the future. Ideally, should select the subset of resources such that overall
 *    codelength is minimized.)
 *    
 * Representation and data structures:
 * 
 * - Bijections between all four data types and the integers:
 *     props, named, blanks, lits
 * - An (object,type) pair is also represented by an integer with value object_ix * 4 + type
 * - A set 'TBox' of (object,type)s that are considered part of the TBox
 * - Each subject has an associated LinkSet with entries (predicate, (object, type)) or (predicate, -type-1) 
 *  
 * Ordering:
 * - named resources (named and predicates) are in alphabetical order
 * - literals are in increasing order in the _subj2links structure
 * - *new* bnodes are in increasing order in the _subj2links structure.
*/


public class RDFGraph {
	
	// mappings of value types to integer identifiers
	public List<URI>      _named;
	public List<Literal>  _literals;
	public int            _nbnodes, _npreds;
	
	public List<SortedMap<Integer,List<Integer>>> _b_subj2pred2obj;
	public List<SortedMap<Integer,List<Integer>>> _n_subj2pred2obj;
		
	public RDFGraph(RDFLoader L) {
		
		/* sort named terms and create mapping from old to new identifiers.
		 * We will also have to do the inverse mapping to construct the new data structure in the correct
		 * order, but that is done later on.
		 */
		_named = new ArrayList<URI>(L._named2ix.keySet());
		Collections.sort(_named, new ByString<URI>());
		NamedMap named_map = new NamedMap(_named, L._named2ix);
		
		// create literal map that will reorder literals in the order they appear in a graph traversal
		LiteralMap lit_map = new LiteralMap(L._ix2lit);
		
		// create BNode map
		BNodeMap bnode_map = new BNodeMap(L._bnodes2ix.size());
		
		IxMap [] mappers = new IxMap[TermType.SIZE];
		mappers[TermType.BNODE]   = bnode_map;
		mappers[TermType.LITERAL] = lit_map;
		mappers[TermType.NAMED]   = named_map;
		
		Set<Integer> preds = new HashSet<Integer>();

		// ------------------------------ construct the new graph structure ---------------------------
		
		_n_subj2pred2obj = new ArrayList<SortedMap<Integer,List<Integer>>>();
		_b_subj2pred2obj = new ArrayList<SortedMap<Integer,List<Integer>>>();
		
		Comparator<Entry<Integer,List<Integer>>> cmp = named_map.compareEntries();

		// the following code renumbers everything; while doing so, it also replaces null entries
		// in _(b|n)_subj2pred2obj (i.e., BNodes or URIs that do not appear as subjects) by empty lists for more
		// uniform handling later on
		
		for (int new_subj=0; new_subj < _named.size(); new_subj++) {
			int old_subj = L._named2ix.get(_named.get(new_subj));
			SortedMap<Integer,List<Integer>> links = L._n_subj2pred2obj.get(old_subj);
			if (links==null) links = new TreeMap<Integer,List<Integer>>();
			for (int pred_id : links.keySet()) preds.add(pred_id);
			_n_subj2pred2obj.add(renumber(links, mappers, cmp));
		}
		
		for (int new_subj=0; new_subj < L._b_subj2pred2obj.size(); new_subj++) {
			int old_subj = bnode_map.new2old(new_subj);
			SortedMap<Integer,List<Integer>> links = L._b_subj2pred2obj.get(old_subj);
			if (links==null) links = new TreeMap<Integer,List<Integer>>();
			for (int pred_id : links.keySet()) preds.add(pred_id);
			_b_subj2pred2obj.add(renumber(links, mappers, cmp));
		}
		
		_literals = lit_map.get_new_list();
		_nbnodes  = bnode_map.size();
		_npreds   = preds.size();
	}
	
	
	private static SortedMap<Integer,List<Integer>> renumber(Map<Integer,List<Integer>> old_links, 
			IxMap [] mappers,
			Comparator<Entry<Integer,List<Integer>>> cmp) {
		
		SortedMap<Integer,List<Integer>> new_links = new TreeMap<Integer,List<Integer>>();
		List<Entry<Integer, List<Integer>>> pred2objlist =
				new ArrayList<Entry<Integer,List<Integer>>>(old_links.entrySet());
		Collections.sort(pred2objlist, cmp);
		
		for (Entry<Integer,List<Integer>> entry : pred2objlist) {
			// renumber objects
			List<Integer> new_objs = new ArrayList<Integer>();
			for (int old_id : entry.getValue()) {
				int obj_type = TermType.id2type(old_id);
				int obj_ix   = TermType.id2ix(old_id);
				new_objs.add(TermType.ix2id(obj_type, mappers[obj_type].old2new(obj_ix)));
			}
			new_links.put(entry.getKey(), new_objs);
		}
		return new_links;
	}
	
	// named ix's are in arbitrary order; this function tests that new bnodes are always the smallest
	// unused number, and literals should appear in ascending order
	public void testSorted() {		
		int [] last = {0,0};
		for (int named_ix=0; named_ix < _named.size(); named_ix++) {
			for (List<Integer> objs : _n_subj2pred2obj.get(named_ix).values()) {
				for (int obj : objs) testSorted_check(last, obj);
			}
		}
		for (int bnode_ix=0; bnode_ix < _nbnodes; bnode_ix++) {
			testSorted_check(last, TermType.ix2id(TermType.BNODE,  bnode_ix));
			for (List<Integer> objs : _b_subj2pred2obj.get(bnode_ix).values()) {
				for (int obj : objs) testSorted_check(last, obj);
			}
		}
	}
	
	void testSorted_check(int [] counts, int id) {
		int ix = TermType.id2ix(id);
		if (id==TermType.BNODE) {
			assert ix <= counts[0] : "BNodes not in order!";
			if (ix == counts[0]) counts[0]++;
		} else if (id==TermType.LITERAL) {
			assert ix == counts[1] : "Literals not in order!";
			counts[1]++;
		}
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

interface IxMap {
	public int old2new(int old_ix);
}

class NamedMap implements IxMap {

	private int [] _old2new;
	
	public NamedMap(List<URI> new_ix2uri, Map<URI,Integer> uri2old_ix) {
		_old2new = new int[new_ix2uri.size()];
		for (int new_ix=0; new_ix<new_ix2uri.size(); new_ix++) {
			int old_ix = uri2old_ix.get(new_ix2uri.get(new_ix));
			_old2new[old_ix] = new_ix;
		}
	}
	
	public <T> Comparator<Entry<Integer,T>> compareEntries() {
		return new Comparator<Entry<Integer,T>>() {
			@Override
			public int compare(Entry<Integer, T> e1, Entry<Integer, T> e2) {
				return _old2new[e1.getKey()] - _old2new[e2.getKey()];
			}
		};
	}
	
	@Override
	public int old2new(int old_ix) { return _old2new[old_ix]; } 
}

class LiteralMap implements IxMap {
	
	private List<Literal> _old_list, _new_list;

	public List<Literal> get_new_list() { return _new_list; }
	
	public LiteralMap(List<Literal> old_list) {
		_old_list = old_list; 
		_new_list = new ArrayList<Literal>();
	}

	@Override
	public int old2new(int old_ix) {
		int new_ix = _new_list.size();
		_new_list.add(_old_list.get(old_ix));
		return new_ix;
	}
}

// stores the full bijection between old and new indices.
// new indices >= _nmapped are considered unmapped.
class BNodeMap implements IxMap {

	private int [] _old2new;
	private int [] _new2old;
	private int _nmapped; // the first _nmapped entries of _new2old have been mapped
	
	public int size() { return _nmapped; }
	
	public BNodeMap(int n) {
		_nmapped = 0;
		_old2new = new int[n];
		_new2old = new int[n];
		for (int i=0; i<n; i++) { 
			_new2old[i] = i;
			_old2new[i] = i;
		}
	}
	
	public int new2old(int new_ix) {
		if (new_ix==_nmapped) _nmapped++; // assign a random old bnode to this new one
		return _new2old[new_ix];
	}
	
	@Override
	public int old2new(int old_ix) {
		int new_ix = _old2new[old_ix];
		if (new_ix>=_nmapped) {
			// ran into an unmapped pair, cross-link old and new items appropriately
			int unused_old = _new2old[_nmapped];
			_old2new[unused_old] = new_ix;
			_new2old[_nmapped] = old_ix;
			_new2old[new_ix] = unused_old;
			_old2new[old_ix] = _nmapped;
			new_ix = _nmapped++;
		}
		return new_ix;
		
	}
	
}

