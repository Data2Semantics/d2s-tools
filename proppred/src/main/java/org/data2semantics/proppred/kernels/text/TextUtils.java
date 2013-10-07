package org.data2semantics.proppred.kernels.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.proppred.learners.SparseVector;

/**
 * Some utilities methods for dealing with text, e.g. computing term-vectors (TF) and TF-IDF from those term-vectors.
 * 
 * 
 * @author Gerben
 *
 */
public class TextUtils {

	public static List<SparseVector> computeTF(List<String> texts) {
		
		Map<String, Integer> word2index = new HashMap<String, Integer>();
		List<SparseVector> res = new ArrayList<SparseVector>();
		
		BreakIterator wordIt = BreakIterator.getWordInstance();
		
		for (String text : texts) {
			SparseVector fv = new SparseVector();
			res.add(fv);
			
			wordIt.setText(text);
		    int start = wordIt.first();
		    int end = wordIt.next();
		    
		    while (end != BreakIterator.DONE) {
		        String word = text.substring(start,end);
		        if (Character.isLetterOrDigit(word.charAt(0))) {
		        	// Get the int key for the word
		        	Integer key = word2index.get(word);
		        	if (key == null) {
		        		key = new Integer(word2index.size()+1);
		        		word2index.put(word, key);
		        	}
		
	        		fv.setValue(key, fv.getValue(key) + 1); // increase count
		        }
		        start = end;
		        end = wordIt.next();
		    }		    
		}		
		return res;
	}
	
	public static List<SparseVector> computeTFIDF(List<SparseVector> fv) {
		Map<Integer, Double> idfMap = new HashMap<Integer, Double>();
		double nrFV = fv.size();
		
		for (SparseVector v : fv) {
			for (int i : v.getIndices()) {
				Double freq = idfMap.get(i);
				if (freq == null) { // Have to compute the IDF
					freq = 0.0;
					for (SparseVector v2 : fv) {
						freq += (v2.getValue(i) > 0) ? 1 : 0;
					}
					if (freq == 0.0) {
						freq = 1.0;
					}
					freq = Math.log(nrFV / (freq));
					idfMap.put(i, freq);	
				}
				v.setValue(i, v.getValue(i) * freq);
			}
		}
		return fv;
	}
	
	
}
