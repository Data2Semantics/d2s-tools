package org.data2semantics.RDFmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

public class RDFhelper {
	
	public static RDFGraph load(String fn, List<URI> uris, List<Literal> lits) {
		RDFLoader L = new RDFLoader();
		File f = new File(fn);
		try {
			if (f.isDirectory()) {
				for (File fc : f.listFiles()) {
					System.out.println("Reading '"+fc+"'...");
					L.load(fc.getAbsolutePath());
				}
			} else {
				System.out.println("Reading '"+fn+"'...");
				L.load(fn);
			}
		} catch (Exception e) {
			System.err.println("Cannot load '"+fn+"': "+e);
			System.exit(1);
		}
		if (uris!=null) uris.addAll(L._named2ix.invert());
		if (lits!=null) lits.addAll(L._ix2lit);
		return new RDFGraph(L);
	}
	
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
	
	public static CoderContext encode(RDFGraph G, List<URI> uris, Boundary B, Set<Integer> tbox, StringTree root) {
		URIDistinguisher D = B==null ? null : new URIDistinguisher(B, root);
		String name = B==null ? "NoUris" : "Bsize="+B.size();
		CoderContext C = new CoderContext(name, tbox, D, uris, G._nnamed, G._nbnodes);
		C._c_graph.encode(C, G);
		return C;
	}
	
	public static Pair<Boundary,Set<Integer>> findBoundaryAndTBox(RDFGraph G, List<URI> uris, StringTree ST) {
		Boundary B = new Boundary();
		Boundary Bold = null;
		B.add(ST);
		Set<Integer> Told = null;
		Set<Integer> T = null;
		
		while (true) {
			if (B.equals(Bold)) break;
			Told = T;
			T = tbox_greedy_search(G, uris, B, ST);
			
			if (T.equals(Told)) break;
			Bold = B;
			B = findBestBoundary(G, uris, T, ST);
		}
		
		return new ImmutablePair<Boundary,Set<Integer>>(B, T);
	}
	
	public static Boundary findBestBoundary(RDFGraph G, List<URI> uris, Set<Integer> tbox, StringTree ST) {
		System.out.println("Looking for best boundary...");
		Boundary B = new Boundary();
		B.add(ST);
		
		Queue<StringTree> Btodo = new LinkedList<StringTree>();
		Btodo.add(ST);
		
		double cl = Double.MAX_VALUE;
		
		while (!Btodo.isEmpty()) {
			StringTree node = Btodo.remove();
			if (node.isLeaf()) continue;
			Boundary Bnw = B.expand(node);
			double ncl = encode(G, uris, Bnw, tbox, ST).getResults().L();
			if (ncl < cl) { B = Bnw; cl = ncl; Btodo.addAll(node.getChildren()); }
		}
		System.out.println("Done, size is "+B.size());
		return B;
	}
	
	public static Set<Integer> tbox_greedy_search(RDFGraph G, List<URI> uris, Boundary B, StringTree ST) {
		System.out.println("Looking for tbox...");
		List<Entry<Integer,Integer>> items = new ArrayList<Entry<Integer,Integer>>(G.num_incoming().entrySet());
		Collections.sort(items, new ByValue<Integer>());
		int last_included = -1;
		Set<Integer> tbox_best = new HashSet<Integer>();
		double cl = encode(G, uris, B, tbox_best, ST).getResults().L();
		for (int ix=0; ix<items.size() && ix-last_included<20; ix++) {
			Entry<Integer,Integer> entry = items.get(items.size()-ix-1);
			Set<Integer> tbox_test = new HashSet<Integer>(tbox_best);
			tbox_test.add(entry.getKey());
			double cl_new = encode(G, uris, B, tbox_test, ST).getResults().L();
			if (cl_new >= cl) continue;
			// add this resource to the tbox 
			cl = cl_new;
			tbox_best = tbox_test;
			last_included = ix;			
		}
		System.out.println("Done, size is "+tbox_best.size());
		return tbox_best;
	}
	
