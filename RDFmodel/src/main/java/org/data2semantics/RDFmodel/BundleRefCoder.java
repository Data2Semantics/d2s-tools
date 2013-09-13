package org.data2semantics.RDFmodel;


public class BundleRefCoder<T> extends Coder<Bundle<T>> {
	
	private RefCoder _rc;
	private Coder<T> _basic_coder;
	
	public BundleRefCoder(Coder<T> basic_coder) {
		CLAccountant acc = basic_coder.getAccountant();
		String prefix = basic_coder.getPrefix();
		init(acc, prefix);
		_rc = new RefCoder(acc, prefix+":refs");
		_basic_coder = basic_coder;
	}
		
	@Override
	public void encode(Bundle<T> b) {
		if (_rc.encode_test_new(b.getInt())) _basic_coder.encode(b.getObj());
	}
}

class ObjRefCoder<T> extends Coder<T> {
	private BundleMaker<T>    _maker = new BundleMaker<T>();
	private BundleRefCoder<T> _brc;
	
	@Override public CLAccountant getAccountant() { return _brc.getAccountant(); }
	@Override public String       getPrefix()     { return _brc.getPrefix(); }
	
	public ObjRefCoder(Coder<T> basic_coder) {
		_brc = new BundleRefCoder<T>(basic_coder); 
	}

	@Override public void encode(T obj) { _brc.encode(_maker.bundle(obj)); }
	public int size() { return _maker.size(); }
}