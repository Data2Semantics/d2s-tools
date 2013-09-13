package org.data2semantics.RDFmodel;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.RDFHandlerBase;



public class RDFLoader extends RDFHandlerBase {
	
	// mappings of value types to integer identifiers
	public IndexMap<BNode>      _bnodes2ix  = new IndexMap<BNode>();
	public IndexMap<URI>        _named2ix   = new IndexMap<URI>();
		
	// duplicate literals are not detected, so the literal map can be constructed the other way around
	public List<Literal> _ix2lit       = new ArrayList<Literal>();
	
	public List<SortedMap<Integer,List<Integer>>> _b_subj2pred2obj = new SoftList<SortedMap<Integer,List<Integer>>>();
	public List<SortedMap<Integer,List<Integer>>> _n_subj2pred2obj = new SoftList<SortedMap<Integer,List<Integer>>>();
	
	public void load(String filename) throws RDFParseException, RDFHandlerException, FileNotFoundException, IOException {
		URL url = new URL("file:"+filename);
		RDFFormat format = RDFFormat.forFileName(filename);
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(this);
		ParserConfig cfg = new ParserConfig();
		cfg.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
		cfg.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, false);
		cfg.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
		parser.setParserConfig(cfg);
		parser.parse(new FileReader(filename), url.toString());
	}
	
	int add(Value item) {
		if (item instanceof BNode) return TermType.ix2id(TermType.BNODE, _bnodes2ix.map((BNode)item));
		if (item instanceof URI)   return TermType.ix2id(TermType.NAMED, _named2ix.map((URI)item));
		assert item instanceof Literal : "Unrecognised value type";
		_ix2lit.add((Literal)item);
		return TermType.ix2id(TermType.LITERAL, _ix2lit.size()-1);
	}
	
	@Override
	public void handleStatement(Statement smt) {
		int subj_id = add(smt.getSubject());
		int pred_id = add(smt.getPredicate());
		int obj_id  = add(smt.getObject()); 
		
		int pred_ix = TermType.id2ix(pred_id); // pred type should be named
		int subj_ix = TermType.id2ix(subj_id), subj_type = TermType.id2type(subj_id);
		SortedMap<Integer,List<Integer>> links; // maps predicates to object indices
		if (subj_type==TermType.NAMED){
			links = _n_subj2pred2obj.get(subj_ix);
			if (links==null) {
				links = new TreeMap<Integer, List<Integer>>();
				_n_subj2pred2obj.set(subj_ix, links);
			}
		} else { // Subj is BNode
			links = _b_subj2pred2obj.get(subj_ix);
			if (links==null) {
				links = new TreeMap<Integer, List<Integer>>();
				_b_subj2pred2obj.set(subj_ix, links);
			}
		}
		
		List<Integer> objs = links.get(pred_ix);
		if (objs==null) {
			objs = new ArrayList<Integer>();
			links.put(pred_ix, objs);
		}
		objs.add(obj_id);
	}

	@Override
	public void endRDF() {
		// do nothing for now
	}


}



