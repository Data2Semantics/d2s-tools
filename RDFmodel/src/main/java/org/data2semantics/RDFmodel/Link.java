package org.data2semantics.RDFmodel;


public class Link {
	
	public LinkType _lt;
	public int _obj_ix;
	
	public Link(LinkType lt, int obj_ix) { _lt = lt; _obj_ix = obj_ix; }
	
	public LinkType getLinkType() { return _lt; }
	public int getObjIx() { return _obj_ix; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_lt == null) ? 0 : _lt.hashCode());
		result = prime * result + _obj_ix;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Link other = (Link) obj;
		return other._lt.equals(_lt) && _obj_ix == other._obj_ix;
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
			LinkType lt = lnk.getLinkType();
			C._c_linktype.encode(C,  lt);
			int obj_ix = lnk.getObjIx();
			boolean in_tbox = obj_ix!=-1;
			C._c_hasobj.encode(C, in_tbox ? 1 : 0);
			if (in_tbox) {
				switch (lt.getObjType()) {
				case TermType.NAMED   : C._c_namedobj.encode(C, obj_ix); break;
				case TermType.BNODE   : C._c_bnodeobj.encode(C, obj_ix); break;
				case TermType.LITERAL : break; // literals appear in order and do not need to be encoded
				default: assert false: "Unknown object type";
				}
			}
		}
	}
}