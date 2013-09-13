package org.data2semantics.RDFmodel;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

public class GraphCoderSigBased extends Coder<RDFGraph> {
	
	private BundleMaker<StringTree> _uri_mapper = new BundleMaker<StringTree>();
	private Conditioner<SortedMap<Integer,List<Integer>>,StringTree> _term_coder;
	private URIDistinguisher _uri_distinguisher;
	
	public GraphCoderSigBased(CLAccountant acc, Set<Integer> tbox, URIDistinguisher uri_distinguisher, 
							int nnamed, int nbnodes) {
		init(acc, null);
		_uri_distinguisher = uri_distinguisher;
		_term_coder = new Conditioner<SortedMap<Integer,List<Integer>>,StringTree>
				(new TermCoderFactory(acc, tbox, nnamed, nbnodes));
	}


	@Override
	public void encode(RDFGraph G) {
		// TermCoder coder = new TermCoder(getAccountant(), _tbox, G._named.size(), G._nbnodes);

		// use null conditioning information for blank nodes
		Coder<SortedMap<Integer,List<Integer>>> bncoder = _term_coder.get(null);
		
		for (int named_ix=0; named_ix < G._named.size(); named_ix++) {
			String uri = G._named.get(named_ix).stringValue();
			Bundle<StringTree> node = _uri_mapper.bundle(_uri_distinguisher.get_node(uri));
			_term_coder.get(node).encode(G._n_subj2pred2obj.get(named_ix));
		}
		for (int bnode_ix=0; bnode_ix < G._nbnodes; bnode_ix++) {
			bncoder.encode(G._b_subj2pred2obj.get(bnode_ix));
		}
		
		/*
		System.out.println("Encoding using "+getAccountant().getName()+":"+
			           " #signatures:" + coder.num_linksets()+
				       " #signature links: " + coder.num_links());
		 */
	}
}
