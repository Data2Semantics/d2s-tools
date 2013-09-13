package org.data2semantics.RDFmodel;


public class LinkType {

	private int _pred_ix;
	private int _obj_type;

	public LinkType(int pred_ix, int obj_type) { 
		_pred_ix = pred_ix; _obj_type = obj_type;
	}
		
	public int getPredIx()  { return _pred_ix; }
	public int getObjType() { return _obj_type; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _obj_type;
		result = prime * result + _pred_ix;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LinkType other = (LinkType) obj;
		return _obj_type == other._obj_type && _pred_ix == other._pred_ix;
	}
	
}

// Learns the distribution on object types for each predicate separately 
class LinkTypeCoder extends Coder<LinkType> {
	
	private BundleMaker<Integer>    _pred_mapper = new BundleMaker<Integer>();
	private Coder<Bundle<Integer>>  _pred_bundle_coder;
	private Conditioner<Integer, Integer> _type_coder;

	public LinkTypeCoder(CLAccountant acc, String prefix, int nnamed) {
		init(acc, prefix);
		_pred_bundle_coder = new BundleRefCoder<Integer>(new UniformCoder(acc, prefix, nnamed));
		_type_coder = new Conditioner<Integer,Integer>(new KTFactory<Integer>(acc, prefix, TermType.SIZE));
	}
	
	@Override
	public void encode(LinkType lt) {
		Bundle<Integer> predb = _pred_mapper.bundle(lt.getPredIx());
		_pred_bundle_coder.encode(predb);
		_type_coder.get(predb).encode(lt.getObjType());
	}
}
