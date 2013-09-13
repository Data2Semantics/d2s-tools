package org.data2semantics.RDFmodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class RDFhelper {
	
	public static RDFGraph load(String fn) {
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
		return new RDFGraph(L);
	}
	
	public static CLAccountant encode(RDFGraph G, Boundary B, Set<Integer> tbox, StringTree root) {
		CLAccountant acc = new CLAccountant("Bsize="+B.size()+"");
		URIDistinguisher D = new URIDistinguisher(B, root);
		new GraphCoderSigBased(acc, tbox, D, G._named.size(), G._nbnodes).encode(G);
		return acc;
	}
	
	public static Boundary findBestBoundary(RDFGraph G, Set<Integer> tbox, StringTree root) {
		Boundary B = new Boundary();
		B.add(root);
		
		Queue<StringTree> Btodo = new LinkedList<StringTree>();
		Btodo.add(root);
		
		double cl = Double.MAX_VALUE;
		
		while (!Btodo.isEmpty()) {
			StringTree node = Btodo.remove();
			if (node.isLeaf()) continue;
			Boundary Bnw = B.expand(node);
			double ncl = encode(G, Bnw, tbox, root).L();
			if (ncl < cl) { B = Bnw; cl = ncl; Btodo.addAll(node.getChildren()); }
		}
		return B;
	}
	
	public static Set<Integer> tbox_heuristic_most_incoming(RDFGraph G, int n) {
		Map<Integer,Integer> count_links = G.num_incoming();
		int included_lits = 0, included_lonely = 0;
		List<Entry<Integer,Integer>> items = new ArrayList<Entry<Integer,Integer>>(count_links.entrySet());
		Collections.sort(items, new ByValue<Integer>());
		Set<Integer> tbox = new HashSet<Integer>();
		for (int i=0; i<n; i++) {
			Entry<Integer,Integer> entry = items.get(items.size()-i-1);
			int obj = entry.getKey();
			if (entry.getValue()==1) included_lonely++;
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
