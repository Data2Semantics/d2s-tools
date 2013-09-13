package org.data2semantics.RDFmodel.modules;

import java.util.Set;

import org.data2semantics.RDFmodel.Boundary;
import org.data2semantics.RDFmodel.CLAccountant;
import org.data2semantics.RDFmodel.RDFGraph;
import org.data2semantics.RDFmodel.RDFhelper;
import org.data2semantics.RDFmodel.StringTree;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

@Module(name="RDFCompression") public class RDFCompression extends RDFhelper {
	
	private String _fn;
	private RDFGraph _G;
	private StringTree _ST;
	private int _minlinks;
	private Set<Integer> _tbox;
	private Boundary _boundary;
	
	// output fields
	@Out(name        = "URI codelength", 
		 description = "The uncompressed codelength of the set of all URI's in the data") 
	public int _cl_uris;
	
	@Out(name        = "URI gzipped codelength", 
		 description = "The gzipped codelength of the set of all URI's in the data") 
	public int _cl_uris_gzipped;
	
	@Out(name        = "URI packed codelength", 
		 description = "The packed codelength of the set of all URI's in the data") 
	public int _cl_uris_packed;
	
	@Out(name        = "URI gzipped,packed codelengths", 
		 description = "The gzipped,packed codelength of the set of all URI's in the data") 
	public int _cl_uris_packed_gzipped;
	
	@Out(name        = "Literal codelengths", 
		 description = "The codelength of the list of all literals in the data") 	
	public int _cl_lits;
	
	@Out(name        = "Literal gzipped codelengths", 
		 description = "The gzipped codelength of the list of all literals in the data") 	
	public int _cl_lits_gzipped;
	
	public RDFCompression(@In(name="file") String filename, @In(name="minlinks") int minlinks) {
		_fn = filename;
		_minlinks = minlinks;
	}
	

	private void codelengths() {
		String uristr = set2string(_G._named);
		String packed = _ST.getPacked();
		_cl_uris                = uristr.length() * 8;
		_cl_uris_gzipped        = gzip(uristr);
		_cl_uris_packed         = packed.length() * 8;
		_cl_uris_packed_gzipped = gzip(packed);
		
		String lits = set2string(_G._literals);
		_cl_lits                = lits.length() * 8;
		_cl_lits_gzipped        = gzip(lits);
	}
	
	@Out(name="Structure codelength no-URIs",
		 description="The codelength of all structural information in the data, without using URIs")
	public double cl_structure_no_uris() {
		Boundary root_b = new Boundary(); root_b.add(_ST);
		return cl_structure(root_b);
	}
	
	@Out(name="Structure codelength URIs",
			 description="The codelength of all structural information in the data, using conditioning on the URIs")
	public double cl_structure_uris() {
		return cl_structure(_boundary);
	}

	private double cl_structure(Boundary B) {
		CLAccountant acc = encode(_G, B, _tbox, _ST);
		return acc.L();
	}
	
	@Main
	public void main() {
		_G = load(_fn);
		_ST = new StringTree(_G._named);
		_tbox = tbox_heuristic_most_incoming(_G, _minlinks);
		_boundary = findBestBoundary(_G, _tbox, _ST);
		codelengths();
	}
	
}