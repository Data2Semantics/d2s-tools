package org.data2semantics.exp.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.utils.DataSetUtils;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="AffiliationDataSet")
public class AffiliationDataSetModule {
	private RDFDataSet dataset;
	private int minSize;
	private double fraction;
	private long seed;
	private String property;
	
	private List<Resource> instances;
	private List<Value> labels;
	private List<Double> target;
	private List<Statement> blacklist;
	
	public AffiliationDataSetModule(
			@In(name="dataset") RDFDataSet dataset,
			@In(name="minSize") int minSize,
			@In(name="fraction") double fraction,
			@In(name="seed") int seed,
			@In(name="property") String property
			) {
		this.dataset = dataset;
		this.minSize = minSize;
		this.fraction = fraction;
		this.seed = seed;
		this.property = property;
	}
	
	@Main
	public List<Resource> createDataSet() {
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		
		Random rand = new Random(seed);
		
		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, property, null);

		// initialize the lists of instances and labels
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			if (rand.nextDouble() <= fraction) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}

		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
		blacklist = DataSetUtils.createBlacklist(dataset, instances, labels);
		target = EvaluationUtils.createTarget(labels);
		
		return instances;
	}
	
	@Out(name="instances")
	public List<Resource> getInstances() {
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
