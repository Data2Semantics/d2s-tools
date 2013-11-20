package org.data2semantics.RDFmodel;
import java.util.ArrayList;

/* Encodes sequences of integers, where the first number is always 0
 * and each subsequent number is at most one higher than the maximum so far.
 * It learns the frequency of each integer as it goes along. Moreover,
 * the achieved codelengths are order independent.
 */

public class RefCoder implements Coder<Integer> {

	private ArrayList<Integer> _counts;
	private int _tot;
	
	private final double _alpha = 0.5;
	private final double _beta  = 0.5;
	
	public RefCoder() {
		_counts = new ArrayList<Integer>();
		_tot = 0;
	}
	
	public boolean isNew(int num) {
		int n = _counts.size();
		assert num<=n : "Refcoder: index out of range: encoding "+num+" while last is "+n+" :-(";
		return num==n; 
	}
	
	@Override public void encode(CoderContext C, Integer num) { encode_test_new(C, num); }
	
	public boolean encode_test_new(CoderContext C, Integer num) {
		int old_cnt = isNew(num) ? 0 : _counts.get(num);
		double p = (old_cnt==0 ? (_counts.size()*_alpha + _beta) : (old_cnt-_alpha))/(_tot+_beta);
		C.use_bits(-Codes.lg(p));
		if (old_cnt==0) _counts.add(1); else _counts.set(num, old_cnt+1);
		_tot++;
		return old_cnt==0;
	}
}

class ObjRefCoder<T> implements Coder<T> {
	private Coder<T>    _basic_coder;
	private IndexMap<T> _map      = new IndexMap<T>();
	private RefCoder    _refcoder;
	
	public ObjRefCoder(Coder<T> basic_coder) {
		_basic_coder = basic_coder;
		_refcoder    = new RefCoder();
	}
	
	@Override public void encode(CoderContext C, T obj) {
		if (_refcoder.encode_test_new(C, _map.map(obj))) _basic_coder.encode(C, obj); 
	}
	
}


class SparseMultinomialCoder implements Coder<Integer> {
	private IndexMap<Integer> _map      = new IndexMap<Integer>();
	private RefCoder          _refcoder = new RefCoder();
	private int               _unseen;

	public SparseMultinomialCoder(int k) {
		_unseen = k;
	}
	
	@Override public void encode(CoderContext C, Integer n) {
		if (_refcoder.encode_test_new(C, _map.map(n))) {
			C.use_bits(Codes.uniform(_unseen));
			_unseen--;
		}
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
