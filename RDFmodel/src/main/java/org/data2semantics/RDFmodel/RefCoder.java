package org.data2semantics.RDFmodel;
import java.util.ArrayList;

/* Encodes sequences of integers, where the first number is always 0
 * and each subsequent number is at most one higher than the maximum so far.
 * It learns the frequency of each integer as it goes along.
 */

public class RefCoder extends Coder<Integer> {

	private ArrayList<Integer> _counts;
	private KT _new;
	private int _tot;
	
	public RefCoder(CLAccountant acc, String prefix) {
		init(acc, prefix);
		_new = new KT(acc, prefix, 2);
		_counts = new ArrayList<Integer>();
		_tot = 0;
	}
	
	public boolean isNew(int num) {
		int n = _counts.size();
		assert num<=n : "Refcoder: index out of range: encoding "+num+" while last is "+n+" :-(";
		return num==n; 
	}
	
	@Override public void encode(Integer num) { encode_test_new(num); }
	
	public boolean encode_test_new(int bi) {
		final double smoothing = 0.5;
		if (isNew(bi)) {
			_new.encode(1);
			_counts.add(0);
			return true;
		}
		_new.encode(0);
		int cnt = _counts.get(bi);
		use_bits(-Codes.lg((cnt+smoothing) / (_tot + smoothing*_counts.size())));
		_counts.set(bi, cnt+1);
		_tot++;
		return false;
	}

}
