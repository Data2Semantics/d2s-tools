package org.data2semantics.RDFmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.data2semantics.RDFmodel.Linkset.LinksetCoderFactory;

/* The following tree depicts in which order the various components of a Term are encoded.
 * Any component can be conditioned on after all its children have been encoded.
 * However there is no provision to condition on an *element* of a sequence after that
 * sequence has transmitted. There is one exception: during the second cycle, the objects
 * that are not part of the linkset need to be encoded. There is a special hack in the code
 * that allows conditioning on the linktype, objtype and predicate corresponding
 * to that object, which have already been transmitted in the first cycle.
 * 
 * Note that all coders may always condition on the URINode if they so desire.
 * 
 * URINode
 * Term
 *   LinkSet
 *     [ Link
 *         Predicate
 *         ObjType
 *         HasObj
 *         ObjIx 
 *       MoreLinks ]
 *   [ ObjIx
 *     MoreOBjs ]
 */

public class CoderContext {
	private CLAccountant       _acc;
	private Set<Integer>       _tbox;
	private Stack<CodeComponent<?>> _components = new Stack<CodeComponent<?>>();
	
	// -------------------------------------- URINode-------------------------------------------------
	public final CodeComponent<Integer> _c_urinode = new CodeComponent<Integer>(null, null);
	
	// GraphCoderSigBased
	// The graph coder has no internal state of its own, so it does not need to be conditioned.
	public final CodeComponent<RDFGraph> _c_graph;
	
	// Term
	// A term has no internal state of its own, so it does not need to be conditioned.
	public final CodeComponent<Term> _c_term = new CodeComponent<Term>("Term", Term.getFactory());

	// LinkSet
	// Can only usefully condition on URINode 
	public final LinksetCoderFactory    _f_linkset = Linkset.getFactory();
	public final CodeComponent<Linkset> _c_linkset;
		
	// Link
	// Can only usefully condition on URINode
	public final CodeComponent<Link>     _c_link = new CodeComponent<Link>("Link", Link.getFactory());
	
	// Predicate
	// Can only usefully condition on URINode
	public final CodeComponent<Integer> _c_pred;
	
	// Object type
	// Can usefully condition on: UriNode, Predicate
	public final CodeComponent<Integer> _c_objtype = new CodeComponent<Integer>("ObjType", KT.getFactory(3));

	// Does link have object?
	// Can usefully condition on: URINode, Predicate
	public final CodeComponent<Integer>  _c_hasobj = new CodeComponent<Integer>("HasObj", KT.getFactory(2));
	
	// Coders for the tbox objects. Separate for named and bnode objects.
	// Can usefully condition on: URINode, Predicate
	public final CodeComponent<Integer> _c_namedobj_t;
	public final CodeComponent<Integer> _c_bnodeobj_t;
	
	// Coders for the abox objects. Separate for named and bnode objects.
	// Can usefully condition on: URINode, Linkset, Predicate
	public final CodeComponent<Integer> _c_namedobj_a;
	public final CodeComponent<Integer> _c_bnodeobj_a;
		
	// More links? (in linkset)
	// Can usefully condition on: URINode.
	// Can NOT condition on the link:
	// - a linkset may be empty in which case there are no links
	// - since linkset ordering is undefined, so after which link the set is done is arbitrary 
	public final CodeComponent<Integer> _c_morelinks;
	
	// More objs with the same linktype? 
	// Can usefully condition on: URINode, LinkSet, Predicate, ObjType
	public final CodeComponent<Integer> _c_moreobjs; 
	
	// ================================================================================================
	
	public CoderContext(String name, int [] uri_map, Set<Integer> tbox, int nnamed, int nbnodes) {
		_acc  = new CLAccountant(name);
		_tbox = tbox;
				
		// instantiate all parameterized codermaps
		_c_graph      = new CodeComponent<RDFGraph>("Graph", GraphCoderSigBased.getFactory(uri_map));
		_c_pred       = new CodeComponent<Integer>("Predicates", SparseMultinomialCoder.getFactory(nnamed));
		_c_namedobj_t = new CodeComponent<Integer>("NamedObjT",  SparseMultinomialCoder.getFactory(nnamed),  _c_urinode, _c_pred);
		_c_bnodeobj_t = new CodeComponent<Integer>("BNodeObjT",  SparseMultinomialCoder.getFactory(nbnodes), _c_urinode, _c_pred);
		_c_namedobj_a = new CodeComponent<Integer>("NamedObjA",  SparseMultinomialCoder.getFactory(nnamed),  _c_urinode, _c_pred);
		_c_bnodeobj_a = new CodeComponent<Integer>("BNodeObjA",  SparseMultinomialCoder.getFactory(nbnodes), _c_urinode, _c_pred);
		_c_moreobjs   = new CodeComponent<Integer>("MoreObjs",  KT.getFactory(2));//, _c_pred, _c_objtype);
		_c_morelinks  =	new CodeComponent<Integer>("MoreLinks", KT.getFactory(2));
		
		_c_linkset    = new CodeComponent<Linkset>("Linkset", _f_linkset, _c_urinode);
	}
	
	public CLAccountant getResults() { return _acc; }
	public void use_bits(double L) { _acc.add(top_component().get_name(), L); }
	public void use_bits(String name, double L) { _acc.add(name, L); }
	public void spawned_new() { _acc.spawned_new(top_component().get_name()); }
	public boolean in_tbox(int obj_id) { return _tbox.contains(obj_id); }
	public Set<Integer> get_tbox() { return _tbox; }
	
	public void push_component(CodeComponent<?> component) { _components.push(component); }
	public void pop_component() { _components.pop(); }
	public CodeComponent<?> top_component() { return _components.peek(); }
	
	static class CodeComponent<T> implements Coder<T> {
		private String _name;
		private CoderFactory<T> _fact;
		private CodeComponent<?> [] _condition_on;
		private Map<List<Object>, Coder<T>> _coders = new HashMap<List<Object>, Coder<T>>();
		private T _last = null;
		
		public CodeComponent(String name, CoderFactory<T> fact, CodeComponent<?> ... condition_on) {
			_name = name;
			_fact = fact;
			_condition_on = condition_on;
		}
				
		public String get_name() { return _name; }
		
		public void set_conditional(T last) { _last = last; }
				
		@Override public void encode(CoderContext C, T obj) {
			C.push_component(this);
			List<Object> key = new ArrayList<Object>();
			for (CodeComponent<?> cm : _condition_on) key.add(cm._last);

			Coder<T> c = _coders.get(key);
			if (c==null) { c = _fact.build(); _coders.put(key, c); C.spawned_new(); }
			c.encode(C, obj);
			_last = obj;
			C.pop_component();
		}	
	}
}
