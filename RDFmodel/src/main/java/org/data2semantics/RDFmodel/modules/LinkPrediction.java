package org.data2semantics.RDFmodel.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.data2semantics.RDFmodel.RDFGraph;
import org.data2semantics.RDFmodel.RDFhelper;
import org.data2semantics.RDFmodel.TermType;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;

public class LinkPrediction extends RDFhelper {
	private String       _fn;
	private String       _predicted_predicate;
	private Set<String>  _suppressed_predicates;
	private Set<String>  _subjects;
	private Set<String>  _targets;
	
	public LinkPrediction(@In(name="file") String filename,
						  @In(name="predicted_predicate") String predicted_predicate,
						  @In(name="suppressed_predicates") List<String> suppressed_predicates) {
		_fn = filename;
		_predicted_predicate = predicted_predicate;
		_suppressed_predicates = new HashSet<String>(suppressed_predicates);
	}
	
	@Out(name="subjects") public Set<String> subjects() {
		return _subjects;
	}
	
	@Out(name="targets") public Set<String> targets() {
		return _targets;
	}
	
	
	@Main public void main() {
		_subjects = new HashSet<String>();
		_targets  = new HashSet<String>();
		
		RDFGraph G = load(_fn);
		for (int subj_ix=0; subj_ix < G._n_subj2pred2obj.size(); subj_ix++) {
			String subject = G._named.get(subj_ix).stringValue();
			SortedMap<Integer,List<Integer>> subj = G._n_subj2pred2obj.get(subj_ix);
			for (Entry<Integer,List<Integer>> entry : subj.entrySet()) {
				int pred_ix = entry.getKey();
				String pred_uri = G._named.get(pred_ix).stringValue();
				if (pred_uri.equals(_predicted_predicate)) {
					_subjects.add(subject);
					for (int obj_id : entry.getValue()) {
						assert TermType.id2type(obj_id)==TermType.NAMED : "Only uri targets are supported";
						_targets.add(G._named.get(TermType.id2ix(obj_id)).stringValue());
					}
				}
			}
		}
	}
}

