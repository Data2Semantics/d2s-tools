package org.data2semantics.RDFmodel;


public class GraphCoderSigBased implements Coder<RDFGraph> {
	
	private int [] _uri_map;
	
	public GraphCoderSigBased(int [] uri_map) { _uri_map = uri_map; }
	
	@Override public void encode(CoderContext C, RDFGraph G) {
		for (int named_ix=0; named_ix < G._nnamed; named_ix++) {
			if (_uri_map != null) C._c_urinode.set_conditional(_uri_map[named_ix]);
			C._c_term.encode(C, G._n_subj2pred2obj.get(named_ix));
		}
		
		C._c_urinode.set_conditional(null); // blank nodes have no uri to condition on
		for (int bnode_ix=0; bnode_ix < G._nbnodes; bnode_ix++) {
			C._c_term.encode(C, G._b_subj2pred2obj.get(bnode_ix));
		}

		C.use_bits("Bnode order bonus", -Codes.lgfac(G._b_subj2pred2obj.size()));
	}
	
	
	public static CoderFactory<RDFGraph> getFactory(int [] uri_map) {
		return new GraphCoderSigBasedFactory(uri_map);
	}
	
	private static class GraphCoderSigBasedFactory implements CoderFactory<RDFGraph> {
		private int [] _uri_map;
		public GraphCoderSigBasedFactory(int [] uri_map) { _uri_map = uri_map; }
		@Override public Coder<RDFGraph> build() { return new GraphCoderSigBased(_uri_map); }
	}
	
}
