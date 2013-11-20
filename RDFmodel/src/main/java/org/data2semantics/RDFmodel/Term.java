package org.data2semantics.RDFmodel;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;

// A TermCoder encodes all triples with this term in the subject position.


public class Term extends TreeMap<Integer, SortedSet<Integer>> {
	private static final long serialVersionUID = -6221740719095752767L;

	public static CoderFactory<Term> getFactory() {
		return new CoderFactory<Term>() {
			@Override public Coder<Term> build() { return new TermCoder(); }	
		};
	}
	

	private static class TermCoder implements Coder<Term> {
		
		
		@Override public void encode(CoderContext C, Term term) {
			// encode the signature
			C._c_linkset.encode(C, new Linkset(C.get_tbox(), term));
			
			// encode objects outside of the signature
			Link lnk = null;
			int n_same_linktype = 0;
			for (Entry<Integer, SortedSet<Integer>> map : term.entrySet()) {
				int pred_id = map.getKey();
				for (int obj_id: map.getValue()) {
					if (C.in_tbox(obj_id)) continue; // object part of signature					
					int obj_type = TermType.id2type(obj_id);
					// Fix conditioning information for this object.
					// This is an ugly hack, necessary because of out-of-order coding
					C._c_pred.set_conditional(pred_id);
					C._c_objtype.set_conditional(obj_type);

					Link next_lnk = new Link(pred_id, obj_type, -1);					
					if (next_lnk.equals(lnk)) { 
						C._c_moreobjs.encode(C, 1);
						n_same_linktype++;
					} else {
						if (lnk!=null) {
							C._c_moreobjs.encode(C, 0);
							C.use_bits("ABox bonus", -Codes.lgfac(n_same_linktype));
						}
						n_same_linktype = 1;
					}
					lnk = next_lnk;
					
					int obj_ix = TermType.id2ix(obj_id);
					switch (obj_type) {
					case TermType.NAMED   : C._c_namedobj_a.encode(C, obj_ix); break;
					case TermType.BNODE   : C._c_bnodeobj_a.encode(C, obj_ix); break;
					case TermType.LITERAL : break; // literals appear in order and do not need to be encoded
					default: assert false: "Unknown object type";
					}
				}
			}
			if (lnk!=null) {
				C._c_moreobjs.encode(C, 0);
				C.use_bits("ABox bonus", -Codes.lgfac(n_same_linktype));
			} 
		}
	}
}