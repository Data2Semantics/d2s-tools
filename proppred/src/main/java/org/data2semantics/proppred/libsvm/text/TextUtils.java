package org.data2semantics.proppred.libsvm.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.proppred.libsvm.SparseVector;

public class TextUtils {

	public static List<SparseVector> computeTFIDF(List<String> texts) {
		
		Map<String, Integer> word2index = new HashMap<String, Integer>();
		Map<Integer, Double> index2freq = new HashMap<Integer,Double>();
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
		        		index2freq.put(key, 0.0);
		        	}
		        	
		        	// Check if we have already seen it for this text
		        	if (fv.getValue(key) == 0) { // if not seen it, increase the doc freq count
		        		index2freq.put(key, index2freq.get(key) + 1.0);
		        		fv.setValue(key, 1);
		        	} else {
		        		fv.setValue(key, fv.getValue(key) + 1); // increase count
		        	}
		        }
		        start = end;
		        end = wordIt.next();
		    }		    
		}
		
		// Compute the IDF scores
		double nrTexts = (double) texts.size();
		for (int key : index2freq.keySet()) {
			index2freq.put(key, Math.log(nrTexts / index2freq.get(key)));
		}
		
		// Compute the TF-IDF scores
		for (SparseVector fv : res) {
			for (int key : fv.getIndices()) {
				fv.setValue(key, fv.getValue(key) * index2freq.get(key));
			}
			fv.setLastIndex(index2freq.size());
		}
		
		return res;
	}
	
	public static List<SparseVector> computeTFIDFforFV(List<SparseVector> fv) {
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
