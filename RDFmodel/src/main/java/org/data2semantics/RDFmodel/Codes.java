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
	
	public static double stirling(long n) {
		final double c = 0.5*lg(2*Math.PI);
		return (n+0.5)*lg(n) - n/_ln2 + c;
	}
	
	public static double lgfac(long n) {
		final int table_size = 10000;
		if (lgfac_table==null) {
			
			lgfac_table = new double[table_size];
			lgfac_table[0] = 0; // log(0!) = log(1) = 0
			for (int i=1; i<table_size; i++) {
				lgfac_table[i] = lgfac_table[i-1] + lg(i); 
			}
		}
		return n < table_size ? lgfac_table[(int)n] : stirling(n);
	}
	
	public static double lgbinomial(long n, long m) {
		return 0<=m && m<=n ? lgfac(n)-lgfac(m)-lgfac(n-m) : 0L; 
	}
	
	public static double lgmultinomial(long [] counts) {
		int sum = 0;
		double L = 0;
		for (int i=0; i<counts.length; i++) {
			L -= lgfac(counts[i]);
			sum += counts[i];
		}
		L += lgfac(sum);
		return L;
	}

	// returns lg(2^l1 + 2^l2)
	public static double lgsum(double l1, double l2) {
		double m = l1 > l2 ? l1 : l2;
		return m+Codes.lg(Math.pow(2, l1-m)+Math.pow(2,  l2-m));
	}
	
	
	
}
