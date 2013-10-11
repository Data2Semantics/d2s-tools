package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.data2semantics.RDFmodel.Linkset.LinksetCoderFactory.LinksetCoder;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;


public class RDFbits extends RDFhelper {
	
	public static void main(String [] args) {
		laurens();
		// test();
	}
	
	public static void laurens() {
		RDFGraph G = load("src/main/resources/laurens/linkedMovieDb", null, null);
		// RDFGraph G = load("src/main/resources/laurens/sp2bench", null, null);
		// RDFGraph G = load("src/main/resources/laurens/SWdogfood", null, null);

		System.out.println("Memory used: "+(mem_used()/1024)+"K");
		G.printSomeStats();

		Set<Integer> tbox = tbox_heuristic_most_incoming(G, -30);
		CoderContext C = encode(G, null, null, tbox, null);
		CLAccountant acc = C.getResults();
		int nl = C._f_linkset.get_nlinks();
		int ntr = G._ntriples;
		
		System.out.println();
		List<LinksetCoder> coders = C._f_linkset.get_coders();
		assert coders.size()==1 : "There should be only one linksetcoder";
		int nls = coders.get(0).get_nlinksets();
		int nsubj = G._nnamed+G._nbnodes;
		System.out.printf("linktypes  / triples  = %7d   /%7d    = %.3f\n", nl,  ntr,   (double)nl/ntr);
		System.out.printf("linksets   / subjects = %7d   /%7d    = %.3f\n", nls, nsubj, (double)nls/nsubj);
		double cmpr = acc.L(), uncompr = log_ngraphs(nsubj, G._nbnodes, G._npreds, G._nlits, ntr);
		System.out.printf("compressed / uncompr  = %9.2f/%9.2f = %.2f\n", cmpr, uncompr, cmpr/uncompr);
	}
	
	/* returns the number of rdf graphs that can be specified using ntriples triples,
	 * using a domain with nres resources of which npred are predicates, and nlit literals.
	 */
	public static double log_ngraphs(long nuri, long nbnodes, long npred, long nlit, long ntriples) {
		long nres = nuri + nbnodes;
		long n_res2res = ntriples - nlit; // number of res to res triples
		long n_res2lit = nlit;            // number of res to lit triples
		
		double lgsum = Double.MIN_VALUE;
		for (long n_r2r_preds = 0; n_r2r_preds<=npred; n_r2r_preds++) {
			// assume n_r2r_preds predicates run from resource to resource\
			long n_r2l_preds = npred - n_r2r_preds;
			lgsum = Codes.lgsum(lgsum, Codes.lgbinomial(nres*nres*n_r2r_preds, n_res2res));
			lgsum = Codes.lgsum(lgsum, Codes.lgbinomial(nres*n_r2l_preds, n_res2lit));
		}
		return lgsum - Codes.lgfac(nbnodes);
	}
	
	public static long mem_used() {
		System.gc(); // this hack is the only easy way to get memory consumption
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}
	
	public static void test() {
		List<URI>     uris = new ArrayList<URI>();
		List<Literal> lits = new ArrayList<Literal>();
		RDFGraph G = load("src/main/resources/AIFB/aifb-fixed_complete.n3", uris, lits);
		
		// RDFGraph G = load("src/main/resources/STCN/STCN_Publications.ttl");
		// RDFGraph G = load("src/main/resources/LDMC/LDMC_Task1_train.ttl");
		// RDFGraph G = load("src/main/resources/STCN_edited");

		G.printSomeStats();
		System.out.println("Memory used: "+(mem_used()/1024) + "K");
		
		int size_uris = 0, size_lits = 0;
		// Blocks are a feeble attempt to be memory-conscious here		
		{
			String uristr = set2string(uris);
			System.out.print("URI's: uncompressed "+uristr.length()*8+", compressed "+gzip(uristr)*8+", ");
		}
		
		StringTree ST = new StringTree(uris);
		{
			String packed = ST.getPacked();
			size_uris = gzip(packed);
			System.out.println("packed "+packed.length()*8+", packed and zipped: "+size_uris+".");
		}
		
		{
			String litstring = set2string(lits);
			size_lits = gzip(litstring);
			System.out.println("Literals: "+litstring.length()*8 +", zipped: "+size_lits+".");
		}
		
		List<CLAccountant> res = new ArrayList<CLAccountant>();
		
		// use root boundary to encode the data
		Set<Integer> tbox = tbox_heuristic_most_incoming(G, -30);
		// Set<Integer> tbox = tbox_greedy_search(G,root_B,ST);
		CLAccountant root_acc = encode(G, null, null, tbox, null).getResults();
		res.add(root_acc);
		
		Boundary best_B;
		Set<Integer> best_tbox;
		CLAccountant best_acc;
		
		// find both boundary and tbox
		Pair<Boundary,Set<Integer>> pair = findBoundaryAndTBox(G, uris, ST);
		best_B = pair.getLeft();
		best_tbox = pair.getRight();
		best_acc = encode(G, uris, best_B, best_tbox, ST).getResults();
		res.add(best_acc);
			
		System.out.println("\n\n-----------------------------------\n");
		CLAccountant.report(res);
		
		CLAccountant comb_acc = new CLAccountant("Combinatorial");
		GraphCoderCombinatorial c = new GraphCoderCombinatorial(comb_acc);
		c.encode(G);
		res.clear();
		res.add(comb_acc);
		CLAccountant.report(res);
		
		System.out.println("\nBest boundary (size "+best_B.size()+"):");
		System.out.println(best_B.toString());
		System.out.println();
		
		System.out.println("\nBest tbox (size "+best_tbox.size()+"):");
		for (int id : best_tbox) {
			int ix   = TermType.id2ix(id);
			switch (TermType.id2type(id)) {
			case TermType.NAMED: System.out.println(". "+uris.get(ix)); break;
			case TermType.BNODE: System.out.println(". Bnode, ix="+ix); break;
			case TermType.LITERAL: System.out.println(". Literal?!: "+lits.get(ix)); break;
			default: assert false: "Unknown type";
			}
		}
		System.out.println();		
	}
	

	
}
