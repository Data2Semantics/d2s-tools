package org.data2semantics.RDFmodel;

public class UniformCoder extends Coder<Integer> {

	private final double _L;
	
	public UniformCoder(CLAccountant acc, String prefix, int n) {
		init(acc, prefix);
		_L = Codes.lg(n); 
	}
	
	@Override
	public void encode(Integer obj) { use_bits(_L); }

}
