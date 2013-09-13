package org.data2semantics.RDFmodel;
import java.util.ArrayList;

/* This class collects a bunch of coders and allows selecting between them on the
 * basis of conditioning information. The conditioning information has to be a bundle,
 * so that the conditioner does not need to use yet another IndexMap. When the coder 
 * factory is invoked, it also receives the conditioning information so it can decide what
 * kind of coder it should build.
 */

public class Conditioner<T,C> {

	private ArrayList<Coder<T>> _coders = new ArrayList<Coder<T>>();
	private CoderFactory<T,C> _fact;
	
	public Conditioner(CoderFactory<T,C> fact) { _fact = fact; }
	
	public Coder<T> get(Bundle<C> cb) {
		if (cb==null) return _fact.construct(null);
		int i = cb.getInt();
		while (_coders.size() <= i) _coders.add(null);
		Coder<T> c = _coders.get(i);
		if (c==null) { 
			c = _fact.construct(cb.getObj());
			_coders.set(i, c);
		}
		return c;
	}
	
}
