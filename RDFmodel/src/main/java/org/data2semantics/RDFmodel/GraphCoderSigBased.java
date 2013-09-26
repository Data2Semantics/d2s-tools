package org.data2semantics.RDFmodel;

public class GraphCoderSigBased implements Coder<RDFGraph> {
	
	private URIDistinguisher _uri_distinguisher;
		
	public GraphCoderSigBased(URIDistinguisher uri_distinguisher) {
		_uri_distinguisher = uri_distinguisher;
	}
	
	@Override public void encode(CoderContext C, RDFGraph G) {
		for (int named_ix=0; named_ix < G._named.size(); named_ix++) {
			String uri = G._named.get(named_ix).stringValue();
			C._c_urinode.set_conditional(_uri_distinguisher.get_node(uri));
			C._c_term.encode(C, G._n_subj2pred2obj.get(named_ix));
		}
		
		C._c_urinode.set_conditional(null); // blank nodes have no uri to condition on
		for (int bnode_ix=0; bnode_ix < G._nbnodes; bnode_ix++) {
			C._c_term.encode(C, G._b_subj2pred2obj.get(bnode_ix));
		}

		C.use_bits(-Codes.lgfac(G._b_subj2pred2obj.size()));
	}
}
