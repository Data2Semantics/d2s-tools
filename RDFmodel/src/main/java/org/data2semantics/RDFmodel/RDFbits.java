package org.data2semantics.RDFmodel;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


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
		RDFGraph G = load("src/main/resources/AIFB/aifb-fixed_complete.n3");
		// RDFGraph G = load("src/main/resources/STCN/STCN_Publications.ttl");
		// RDFGraph G = load("src/main/resources/LDMC/LDMC_Task1_train.ttl");
		// RDFGraph G = load("src/main/resources/STCN_edited");

		// Blocks are a feeble attempt to be memory-conscious here
		
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
		
		
		// process(stringify(G._literals), "literals");

		G.testSorted();
		G.printSomeStats();
		Set<Integer> tbox = tbox_heuristic_most_incoming(G,8); // TODO tune parameter
		
		// use root boundary to encode the data
		Boundary root_B = new Boundary();
		root_B.add(ST);
		CLAccountant root_acc = encode(G, root_B, tbox, ST);
		
		// use best boundary to encode the data
		Boundary best_B = findBestBoundary(G, tbox, ST);
		CLAccountant best_acc = encode(G, best_B, tbox, ST);
		
		
		System.out.println("\n\n-----------------------------------\n");
		List<CLAccountant> res = new ArrayList<CLAccountant>();
		res.add(root_acc); res.add(best_acc);
		CLAccountant.report(res);
		
		System.out.println("Best boundary (size "+best_B.size()+"):");
		System.out.println(best_B.toString());
		System.out.println("");
	}
	

	
}
