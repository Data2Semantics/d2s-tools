package org.data2semantics.RDFmodel;

import java.util.List;

import org.openrdf.model.URI;

public class GraphCoderSigBased implements Coder<RDFGraph> {
	
	private URIDistinguisher _uri_distinguisher;
	private List<URI>        _uris;
	
	public GraphCoderSigBased(URIDistinguisher uri_distinguisher, List<URI> uris) {
		_uri_distinguisher = uri_distinguisher;
		_uris              = uris;
	}
	
	@Override public void encode(CoderContext C, RDFGraph G) {
		for (int named_ix=0; named_ix < G._nnamed; named_ix++) {
			StringTree node;
			if (_uri_distinguisher==null) {
				node = null;
			} else {
				String uri = _uris.get(named_ix).stringValue();
				node = _uri_distinguisher.get_node(uri);
			}
			C._c_urinode.set_conditional(node);
			C._c_term.encode(C, G._n_subj2pred2obj.get(named_ix));
		}
		
		C._c_urinode.set_conditional(null); // blank nodes have no uri to condition on
		for (int bnode_ix=0; bnode_ix < G._nbnodes; bnode_ix++) {
			C._c_term.encode(C, G._b_subj2pred2obj.get(bnode_ix));
		}

		C.use_bits(-Codes.lgfac(G._b_subj2pred2obj.size()));
	}
	
	
	public static CoderFactory<RDFGraph> getFactory(URIDistinguisher D, List<URI> uris) {
		return new GraphCoderSigBasedFactory(D, uris);
	}
	
	private static class GraphCoderSigBasedFactory implements CoderFactory<RDFGraph> {
		private URIDistinguisher _D;
		private List<URI> _uris;
		public GraphCoderSigBasedFactory(URIDistinguisher D, List<URI> uris) { _D = D; _uris = uris; }
		@Override public Coder<RDFGraph> build() { return new GraphCoderSigBased(_D, _uris); }
	}
	
}
