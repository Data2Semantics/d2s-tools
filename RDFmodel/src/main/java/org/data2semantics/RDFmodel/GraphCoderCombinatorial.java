package org.data2semantics.RDFmodel;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

public class GraphCoderCombinatorial implements Coder<RDFGraph> {
	
	@Override public void encode(CoderContext C, RDFGraph G) {		
		// already known: number of named nodes, number of literals
		
		// 1. Initialise 
		
		int npreds  = G._npreds;
		int nnamed  = G._n_subj2pred2obj.size();
		int nbnodes = G._nbnodes;
		int nlits   = G._literals.size();
		
		int [] nsubj = { nnamed, nbnodes, nlits };
		
		int [][][] counts = new int[2][3][npreds];
		int [][]   sum    = new int[2][3];          // sum over counts for each pred
		int        tot    = 0;                      // sum over sum for all types

		for (int t1=0; t1<2; t1++) {
			for (int t2=0; t2<3; t2++) {
				for (int pred=0; pred<npreds; pred++) { 
					counts[t1][t2][pred]=0;
				}
				sum[t1][t2]=0;
			}
		}
		
		IndexMap<Integer> pred_map = new IndexMap<Integer>();
		
		// 2. Count some stuff
		
		for (int nb=0; nb<2; nb++) {
			for (SortedMap<Integer,List<Integer>> map : (nb==0 ? G._n_subj2pred2obj : G._b_subj2pred2obj)) {
				for (Entry<Integer,List<Integer>> e : map.entrySet()) {
					int pred_ix = e.getKey();
					int pred = pred_map.map(pred_ix);
					for (int obj_id : e.getValue()) {
						int type = TermType.id2type(obj_id);
						int t = type==TermType.NAMED ? 0 : type==TermType.BNODE ? 1 : 2;
						counts[nb][t][pred]++;
						sum[nb][t]++;
						tot++;
					}
				}
			}
		}
		
		// 3. Encode graph structure

		C.use_bits("prelim", Codes.universal_nonnegint(nbnodes));      // number of bnodes
		C.use_bits("prelim", Codes.universal_nonnegint(npreds));       // number of predicates
		C.use_bits("prelim", npreds * Codes.lg(nnamed));               // set of predicates

		// encode first tot, then sum using tot, then counts using sum
		C.use_bits("counts", Codes.universal_nonnegint(tot));  // tot # links
		C.use_bits("counts", Codes.lgbinomial(tot+6-1,  6-1)); // array of link counts per src/dst type
		for (int t1=0; t1<2; t1++) {
			for (int t2=0; t2<3; t2++) {
				C.use_bits("counts", Codes.lgbinomial(sum[t1][t2]+npreds-1, npreds-1));
			}
		}
		
		/* now that the decoder knows the #links for each src type,dst type,pred combination,
		 * we can transmit the individual links. Literals are handled separately because
		 * on the one hand, we don't need to encode the targets, as they are transmitted in
		 * order in the first part of the code. We do need to take care that here may be multiple
		 * literals for each (subj,pred) pair.
		 */
		for (int pred=0; pred<npreds; pred++) {
			for (int t1=0; t1<2; t1++) {
				for (int t2=0; t2<2; t2++) {
					// adjacency matrix between objects of type t1 and t2 respectively:			
					C.use_bits("adjacency", Codes.lgbinomial(nsubj[t1]*nsubj[t2], counts[t1][t2][pred]));
				}
				// specify #literals with this pred for each subject
				if (nsubj[t1]>0) C.use_bits("literals", Codes.lgbinomial(counts[t1][2][pred]+nsubj[t1]-1,  nsubj[t1]-1));
			}
		}
		
		// We used a fixed bnode ordering, however, bnodes can be reordered arbitrarily without
		// affecting code length
		C.use_bits("bnode_bonus", -Codes.lgfac(nbnodes));			
	}
}
