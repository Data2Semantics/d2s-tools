package org.data2semantics.RDFmodel;

public class KT extends Coder<Integer> {

	private int _tot;
	private int [] _counts;
	private int _k;
	
	public KT(CLAccountant acc, String prefix, int k) {
		init(acc, prefix);
		_tot = 0;
		_k = k;
		_counts = new int [k];
		for (int i=0; i<k; i++) _counts[i] = 0;
	}
	
	@Override
	public void encode(Integer n) {
		final double smoothing = 0.5; // Hand-tweaked
		double p = (_counts[n]+smoothing)/(_tot + smoothing*_k);
		_counts[n]++;
		_tot++;
		use_bits(-Codes.lg(p));
	}
	
}

// Coder for multinomial data, but handles unobserved symbols in a more sophisticated manner:
// it is efficient if the used symbols are a sparse subset of the alphabet.
class MultinomialCoder extends Coder<Integer> {

	private int _tot;
	private int [] _counts;
	private int _k;
	private KT  _new;
	private int _distinct_seen;
	
	public MultinomialCoder(CLAccountant acc, String prefix, int k) {
		init(acc, prefix);
		_tot = 0;
		_k = k;
		_counts = new int [k];
		for (int i=0; i<k; i++) _counts[i] = 0;
		_new = new KT(acc, prefix, 2);
		_distinct_seen = 0;
	}
	
	@Override
	public void encode(Integer n) {
		if (_counts[n]==0) {
			// new symbol
			_new.encode(1);
			use_bits(Codes.uniform(_k - _distinct_seen)); // uniform over all unseen symbols
			_distinct_seen++;
		} else {
			_new.encode(0);
			double p = _counts[n] / (double)_tot;
			use_bits(-Codes.lg(p));	
		}
		_counts[n]++;
		_tot++;
	}
	
}


class KTFactory<C> implements CoderFactory<Integer, C> {
	private CLAccountant _acc;
	private String _prefix;
	private int _k;
	
	public KTFactory(CLAccountant acc, String prefix, int k) {
		_acc = acc; _prefix = prefix; _k = k;
	}
	
	@Override
	public Coder<Integer> construct(C sideinfo) {
		return new KT(_acc, _prefix, _k);
	}
	
}