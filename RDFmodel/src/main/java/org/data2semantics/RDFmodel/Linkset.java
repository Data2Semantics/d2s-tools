package org.data2semantics.RDFmodel;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Linkset extends HashSet<Link> {
	private static final long serialVersionUID = 1L;

	public Linkset(Set<Integer> tbox, Map<Integer,List<Integer>> edge_map) {
		assert edge_map!=null : "edge map lists may be empty but not null";
		for (Map.Entry<Integer,List<Integer>> pred : edge_map.entrySet()) {
			for (int obj_id : pred.getValue()) {
				LinkType lt = new LinkType(pred.getKey(), TermType.id2type(obj_id));
				add(new Link(lt, tbox.contains(obj_id) ? TermType.id2ix(obj_id) : -1));
			}
		}
    }
	
	
	public static CoderFactory<Linkset> getFactory() {
		return new CoderFactory<Linkset>() {
			
			private IndexMap<Link> _link_map = new IndexMap<Link>();

			/* Implements a bijection between BitSets and Linksets.
			 * While we could use the Linkset itself as a key in the _map hash below,
			 * BitSets are more efficient.
			 */ 
			private BitSet getKey(Linkset bs) {
				BitSet key = new BitSet();
				for (Link l : bs) key.set(_link_map.map(l));
				return key;
			}
			
			@Override public Coder<Linkset> build() {
				return new Coder<Linkset>() {
					private BasicLinksetCoder _lsc = new BasicLinksetCoder();
					private IndexMap<BitSet>  _map = new IndexMap<BitSet>();
					private RefCoder          _ref = new RefCoder();
					@Override public void encode(CoderContext C, Linkset obj) {
						int ix = _map.map(getKey(obj));
						if (_ref.encode_test_new(C, ix)) _lsc.encode(C, obj);
					}
				};
			}
		};
	}

	private static class BasicLinksetCoder implements Coder<Linkset> {
		@Override public void encode(CoderContext C, Linkset linkset) {
			for (Link lnk : linkset) {
				C._c_morelinks.encode(C, 1);
				C._c_link.encode(C,  lnk); 
			}
			C._c_morelinks.encode(C, 0);
			
			/* We redundantly encoded a set as a sequence;
			 * by combining all code words that represent the same set using a permutation of 
			 * the sequence we can reclaim some bits.
			 */
			C.use_bits("Linkset bonus", -Codes.lgfac(linkset.size()));
		}
	}
}
