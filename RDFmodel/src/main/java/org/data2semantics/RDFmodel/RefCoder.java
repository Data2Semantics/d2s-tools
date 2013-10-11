package org.data2semantics.RDFmodel;
import java.util.ArrayList;

/* Encodes sequences of integers, where the first number is always 0
 * and each subsequent number is at most one higher than the maximum so far.
 * It learns the frequency of each integer as it goes along.
 */

public class RefCoder implements Coder<Integer> {

	private ArrayList<Integer> _counts;
	private KT _new;
	private int _tot;
	
	public RefCoder() {
		_new = new KT(2);
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
		final double smoothing = 0.5;
		if (isNew(num)) {
			_counts.add(0);
			_new.encode(C, 1);
			return true;
		} 
		_new.encode(C, 0);
		int cnt = _counts.get(num);
		C.use_bits(-Codes.lg((cnt+smoothing) / (_tot + smoothing*_counts.size())));
		_counts.set(num, cnt+1);
		_tot++;
		return false;
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
