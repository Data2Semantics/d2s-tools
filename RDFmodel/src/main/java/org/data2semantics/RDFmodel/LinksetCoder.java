package org.data2semantics.RDFmodel;
import java.util.BitSet;
import java.util.Set;

/* Given a collection of links in the form of a Map<Integer,List<Integer>>,
 * this class constructs an object that represents this collection's signature: that is,
 * the set of (predicate, object_id) pairs for all objects in the tbox, and the set of (predicate, object_type)
 * pairs for all objects that are not.
 */

public class LinksetCoder extends Coder<Set<Link>> {
	private Coder<Link>    _link_coder;
	private Coder<Integer> _more;
	
	public LinksetCoder(CLAccountant acc, String prefix, Coder<Link> link_coder) {
		init(acc,prefix);
		_more = new KT(acc, prefix, 2);
		_link_coder = link_coder;
	}

	@Override
	public void encode(Set<Link> linkset) {
		for (Link l : linkset) { _more.encode(1); _link_coder.encode(l); }
		_more.encode(0);
	}
}

class SignatureFactory {
	private IndexMap<Link> _link_map = new IndexMap<Link>();
		
	public BitSet make_signature(Set<Link> linkset) {
		BitSet s = new BitSet();
		for (Link l : linkset) s.set(_link_map.map(l));
		return s;
	}
}


class LinkSetRefCoder extends Coder<Set<Link>> {
	private BundleMaker<Set<Link>>    _maker = new BundleMaker<Set<Link>>();
	private BundleRefCoder<Set<Link>> _brc;
	private SignatureFactory          _fact;
	
	@Override public CLAccountant getAccountant() { return _brc.getAccountant(); }
	@Override public String       getPrefix()     { return _brc.getPrefix(); }
	
	public LinkSetRefCoder(Coder<Set<Link>> basic_coder, SignatureFactory fact) {
		_brc  = new BundleRefCoder<Set<Link>>(basic_coder);
		_fact = fact;
	}

	@Override public void encode(Set<Link> ls) { 
		_brc.encode(_maker.bundle(ls, _fact.make_signature(ls))); 
	}
	
	public int size() { return _maker.size(); }
}
