package org.data2semantics.RDFmodel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

// A TermCoder encodes all triples with this term in the subject position.

public class TermCoder extends Coder<SortedMap<Integer,List<Integer>>> {

	private Set<Integer>                   _tbox;
	
	private Conditioner<Integer, LinkType> _more;
	private Conditioner<Integer, LinkType> _obj_coder;
	private ObjRefCoder<Link>              _link_coder;
	
	private Coder<Set<Link>>               _linkset_coder;
	
	private BundleMaker<LinkType> 		   _linktype_mapper;

	public TermCoder(CLAccountant acc, Set<Integer> tbox, 
					Conditioner<Integer, LinkType> more,
					Conditioner<Integer,LinkType>  obj_coder,
					Coder<Set<Link>>               linkset_coder,
					ObjRefCoder<Link>              link_coder,
					BundleMaker<LinkType> 		   linktype_mapper) {
		init(acc, null);
		_tbox            = tbox;
		_more            = more;
		_obj_coder       = obj_coder;
		_linkset_coder   = linkset_coder;
		_link_coder      = link_coder;
		_linktype_mapper = linktype_mapper;
	}
	
	public Set<Link> make_linkset(Map<Integer,List<Integer>> links) {
		HashSet<Link> ls = new HashSet<Link>();
		for (Map.Entry<Integer,List<Integer>> pred : links.entrySet()) {
			for (int obj_id : pred.getValue()) {
				ls.add(new Link(_tbox, pred.getKey(), obj_id));
			}
		}
		return ls;
    }
	
	@Override
	public void encode(SortedMap<Integer, List<Integer>> links) {
		
		// encode the signature
		_linkset_coder.encode(make_linkset(links));
		
		// encode objects outside of the signature
		Bundle<LinkType> ltb = null;
		for (Entry<Integer,List<Integer>> map : links.entrySet()) {
			int pred_ix = map.getKey();
			for (int obj_id: map.getValue()) {
				if (!_tbox.contains(obj_id)) {
					Bundle<LinkType> next_ltb = _linktype_mapper.bundle(new LinkType(pred_ix, TermType.id2type(obj_id)));
					if (ltb != null) _more.get(ltb).encode(next_ltb.equals(ltb) ? 1 : 0);
					ltb = next_ltb;
					_obj_coder.get(next_ltb).encode(TermType.id2ix(obj_id));
				}
			}
		}
		if (ltb!=null) _more.get(ltb).encode(0);
	}
	
	public int num_links()      { return _link_coder.size(); }
	// public int num_linksets()   { return _linkset_coder.num_linksets(); }
}

class TermCoderFactory implements CoderFactory<SortedMap<Integer,List<Integer>>,StringTree> {

	private CLAccountant                   _acc;
	private Set<Integer>                   _tbox;
	private SignatureFactory               _fact = new SignatureFactory();
	private Conditioner<Integer, LinkType> _obj_coder;
	private Conditioner<Integer, LinkType> _more;
	private ObjRefCoder<Link>              _link_coder;
	private Coder<Set<Link>>               _linkset_basic_coder;
	private BundleMaker<LinkType> 		   _linktype_mapper  = new BundleMaker<LinkType>();
	
	public TermCoderFactory(CLAccountant acc, Set<Integer> tbox, int nnamed, int nbnodes) {
		_acc                 = acc;
		_tbox                = tbox;
		_obj_coder           = new Conditioner<Integer,LinkType>(new ObjCoderFactory(acc, "object", nnamed));
		_more                = new Conditioner<Integer,LinkType>(new KTFactory<LinkType>(acc, "more", 2));
		_link_coder          = new ObjRefCoder<Link>(new LinkCoder(acc, "links", nnamed, _linktype_mapper, _obj_coder));
		_linkset_basic_coder = new LinkSetRefCoder(new LinksetCoder(acc, "linksets", _link_coder), _fact);
	}
	
	@Override
	public Coder<SortedMap<Integer, List<Integer>>> construct(StringTree conditional) {
		Coder<Set<Link>> linkset_coder = new ObjRefCoder<Set<Link>>(_linkset_basic_coder);
		return new TermCoder(_acc, _tbox, _more, _obj_coder, linkset_coder, _link_coder, _linktype_mapper);
	}
	
}


/* Normally, most objects are not part of the link and are encoded separately.
 * For simplicity, this coder is used for both objects that are in a link and
 * separate objects, but it should be optimized for the latter.
 */
class ObjCoderFactory implements CoderFactory<Integer,LinkType> {
	
	private CLAccountant _acc;
	private String _prefix;
	private int _nnamed;
	
	public ObjCoderFactory(CLAccountant acc, String prefix, int nnamed) {
		_acc    = acc;
		_prefix = prefix;
		_nnamed = nnamed; 
	}
	
	@Override
	public Coder<Integer> construct(LinkType conditional) {
		Coder<Integer> c = null;
		switch (conditional.getObjType()) {
		case TermType.NAMED:   c = new MultinomialCoder(_acc, _prefix, _nnamed); break;
		case TermType.BNODE:   c = new RefCoder(_acc, _prefix); break;
		case TermType.LITERAL: c = new DummyCoder<Integer>(); break;
		default: assert false: "Unknown type"; 
		}
		return c;
	}

}

