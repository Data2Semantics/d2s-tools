package org.data2semantics.RDFmodel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;


public class RDFGraph {
	public int _nnamed   = 0;
	public int _nbnodes  = 0;
	public int _nlits    = 0;
	public Set<Integer> _preds = new HashSet<Integer>();
	public int _ntriples = 0;
	
	public List<Term> _n_subj2pred2obj = new SoftList<Term>();
	public List<Term> _b_subj2pred2obj = new SoftList<Term>();
	
	private void add_term(int id) {
		int type = TermType.id2type(id);
		int ix   = TermType.id2ix(id);
		switch (type) {
		case TermType.NAMED:   if (ix >= _nnamed)  _nnamed  = ix+1; break;
		case TermType.BNODE:   if (ix >= _nbnodes) _nbnodes = ix+1; break;
		case TermType.LITERAL: if (ix >= _nlits)   _nlits   = ix+1; break;
		}
	}

	private void add_triple(List<Integer> triple) {
		int subj_id = triple.get(0), pred_id = triple.get(1), obj_id = triple.get(2);
		add_term(subj_id);
		add_term(pred_id); _preds.add(pred_id);
		add_term(obj_id);
		_ntriples++;
		
		// insert this link into the data structure
		int subj_type = TermType.id2type(subj_id);
		int subj_ix   = TermType.id2ix(subj_id);
		List<Term> terms = subj_type==TermType.NAMED ? _n_subj2pred2obj : _b_subj2pred2obj;
		Term t = terms.get(subj_ix);
		if (t==null) { t = new Term(); terms.set(subj_ix, t); }
		SortedSet<Integer> objs = t.get(pred_id);
		if (objs==null) { objs = new TreeSet<Integer>(new TermType.IdComparator()); t.put(pred_id,  objs); }
		objs.add(obj_id);
	}
	
	public RDFGraph(Iterable<List<Integer>> triples) { 
		for (List<Integer> triple : triples) add_triple(triple);
		
		// initialise null entries in the data structure
		for (int ix=0; ix<_nnamed; ix++) 
			if (_n_subj2pred2obj.get(ix)==null) _n_subj2pred2obj.set(ix,  new Term());
		
		for (int ix=0; ix<_nbnodes; ix++) 
			if (_b_subj2pred2obj.get(ix)==null) _b_subj2pred2obj.set(ix,  new Term());
	}
	
	// this version of the class pre-loads all triples, then permutes the whole bunch randomly
	// and provides iteration access to that
	public static class PermutedTripleFile implements Iterable<List<Integer>> {
		
		private List<List<Integer>> _triples = new ArrayList<List<Integer>>();
		private int _nnamed, _nbnodes, _nlits;
		
		private int [] _named_permutation;
		private int [] _bnode_permutation;
		private int [] _literal_permutation;
		
		private void add_term(int id) {
			int type = TermType.id2type(id);
			int ix   = TermType.id2ix(id);
			switch (type) {
			case TermType.NAMED: if (ix >= _nnamed) _nnamed = ix+1; break;
			case TermType.BNODE: if (ix >= _nbnodes) _nbnodes = ix+1; break;
			case TermType.LITERAL: if (ix >= _nlits) _nlits = ix+1; break;
			}
		}
		
		private static int [] create_permutation(int n) {
			int [] a = new int[n];
			a[0] = 0;
			for (int i=0; i<n; i++) {
				int k;
				k = (int)(Math.random()*(i+1));
				if (k<i) a[i] = a[k];
				a[k] = i;
			}
			return a;
		}
		
		public PermutedTripleFile(String fn) {
			
			_nnamed = _nlits = _nbnodes = 0;
			
			System.out.println("Reading...");
			
			for (List<Integer> triple: new TripleFile(fn)) {
			 	_triples.add(triple);
			 	add_term(triple.get(0));
			 	add_term(triple.get(1));
			 	add_term(triple.get(2));
			}
			
			System.out.println("Creating permutations...");
			
			_named_permutation   = create_permutation(_nnamed);
			_bnode_permutation   = create_permutation(_nbnodes);
			_literal_permutation = create_permutation(_nlits);
			
			System.out.println("PermutedTripleFile constructed!");
		}

