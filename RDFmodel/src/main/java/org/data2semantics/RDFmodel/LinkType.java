package org.data2semantics.RDFmodel;


public class LinkType {

	private int _pred_ix;
	private int _obj_type;

	public LinkType(int pred_ix, int obj_type) { 
		_pred_ix = pred_ix; _obj_type = obj_type;
	}
		
	public int getPredIx()  { return _pred_ix; }
	public int getObjType() { return _obj_type; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _obj_type;
		result = prime * result + _pred_ix;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LinkType other = (LinkType) obj;
		return _obj_type == other._obj_type && _pred_ix == other._pred_ix;
	}
	
	public static CoderFactory<LinkType> getFactory() {
		return new CoderFactory<LinkType>() {
			@Override public Coder<LinkType> build() {
				return new ObjRefCoder<LinkType>(new BasicLinkTypeCoder());
			}
		};
	}

	// Learns the distribution on object types for each predicate separately 
	private static class BasicLinkTypeCoder implements Coder<LinkType> {		
		@Override public void encode(CoderContext C, LinkType lt) {
			C._c_pred.encode(C, lt.getPredIx());
			C._c_objtype.encode(C, lt.getObjType());
		}
	}
}