package org.data2semantics.exp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.Pair;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class DataSetUtils {

	public static List<Statement> createBlacklist(RDFDataSet dataset, List<Resource> instances, List<Value> labels) {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i), null, labels.get(i), true));
			if (labels.get(i) instanceof Resource) {
				newBL.addAll(dataset.getStatements((Resource) labels.get(i), null, instances.get(i), true));
			}
		}
		return newBL;
	}
	
	public static List<Statement> createBlacklist(RDFDataSet dataset, List<Pair<Resource>> instances) {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i).getFirst(), null, instances.get(i).getSecond(), true));
			newBL.addAll(dataset.getStatements(instances.get(i).getSecond(), null, instances.get(i).getFirst(), true));		
		}
		return newBL;
	}
	
}