		@Override public Iterator<List<Integer>> iterator() {
			return new PermutedIterator();
		}
		
		class PermutedIterator implements Iterator<List<Integer>> {
			
			private int map(int id) {
				int type = TermType.id2type(id);
				int ix   = TermType.id2ix(id);
				int [] perm =
						type==TermType.NAMED ? _named_permutation :
						type==TermType.BNODE ? _bnode_permutation :
						_literal_permutation;
				return TermType.ix2id(type,  perm[ix]);
			}
			
			@Override public boolean hasNext() { return !_triples.isEmpty(); }
			
			@Override public List<Integer> next() {
				List<Integer> triple = _triples.remove(_triples.size()-1);
				/*
				int k = (int)(Math.random()*_triples.size());
				List<Integer> triple = _triples.get(k);
				List<Integer> last = _triples.remove(_triples.size()-1);
				if (last!=triple) _triples.set(k, last);
				*/
				List<Integer> mapped_triple = new ArrayList<Integer>();
				mapped_triple.add(map(triple.get(0)));
				mapped_triple.add(map(triple.get(1)));
				mapped_triple.add(map(triple.get(2)));
				return mapped_triple;
			}

			@Override public void remove() { throw new UnsupportedOperationException("Cannot remove triples"); }
		}
		
	}
	
	
	public static class TripleFile implements Iterable<List<Integer>> {
		
		private String _fn;
	
		public TripleFile(String fn) { _fn = fn; }
		
		private static class MyIterator implements Iterator<List<Integer>> { 
			private BufferedReader _in;
			private String _line;
	
			public MyIterator(String fn) {
				try {
					_in = new BufferedReader(new FileReader(fn));
					cache_line();
				} catch (IOException e) {
					throw new RuntimeException("Cannot read triple file '"+fn+"'", e);
				}
			}
			
			private void cache_line() {
				try {
					_line = _in.readLine();
					if (_line==null) { _in.close(); }
				} catch (IOException e) {
					throw new RuntimeException("Cannot read line in triple file", e);
				}
			}
			
			@Override public boolean hasNext() { return _line!=null; }
				
			@Override public List<Integer> next() { 
				String line = _line; 
				cache_line();
				String [] parts = line.split("\\s+");
				assert parts.length==3 : "Not a triple!";
				int subj_id = Integer.parseInt(parts[0]);
				int pred_id = Integer.parseInt(parts[1]);
				int obj_id  = Integer.parseInt(parts[2]);
				return Arrays.asList(subj_id, pred_id, obj_id);
			}
			
			@Override public void remove() { throw new UnsupportedOperationException("Cannot remove triples"); }
		}
		
		@Override public Iterator<List<Integer>> iterator() { return new MyIterator(_fn); }
		
	}

	

	public void printSomeStats() {
		System.out.println("#triples     :"+ _ntriples);
		System.out.println("#uris        :" + _nnamed);
		System.out.println("#blank nodes :" + _nbnodes);
		System.out.println("#predicates  :" + _preds.size());
		System.out.println("#subjects    :" + (_n_subj2pred2obj.size()+_b_subj2pred2obj.size()));
		System.out.println("#literals    :" + _nlits);
	}
	
	public List<SortedMap<Integer, SortedSet<Integer>>> all_subjects_in_order() {
		List<SortedMap<Integer, SortedSet<Integer>>> all_subjects = new ArrayList<SortedMap<Integer, SortedSet<Integer>>>();
		all_subjects.addAll(_n_subj2pred2obj);
		all_subjects.addAll(_b_subj2pred2obj);
		return all_subjects;
	}
	
	// returns the number of incoming edges for each resource id
	public Map<Integer,Integer> num_incoming() {
		HashMap<Integer,Integer> count_links = new HashMap<Integer,Integer>();
		for (SortedMap<Integer, SortedSet<Integer>> links : all_subjects_in_order()) {
			for (SortedSet<Integer> objs : links.values()) {
				for (int obj : objs) {
					Integer num = count_links.get(obj);
					count_links.put(obj, num==null ? 1 : num+1);
				}
			}
		}
		return count_links;
	}
}


