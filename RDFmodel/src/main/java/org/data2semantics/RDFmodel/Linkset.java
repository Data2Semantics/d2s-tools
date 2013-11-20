package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class Linkset extends HashSet<Link> {
	private static final long serialVersionUID = 1L;

	public Linkset(Set<Integer> tbox, Map<Integer, SortedSet<Integer>> edge_map) {
		assert edge_map!=null : "edge map lists may be empty but not null";
		for (Map.Entry<Integer, SortedSet<Integer>> pred : edge_map.entrySet()) {
			for (int obj_id : pred.getValue()) {
				add(new Link(pred.getKey(),
							 TermType.id2type(obj_id),
							 tbox.contains(obj_id) ? TermType.id2ix(obj_id) : -1));
			}
		}
    }
	
	public static class LinksetCoderFactory implements CoderFactory<Linkset>  {
		private IndexMap<Link> _link_map = new IndexMap<Link>();
		private List<LinksetCoder> _coders = new ArrayList<LinksetCoder>();
		
		/* Implements a bijection between BitSets and Linksets.
		 * While we could use the Linkset itself as a key in the _map hash below,
		 * BitSets are more efficient.
		 */ 
		private BitSet getKey(Linkset bs) {
			BitSet key = new BitSet();
			for (Link l : bs) key.set(_link_map.map(l));
			return key;
		}
		
		public int get_nlinks() { return _link_map.size(); }
		public List<LinksetCoder> get_coders() { return _coders; }
		
		@Override public Coder<Linkset> build() { 
			LinksetCoder c = new LinksetCoder();
			_coders.add(c);
			return c;
		}
		
		public class LinksetCoder implements Coder<Linkset> {
			private BasicLinksetCoder _lsc = new BasicLinksetCoder();
			private IndexMap<BitSet>  _map = new IndexMap<BitSet>();
			private RefCoder          _ref = new RefCoder();
		
			@Override public void encode(CoderContext C, Linkset obj) {
				int ix = _map.map(getKey(obj));
				if (_ref.encode_test_new(C, ix)) _lsc.encode(C, obj);
			}
			public int get_nlinksets() { return _map.size(); }
		}
	
		private static class BasicLinksetCoder implements Coder<Linkset> {
			@Override public void encode(CoderContext C, Linkset linkset) {
				for (Link lnk : linkset) {
					C._c_morelinks.encode(C, 1); // at least one more link
					C._c_link.encode(C,  lnk); 
				}
				C._c_morelinks.encode(C, 0); // no more links after the last one
				
				/* We redundantly encoded a set as a sequence;
				 * by combining all code words that represent the same set using a permutation of 
				 * the sequence we can reclaim some bits.
				 */
				C.use_bits("Linkset bonus", -Codes.lgfac(linkset.size()));
			}
		}
		
	}
	
	public static LinksetCoderFactory getFactory() {
		return new LinksetCoderFactory(); 
	}
}
	