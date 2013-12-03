package org.data2semantics.RDFmodel;

import java.util.Comparator;

/* There are four types of terms in an RDF graph.
 * For each type, there is an enumeration of terms of this type, and term is associated with its index
 * in this enumeration. That is the term's "ix" value.
 * The type can also be encoded in an integer; an (ix,type) pair is called a term's "id".
 * This enum also supplies methods to convert between ix, id and type.
 */
public class TermType {

	public static final int BNODE=0, NAMED=1, LITERAL=2, SIZE=3;
	
	public static int ix2id(int type, int ix) { return ix*SIZE+type; }
	public static int id2ix(int id)           { return id/SIZE; }
	public static int id2type(int id)         { return id%SIZE; }
	
	public static int compare(int id1, int id2) {
		int d = id2type(id1) - id2type(id2);
		return d==0 ? id2ix(id1) - id2ix(id2) : d;
	}
	
	public static String id2string(int id) {
		return type2string(id2type(id))+":"+id2ix(id);
	}
	
	public static String type2string(int type) {
		return type==BNODE ? "BN" : type==NAMED ? "NA" : type==LITERAL ? "LI" : "??";
	}
	
	public static class IdComparator implements Comparator<Integer> {
		@Override public int compare(Integer arg0, Integer arg1) {
			int d = id2type(arg0)-id2type(arg1);
			return d==0 ? id2ix(arg0)-id2ix(arg1) : d;
		}
	}
	
}