	/* If n > 0, returns the n objects with most incoming edges.
	 * If n < 0, returns the objects with at least incoming -n edges. 
	 */
	public static Set<Integer> tbox_heuristic_most_incoming(RDFGraph G, int n) {
		Map<Integer,Integer> count_links = G.num_incoming();
		int included_lits = 0, included_lonely = 0;
		List<Entry<Integer,Integer>> items = new ArrayList<Entry<Integer,Integer>>(count_links.entrySet());
		Collections.sort(items, new ByValue<Integer>());		
		Set<Integer> tbox = new HashSet<Integer>();
		for (int i=0; i<(n<0 ? items.size() : n); i++) {
			Entry<Integer,Integer> entry = items.get(items.size()-i-1);
			int obj = entry.getKey(), freq = entry.getValue();
			if (n<0 && freq<-n) break;
			if (freq==1) included_lonely++;
			if (TermType.id2type(obj)==TermType.LITERAL) included_lits++;
			tbox.add(obj);
		}
		if (included_lits>0)   System.out.println("Warning: including "+included_lits+" literals in TBox");
		if (included_lonely>included_lits) System.out.println("Warning: including "+included_lonely+" objects with only one incoming edge in TBox");
		return tbox;
	}
	
	public static String set2string(Collection<?> items) {
		StringBuilder sb = new StringBuilder();
		for (Object o : items) { sb.append(o.toString()); sb.append('\n'); }
		return sb.toString();
	}
	
	public static int gzip(String data) {
		int L = 0;
		try {
			PipedInputStream  pis = new PipedInputStream();
			new WriterThread(data, new GZIPOutputStream(new PipedOutputStream(pis))).start();
			final int buflen = 16384;
			byte [] buf = new byte[buflen];
			while (true) {
				int nread = pis.read(buf, 0, buflen);
				if (nread==-1) break;
				L += nread;  
			}
			pis.close();
		} catch (IOException e) {
			System.err.println("gzip failed: "+e);
			System.exit(1);
		}		
		return L * 8; // size in bits
	}
	
	public static class WriterThread extends Thread {
		
		private byte [] _data;
		private OutputStream _os;
		
		public WriterThread(String data, OutputStream os) {
			_data = data.getBytes();
			_os = os;
		}
		
		@Override public void run() {
			try {
				_os.write(_data);
				_os.close();
			} catch (IOException e) {
				System.err.println("Error writing to pipe: "+e);
				System.exit(1);
			} 
		}
	}
}

class ByValue<T> implements Comparator<Entry<T, Integer>> {
	@Override
	public int compare(Entry<T,Integer> e1, Entry<T,Integer> e2) {
		return e1.getValue() - e2.getValue();
	}
}

/* The following code does breadth-first exhaustive search 
 * 
 *  
 * Collection<Boundary> Q = new ArrayList<Boundary>();
	Q.add(rootonly);
	int num=0;
	for (int depth=0; depth<7; depth++) {
		System.out.println("\n*** Depth "+depth+" ***");
		HashSet<BitSet> not_yet_seen = new HashSet<BitSet>();
		Collection<Boundary> Q_expanded = new ArrayList<Boundary>();
		for (Boundary B: Q) {
			num++;
			CLAccountant acc = depth==0 ? root : new CLAccountant("Boundary #"+num);
			URIDistinguisher D = new URIDistinguisher(B, ST, G._named);
			new GraphCoderSigBased(acc, tbox, D, G._named.size(), G._nbnodes).encode(G);
			System.out.print("Tree #"+num+", codelength: "+acc.L());
			if (acc.L() < best.L()) { best=acc; bestB = B; System.out.print(" (new best!)"); }
			System.out.println();
			
			for (StringTree node : B) {
				Boundary bnw = B.expand(node);
				if (bnw!=null && not_yet_seen.add(ST.key(bnw))) Q_expanded.add(bnw);
			}
		}
		Q = Q_expanded;
	}
 */

