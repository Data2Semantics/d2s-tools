package org.data2semantics.RDFmodel;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;


public class CLAccountant {

	private final HashMap<String, Double> _parts = new HashMap<String, Double>();
	private final HashMap<String, Integer> _num  = new HashMap<String, Integer>();
	
	private String _name;
	
	public CLAccountant(String name) { _name=name; }
	
	public String getName() { return _name; }
	
	public double add(String name, double L) {
		Double d = _parts.get(name);
		double l = d==null ? 0 : d.doubleValue();
		_parts.put(name, l+L);	
		return L;
	}
	
	public void spawned_new(String name) {
		Integer num = _num.get(name);
		_num.put(name,  num==null ? 1 : num+1);
	}
	
	public double L() {
		double L = 0;
		for (Double d: _parts.values()) L+=d.doubleValue();
		return L;
	}

	@Override
	public String toString() {
		String res = String.format("%7.2f", L()) + " bits (";
		boolean first = true;
		for (String name : _parts.keySet()) {
			if (first) first=false; else res += ", ";
			res += String.format("%s:%.1f",  name, _parts.get(name));
		}
		res+=")";
		return res;
	}
	
	public static void report(Collection<CLAccountant> results) {
		report(results, System.out);
	}

	public static void report(Collection<CLAccountant> results, PrintStream out) {
		// first map all keys that appear in at least one code to the set of associated codelengths 
		HashMap<String,Vector<Double>> sizes = new HashMap<String, Vector<Double>>();
		for (CLAccountant acc : results) {
			for (Entry<String, Double> entry : acc._parts.entrySet()) {
				String key = entry.getKey();
				Double val = entry.getValue();
				if (!sizes.containsKey(key)) sizes.put(key, new Vector<Double>());
				sizes.get(key).add(val);
			}
		}
		
		// convert to sorted List
		List<String> list = new ArrayList<String>(sizes.keySet());
		Collections.sort(list);
				
		// header
		out.printf("%-20s |", "");
		for (CLAccountant acc : results) out.printf(" %15s",  acc.getName());
		out.println();
		for (int i=0; i<20; i++) out.print("-");
		out.print("-+");
		for (int i=0; i<results.size()*16; i++) out.print("-");
		out.println();

		// print row for each code length key in the list
		for (String key : list) {
			out.printf("%-20s |", key);
			for (CLAccountant acc : results) {
				Double cl = acc._parts.get(key);
				if (cl==null) { 
					out.printf(" %15s", "-");
				} else {
					Integer num = acc._num.get(key);
					if (num==null) {
						out.printf(" %10.1f     ", cl.doubleValue());
					} else {
						out.printf(" %10.1f(%3d)", cl.doubleValue(), acc._num.get(key));
					}
				}
			}
			out.println();
		}
		
		// footer
		for (int i=0; i<20; i++) out.print("-");
		out.print("-+");
		for (int i=0; i<results.size()*16; i++) out.print("-");
		out.println();
		out.printf("%-20s |", "");
		for (CLAccountant acc : results) out.printf(" %10.1f     ",  acc.L());
		out.println();
		
	}
	
}
