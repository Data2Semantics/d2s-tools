package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

@Module(name="RDFDataPreProcessor")
public class RDFDataPreProcessorModule extends RDFMLExperiment {
	private final static Logger LOG = Logger.getLogger(RDFDataPreProcessorModule.class.getName());
	
	@Out(name = "instances")
	public ArrayList<Resource> instances;
	

	@Out(name = "labels")
	public ArrayList<Value> labels;
	

	@Out(name = "blacklist")
	public ArrayList<Statement> blackList;
	

	@Out(name = "dataset")
	public RDFFileDataSet dataset;
	
	@Main
	public RDFFileDataSet processRDFData(@In(name="fraction") Double fraction, 
										 @In(name="dataDir") String dataDirectory, 
										 @In(name="objectFilter1") String objectFilter1,
										 @In(name="predicateFilter1") String predicateFilter1,
										 @In(name="predicateFilter2") String predicateFilter2
			)	{
		
		LOG.info("In module, creating dataset");
		dataset=new RDFFileDataSet(dataDirectory, RDFFormat.NTRIPLES);
		createGeoDataSet(10,fraction,123, predicateFilter2);
		LOG.info("In module, done creating dataset");
		return dataset;
	}
	
    protected void createGeoDataSet(int minSize, double frac, long seed, String property) {
		
		
		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		
		// These are actually the output of this module if I am going to extract.
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			for (Statement stmt2 : stmts2) {


				if (rand.nextDouble() < frac) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());
				}
				
			}
		}

		//TODO: Include this back as part of the preparation
		RDFMLExperiment.labels=labels;
		RDFMLExperiment.instances=instances;
		RDFMLExperiment.blackList=blackList;
		RDFMLExperiment.dataset=dataset;
		
		removeSmallClasses(minSize);
		createBlackList();
		
		labels.clear(); labels.addAll(RDFMLExperiment.labels);
		instances.clear(); instances.addAll(RDFMLExperiment.instances);
		blackList.clear(); blackList.addAll(RDFMLExperiment.blackList);
		
		
		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
	}
}
