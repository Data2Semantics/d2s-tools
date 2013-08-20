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

@Module(name="GeoDataSet")
public class GeoDataSetModule {
	private RDFDataSet dataset;
	private int minSize;
	private double fraction;
	private long seed;
	private String property;
	
	private List<Resource> instances;
	private List<Value> labels;
	private List<Double> target;
	private List<Statement> blacklist;
	
	public GeoDataSetModule(
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

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);
			for (Statement stmt2 : stmts2) {
			if (rand.nextDouble() < fraction) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
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
