package org.data2semantics.RDFmodel;

public class SparseMultinomialCoder implements Coder<Integer> {
	
	private int _tot;
	private int [] _counts;
	private int _k;
	private KT  _new;
	private int _distinct_seen;
	
	public SparseMultinomialCoder(int k) {
		_tot = 0;
		_k = k;
		_counts = new int [k];
		for (int i=0; i<k; i++) _counts[i] = 0;
		_new = new KT(2);
		_distinct_seen = 0;
	}
		
	@Override public void encode(CoderContext C, Integer n) {
		if (_counts[n]==0) {
			// new symbol
			_new.encode(C, 1);
			C.use_bits(Codes.uniform(_k - _distinct_seen)); // uniform over all unseen symbols
			_distinct_seen++;
		} else {
			_new.encode(C, 0);
			double p = _counts[n] / (double)_tot;
			C.use_bits(-Codes.lg(p));	
		}
		_counts[n]++;
		_tot++;
	}
	
	public static CoderFactory<Integer> getFactory(int k) {
		return new SparseMultinomialFactory(k);
	}
	
	private static class SparseMultinomialFactory implements CoderFactory<Integer> {
		private int _k;
		
		public SparseMultinomialFactory(int k) { _k=k; }
		
		@Override public Coder<Integer> build() { return new SparseMultinomialCoder(_k); }
	}
}
