package org.data2semantics.RDFmodel;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

// A TermCoder encodes all triples with this term in the subject position.


public class Term extends TreeMap<Integer, List<Integer>> {
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
			int n_same_linktype = 1;
			for (Entry<Integer,List<Integer>> map : term.entrySet()) {
				int pred_ix = map.getKey();
				for (int obj_id: map.getValue()) {
					if (C.in_tbox(obj_id)) continue; // object part of signature
					Link next_lnk = new Link(pred_ix, TermType.id2type(obj_id), -1);
					
					// Fix conditioning information for this object.
					// This is an ugly hack, necessary because of out-of-order coding
					C._c_pred.set_conditional(next_lnk.getPredIx());
					C._c_objtype.set_conditional(next_lnk.getObjType());
					
					if (lnk != null) {
						boolean same = next_lnk.equals(lnk);
						if (same) { 
							n_same_linktype++;
						} else {
							C.use_bits("ABox bonus", -Codes.lgfac(n_same_linktype));
							n_same_linktype = 1;
						}
						C._c_moreobjs.encode(C, same ? 1 : 0);
					}
					lnk = next_lnk;
					int obj_ix = TermType.id2ix(obj_id);
					switch (lnk.getObjType()) {
					case TermType.NAMED   : C._c_namedobj.encode(C, obj_ix); break;
					case TermType.BNODE   : C._c_bnodeobj.encode(C, obj_ix); break;
					case TermType.LITERAL : break; // literals appear in order and do not need to be encoded
					default: assert false: "Unknown object type";
					}
				}
			}
			if (lnk!=null) C._c_moreobjs.encode(C, 0);
		}
		
	}
}