package org.data2semantics.RDFmodel;

public class KT implements Coder<Integer> {
	private int _tot;
	private int [] _counts;
	private int _k;
	
	public KT(int k) {
		_tot = 0;
		_k = k;
		_counts = new int [k];
		for (int i=0; i<k; i++) _counts[i] = 0;
	}
	
	@Override public void encode(CoderContext C, Integer n) {
		final double smoothing = 0.5; // Hand-tweaked
		double p = (_counts[n]+smoothing)/(_tot + smoothing*_k);
		_counts[n]++;
		_tot++;
		C.use_bits(-Codes.lg(p));
	}
	
	public static CoderFactory<Integer> getFactory(int k) { return new KTFactory(k); }
		
	private static class KTFactory implements CoderFactory<Integer> {
		private int _k;
		public KTFactory(int k) { _k = k; }
		@Override public Coder<Integer> build() { return new KT(_k); }
	}
	
}
