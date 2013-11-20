package org.data2semantics.RDFmodel;


public class Link {
	
	private int _pred_id;
	private int _obj_type;
	private int _obj_ix;
	
	public Link(int pred_id, int obj_type, int obj_ix) {
		_pred_id  = pred_id;
		_obj_type = obj_type;
		_obj_ix   = obj_ix;
	}
	
	public int getPredId()  { return _pred_id;  }
	public int getObjType() { return _obj_type; }
	public int getObjIx()   { return _obj_ix;   }
	
		
	@Override public String toString() { return "["+_pred_id+","+_obj_type+","+(_obj_ix==-1 ? "*" : _obj_ix)+"]"; }
	
	@Override public int hashCode() { return _pred_id + 31 * (_obj_type * 5 + _obj_ix);	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Link o = (Link) obj;
		return o._pred_id==_pred_id && o._obj_type==_obj_type && o._obj_ix==_obj_ix;
	}

	public static CoderFactory<Link> getFactory() {
		return new CoderFactory<Link>() {
			@Override public Coder<Link> build() {
				return new ObjRefCoder<Link>(new BasicLinkCoder());
			}
		};
		
	}

	private static class BasicLinkCoder implements Coder<Link> {
				
		@Override public void encode(CoderContext C, Link lnk) {
			C._c_pred.encode(C, lnk._pred_id);
			C._c_objtype.encode(C, lnk._obj_type);
			boolean in_tbox = lnk._obj_ix!=-1;
			C._c_hasobj.encode(C, in_tbox ? 1 : 0);
			if (in_tbox) {
				switch (lnk._obj_type) {
				case TermType.NAMED   : C._c_namedobj_t.encode(C, lnk._obj_ix); break;
				case TermType.BNODE   : C._c_bnodeobj_t.encode(C, lnk._obj_ix); break;
				case TermType.LITERAL : break; // literals appear in order and do not need to be encoded
				default: assert false: "Unknown object type";
				}
			}
		}
	}
}