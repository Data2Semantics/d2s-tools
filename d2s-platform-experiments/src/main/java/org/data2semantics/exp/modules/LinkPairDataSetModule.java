package org.data2semantics.exp.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.exp.utils.DataSetUtils;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.kernels.Pair;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="LinkPairDataSet")
public class LinkPairDataSetModule {
	private RDFDataSet dataset;
	private double fraction;
	private long seed;
	private String predicate;
	
	private List<Pair<Resource>> instances;
	private List<Value> labels;
	private List<Double> target;
	private List<Statement> blacklist;
	
	public LinkPairDataSetModule(
			@In(name="dataset") RDFDataSet dataset,
			@In(name="fraction") double fraction,
			@In(name="seed") int seed,
			@In(name="predicate") String predicate
			) {
		this.dataset = dataset;
		this.fraction = fraction;
		this.seed = seed;
		this.predicate = predicate;
	}
	
	@Main
	public List<Pair<Resource>> createDataSet() {
		Random rand = new Random(seed);
		
		// Extract all triples with the predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, predicate, null);

		Set<Resource> instA = new HashSet<Resource>();
		Set<Resource> instB = new HashSet<Resource>();
		Set<Pair<Resource>> posSet = new HashSet<Pair<Resource>>();
		Set<Pair<Resource>> negSet = new HashSet<Pair<Resource>>();
		
		// Get all the positive examples and put them in the set, store the elements of the pairs to generate negative examples
		for (Statement stmt : stmts) {
			instA.add(stmt.getSubject());
			if (stmt.getObject() instanceof Resource) {
				instB.add((Resource) stmt.getObject());
				posSet.add(new Pair<Resource>(stmt.getSubject(), (Resource) stmt.getObject()));
			}
		}
		
		// Generate the negative examples
		for (Resource a : instA) {
			for (Resource b : instB) {
				Pair<Resource> pair = new Pair<Resource>(a,b);
				if (!posSet.contains(pair)) {
					negSet.add(pair);
				}
			}
		}

		// initialize the lists of instances and labels
		instances = new ArrayList<Pair<Resource>>();
		labels = new ArrayList<Value>();
		
		for (Pair<Resource> pair : posSet) {
			if (rand.nextDouble() <= fraction) {
				instances.add(pair);
				labels.add(dataset.createLiteral("true"));
			}
		}
		
		for (Pair<Resource> pair : negSet) {
			if (rand.nextDouble() <= fraction) {
				instances.add(pair);
				labels.add(dataset.createLiteral("false"));
			}
		}
		
		// Shuffle, since we had a perfectly ordered set
		Collections.shuffle(instances, new Random(seed));
		Collections.shuffle(labels, new Random(seed));
	
		blacklist = DataSetUtils.createBlacklist(dataset, instances);
		target = EvaluationUtils.createTarget(labels);
		
		return instances;
	}
	
	@Out(name="instances")
	public List<Pair<Resource>> getInstances() {
		return instances;
	}
	
	@Out(name="labels")
	public List<Value> getLabels() {
		return labels;
	}
	
	@Out(name="target")
	public List<Double> getTarget() {
		return target;
	}
	
	@Out(name="blacklist") 
	public List<Statement> getBlacklist() {
		return blacklist;
	}
	
	
}
