package org.data2semantics.RDFmodel;

public class Codes {

	public static final double _ln2 = Math.log(2);
	
	public static double lg(double x) { return Math.log(x)/_ln2; }
	
	public static double universal_posint(int n) {
		return -lg(1/lg(n+1.0) - 1/lg(n+2.0));
	}
	
	public static double universal_nonnegint(int n) {
		return universal_posint(n+1);
	}
	
	public static double uniform(int n) { return lg(n); }

	private static double [] lgfac_table = null;
	
	public static double sterling(int n) {
		final double c = 0.5*lg(2*Math.PI);
		return (n+0.5)*lg(n) - n/_ln2 + c;
	}
	
	public static double lgfac(int n) {
		final int table_size = 10000;
		if (lgfac_table==null) {
			
			lgfac_table = new double[table_size];
			lgfac_table[0] = 0; // log(0!) = log(1) = 0
			for (int i=1; i<table_size; i++) {
				lgfac_table[i] = lgfac_table[i-1] + lg(i); 
			}
		}
		return n < table_size ? lgfac_table[n] : sterling(n);
	}
	
	public static double lgbinomial(int n, int m) { return lgfac(n)-lgfac(m)-lgfac(n-m); }
	
	public static double lgmultinomial(int [] counts) {
		int sum = 0;
		double L = 0;
		for (int i=0; i<counts.length; i++) {
			L -= lgfac(counts[i]);
			sum += counts[i];
		}
		L += lgfac(sum);
		return L;
	}
	
	
	
}
