package org.data2semantics.RDFmodel;

public abstract class Coder<T> {

	protected CLAccountant _acc;
	protected String _prefix;
	
	protected void init(CLAccountant acc, String prefix) {
		_acc    = acc;
		_prefix = prefix;
	}
	
	protected String getPrefix() { return _prefix; }
	protected CLAccountant getAccountant() { return _acc; }
	
	protected void use_bits(double L) { _acc.add(_prefix, L); }
	protected void use_bits(String spec, double L) { _acc.add(_prefix+":"+spec, L); }
	
	public abstract void encode(T obj);
	
}
