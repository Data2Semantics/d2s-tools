package org.data2semantics.RDFmodel;
import java.util.Set;


public class Link {
	
	public LinkType _lt;
	public int _obj_ix;
	
	public Link(LinkType lt, int obj_ix) { _lt = lt; _obj_ix = obj_ix; }
	
	public Link(Set<Integer> tbox, int pred_ix, int obj_id) {
		int obj_type = TermType.id2type(obj_id);
		int obj_ix   = TermType.id2ix(obj_id);
		_lt = new LinkType(pred_ix, obj_type);
		_obj_ix = tbox.contains(obj_id) ? obj_ix : -1;
	}
	
	public LinkType getLinkType() { return _lt; }
	public int getObjIx() { return _obj_ix; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_lt == null) ? 0 : _lt.hashCode());
		result = prime * result + _obj_ix;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Link other = (Link) obj;
		return other._lt.equals(_lt) && _obj_ix == other._obj_ix;
	}
}

class LinkCoder extends Coder<Link> {
	private Conditioner<Integer, LinkType> _obj_coder;
	private Conditioner<Integer, LinkType> _obj_in_tbox;
	private BundleMaker<LinkType>          _lt_mapper;
	private Coder<Bundle<LinkType>>        _linktype_coder;
	
	public LinkCoder(CLAccountant acc, String prefix, int nnamed, 
					 BundleMaker<LinkType> lt_mapper, Conditioner<Integer,LinkType> obj_coder) {
		init(acc, prefix);
		_linktype_coder = new BundleRefCoder<LinkType>(new LinkTypeCoder(acc, "linktype", nnamed));
		_obj_coder      = obj_coder;
		_lt_mapper      = lt_mapper;
		_obj_in_tbox    = new Conditioner<Integer,LinkType>(new KTFactory<LinkType>(acc, "in_tbox?", 2));
	}
	
	@Override
	public void encode(Link lnk) {
		Bundle<LinkType> ltb = _lt_mapper.bundle(lnk.getLinkType());
		_linktype_coder.encode(ltb);
		boolean in_tbox = lnk.getObjIx()!=-1;
		_obj_in_tbox.get(ltb).encode(in_tbox ? 1 : 0);
		if (in_tbox) _obj_coder.get(ltb).encode(lnk.getObjIx());
	}
}
