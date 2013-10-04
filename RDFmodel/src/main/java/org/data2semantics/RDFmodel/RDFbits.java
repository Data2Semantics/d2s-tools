package org.data2semantics.RDFmodel;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;


public class RDFbits extends RDFhelper {

	public static <K> void save(K item, String filename) {
		System.out.println("Writing '"+filename+"'...");
		try {
			BufferedWriter W = new BufferedWriter(new FileWriter(filename));
			W.write(item.toString());
			W.close();
		} catch (IOException e) {
			System.err.println("Failed to write '"+filename+"': "+e);
		}
	}
		
	
	public static void main(String [] args) throws FileNotFoundException {
		// RDFGraph G = load("src/main/resources/AIFB/aifb-fixed_complete.n3");
		// RDFGraph G = load("src/main/resources/STCN/STCN_Publications.ttl");
		// RDFGraph G = load("src/main/resources/LDMC/LDMC_Task1_train.ttl");
		RDFGraph G = load("src/main/resources/STCN_edited");

		// Blocks are a feeble attempt to be memory-conscious here
		
		G.printSomeStats();
		
		int size_uris = 0, size_lits = 0;
		
		{
			String uristr = set2string(G._named);
			System.out.print("URI's: uncompressed "+uristr.length()*8+", compressed "+gzip(uristr)*8+", ");
		}
		
		StringTree ST = new StringTree(G._named);
		{
			String packed = ST.getPacked();
			size_uris = gzip(packed);
			System.out.println("packed "+packed.length()*8+", packed and zipped: "+size_uris+".");
		}
		
		{
			String lits = set2string(G._literals);
			size_lits = gzip(lits);
			System.out.println("Literals: "+lits.length()*8 +", zipped: "+size_lits+".");
		}
		
		// use root boundary to encode the data
		Boundary root_B = new Boundary();
		Set<Integer> tbox = tbox_heuristic_most_incoming(G,8);
		root_B.add(ST);
		CLAccountant root_acc = encode(G, root_B, tbox, ST);
		
		// find both boundary and tbox
		Pair<Boundary,Set<Integer>> pair = findBoundaryAndTBox(G, ST);
		Boundary best_B = pair.getLeft();
		Set<Integer> best_tbox = pair.getRight();
		CLAccountant best_acc = encode(G, best_B, best_tbox, ST);	
		
		System.out.println("\n\n-----------------------------------\n");
		List<CLAccountant> res = new ArrayList<CLAccountant>();
		res.add(root_acc); res.add(best_acc);
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
			case TermType.NAMED: System.out.println(". "+G._named.get(ix)); break;
			case TermType.BNODE: System.out.println(". Bnode, ix="+ix); break;
			case TermType.LITERAL: System.out.println(". Literal?!: "+G._literals.get(ix)); break;
			default: assert false: "Unknown type";
			}
		}
		System.out.println();
	}
	

	
}
