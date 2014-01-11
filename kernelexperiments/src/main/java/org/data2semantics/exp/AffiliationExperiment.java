package org.data2semantics.exp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.molecules.GraphUtils;
import org.data2semantics.exp.molecules.MoleculeGraphExperiment;
import org.data2semantics.exp.molecules.RDFIntersectionSubTreeSlashBurnKernel;
import org.data2semantics.exp.molecules.RDFWLSubTreeSlashBurnKernel;
import org.data2semantics.exp.molecules.WLSubTreeKernel;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFGraphKernelExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFCombinedKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexWithSuperTypesPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLBiSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.learners.evaluation.Error;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.nodes.DegreeComparator;
import org.nodes.Node;
import org.nodes.algorithms.SlashBurn;
import org.nodes.rdf.InstanceHelper;
import org.nodes.util.MaxObserver;
import org.nodes.util.Functions.Dir;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class AffiliationExperiment extends RDFMLExperiment {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";


	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				i++;
				dataFile = args[i];
			}
		}		

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	

		int[] depths = {3};
		int[] iterations = {0,2,4,6};

		boolean inference = false;
		boolean forward = true;
		boolean relabel = true;


		createAffiliationPredictionDataSet(1);
		//dataset = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);
		//createGeoDataSet(1, 1, "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");




		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Error());
		evalFuncs.add(new F1());

		List<Double> target = EvaluationUtils.createTarget(labels);

		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Error());
		linParms.setDoCrossValidation(false);
		linParms.setNumFolds(10);

		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
		int[] wLabels = new int[counts.size()];
		double[] weights = new double[counts.size()];

		for (double label : counts.keySet()) {
			wLabels[(int) label - 1] = (int) label;
			weights[(int) label - 1] = 1 / counts.get(label);
		}
		linParms.setWeightLabels(wLabels);
		linParms.setWeights(weights);

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);

		List<Statement> allStmts = dataset.getStatements(null, null, null, inference);
		allStmts.removeAll(blackList);
		DTGraph<String,String> sGraph = org.nodes.data.RDF.createDirectedGraph(allStmts, null, null);
		System.out.println("Total nodes: " + sGraph.nodes().size());
		List<DTNode<String,String>> hubs = SlashBurn.getHubs(sGraph, 1, true);

		Comparator<DTNode<String,String>> comp = new SlashBurn.SignatureComparator<String,String>();
		MaxObserver<DTNode<String,String>> obs = new MaxObserver<DTNode<String,String>>(hubs.size() + instances.size(), comp);		
		obs.observe(sGraph.nodes());
		List<DTNode<String,String>> degreeHubs = new ArrayList<DTNode<String,String>>(obs.elements());

		Comparator<Node<String>> comp2 = new DegreeComparator<String>();
		MaxObserver<Node<String>> obs2 = new MaxObserver<Node<String>>(hubs.size() + instances.size(), comp2);
		obs2.observe(sGraph.nodes());
		List<DTNode<String,String>> nonSigDegreeHubs = new ArrayList<DTNode<String,String>>();
		for (Node<String> n : obs2.elements()) {
			nonSigDegreeHubs.add((DTNode<String,String>) n);
		}

		List<DTNode<String,String>> in = new ArrayList<DTNode<String,String>>();
		
		for (Resource i : instances) {
			in.add(sGraph.node(i.toString()));
		}
		
		
		/*
		List<DTGraph<String,String>> ihDepth = InstanceHelper.getInstances(sGraph, in, target, InstanceHelper.Method.DEPTH, 200, 4);	
		List<DTGraph<String,String>> ihUnInformed = InstanceHelper.getInstances(sGraph, in, target, InstanceHelper.Method.UNINFORMED, 200, 4);
		List<DTGraph<String,String>> ihInformed = InstanceHelper.getInstances(sGraph, in, target, InstanceHelper.Method.INFORMED, 200, 4);
		//*/

		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is = new HashSet<String>();
		for (Resource r : instances) {
			is.add(r.toString());
		}
		for (DTNode<String,String> n : sGraph.nodes()) {
			if (is.contains(n.label())) {
				rn.add(n);
			}
		}
		hubs.removeAll(rn);				
		degreeHubs.removeAll(rn);
		nonSigDegreeHubs.removeAll(rn);

		System.out.println("Total SB hubs: " + hubs.size());
		System.out.println(hubs);	
		System.out.println(degreeHubs);
		System.out.println(nonSigDegreeHubs);

		/*
		for (int i = 0; i < degreeHubs.size() && i < nonSigDegreeHubs.size(); i++) {
			if (!nonSigDegreeHubs.get(i).equals(degreeHubs.get(i))) {
				System.out.println(i + " " + nonSigDegreeHubs.get(i).label() + " " + degreeHubs.get(i).label());
			}
		}
		 */


		/*
		Map<String,Integer> dMap  = GraphUtils.createDegreeHubMap(degreeHubs, 300);
		Map<String,Integer> sbMap = GraphUtils.createHubMap(hubs, 300);

		for (String k : dMap.keySet()) {
			int l = dMap.get(k);
			if (sbMap.get(k) != l) {
				System.out.println("fail in level: " + l + " " + sbMap.get(k));
			}

		}
		 */


		//int[] hf = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};

		int[] hf = new int[1];
		for (int i = 0; i < hf.length; i++) {
			hf[i] = i+21;
		}

		/*
		for (int it : iterations) {
			resTable.newRow("WL DEPTH, it: " + it);
			MoleculeGraphExperiment<DTGraph<String,String>> exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, true), seeds, svmParms, ihDepth, target, evalFuncs);

			System.out.println("Running WL DEPTH, it: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		
		for (int it : iterations) {
			resTable.newRow("WL UN IN, it: " + it);
			MoleculeGraphExperiment<DTGraph<String,String>> exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, true), seeds, svmParms, ihUnInformed, target, evalFuncs);

			System.out.println("Running WL UN IN, it: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		
		for (int it : iterations) {
			resTable.newRow("WL INF, it: " + it);
			MoleculeGraphExperiment<DTGraph<String,String>> exp = new MoleculeGraphExperiment<DTGraph<String,String>>(new WLSubTreeKernel(it, true, true), seeds, svmParms, ihInformed, target, evalFuncs);

			System.out.println("Running WL INF, it: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		System.out.println(resTable);
		//*/
		
		
		
		
		///*	
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("RDF WL, " + i + ", " + it);

				RDFWLSubTreeKernel k = new RDFWLSubTreeKernel(it, i, inference, true, forward, false);

				KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);
		//*/


		///*
		for (int i : depths) {			
			for (int it : iterations) {
				resTable.newRow("RDF WL TYPE, " + i + ", " + it);

				RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, inference, true, forward);
				k.setHubMap(GraphUtils.createRDFTypeHubMap(dataset, inference));
				k.setRelabel(relabel);
				
				KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running WL RDF TYPE: " + i + " " + it);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);


		///*
		for (int h : hf) {
			for (int i : depths) {			
				for (int it : iterations) {
					resTable.newRow("RDF WL Regular Degree, " + h + ", " + i + ", " + it);

					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, inference, true, forward);
					k.setHubMap(GraphUtils.createNonSigHubMap(nonSigDegreeHubs, h));
					k.setRelabel(relabel);

					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

					System.out.println("Running WL RDF Regular Degree: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
		}
		System.out.println(resTable);
		//*/


		///*
		for (int h : hf) {
			for (int i : depths) {			
				for (int it : iterations) {
					resTable.newRow("RDF WL Signature Degree (SB), " + h + ", " + i + ", " + it);

					RDFWLSubTreeSlashBurnKernel k = new RDFWLSubTreeSlashBurnKernel(it, i, inference, true, forward);
					k.setHubMap(GraphUtils.createHubMap(hubs, h));
					k.setRelabel(relabel);

					KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

					System.out.println("Running WL RDF fwd SB: " + i + " " + it + " " + h);
					exp.run();

					for (Result res : exp.getResults()) {
						resTable.addResult(res);
					}	
				}
			}
		}
		System.out.println(resTable);
		//*/

		///*
		for (int i : depths) {			
			resTable.newRow("RDF IST, " + i);
			RDFIntersectionSubTreeKernel k = new RDFIntersectionSubTreeKernel(i, 1, inference, true);
			
			KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

			System.out.println("Running RDF IST: " + i);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}	
		}
		System.out.println(resTable);
		//*/

		///*
		for (int i : depths) {			
			resTable.newRow("RDF IST TYPE, " + i);
			RDFIntersectionSubTreeSlashBurnKernel k = new RDFIntersectionSubTreeSlashBurnKernel(i, 1, inference, true);
			k.setHubMap(GraphUtils.createRDFTypeHubMap(dataset, inference));

			KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

			System.out.println("Running RDF IST TYPE: " + i);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}	
		}
		System.out.println(resTable);
		//*/


		///*
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF IST Regular Degree, " + h + ", " + i);
				RDFIntersectionSubTreeSlashBurnKernel k = new RDFIntersectionSubTreeSlashBurnKernel(i, 1, inference, true);
				k.setHubMap(GraphUtils.createNonSigHubMap(nonSigDegreeHubs, h));

				KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running RDF IST Regular Degree: " + i + " " + h);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);
		//*/


		///*
		for (int h : hf) {
			for (int i : depths) {			
				resTable.newRow("RDF IST Signature Degree (SB), " + h + ", " + i);
				RDFIntersectionSubTreeSlashBurnKernel k = new RDFIntersectionSubTreeSlashBurnKernel(i, 1, inference, true);
				k.setHubMap(GraphUtils.createHubMap(hubs, h));

				KernelExperiment<RDFGraphKernel> exp = new RDFGraphKernelExperiment(k, seeds, svmParms, dataset, instances, target, blackList, evalFuncs);

				System.out.println("Running RDF IST Signature Degree (SB): " + i + " " + h);
				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}	
			}
		}
		System.out.println(resTable);
		//*/


	

		resTable.addCompResults(resTable.getBestResults());
		//resTable.addCompResults(table2.getBestResults());
		System.out.println(resTable);
		
		System.out.println(resTable.allScoresToString());
	}



	private static void createAffiliationPredictionDataSet(double frac) {
		Random rand = new Random(1);

		// Read in data set
		dataset = new RDFFileDataSet(dataFile, RDFFormat.N3);

		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		// initialize the lists of instances and labels
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			if (rand.nextDouble() <= frac) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}

		//capClassSize(20, 1);
		removeSmallClasses(5);
		// the blackLists data structure
		blackList = new ArrayList<Statement>();
		blackLists = new HashMap<Resource, List<Statement>>();

		// For each instance we add the triples that give the label of the instance (i.e. the URI of the affiliation)
		// In this case this is the affiliation triple and the reverse relation triple, which is the employs relation.
		for (Resource instance : instances) {
			blackList.addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			blackList.addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
		}

		for (Resource instance : instances) {
			blackLists.put(instance, blackList);
		}
	}

	private static void createGeoDataSet(long seed, double fraction, String property) {
		createGeoDataSet(seed, fraction, 10, property);
	}

	private static void createGeoDataSet(long seed, double fraction, int minSize, String property) {
		String majorityClass = "http://data.bgs.ac.uk/id/Lexicon/Class/LS";
		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		System.out.println(dataset.getLabel());

		System.out.println("Component Rock statements: " + stmts.size());
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() <= fraction) {
					instances.add(stmt2.getSubject());

					labels.add(stmt2.getObject());
					/*
				if (stmt2.getObject().toString().equals(majorityClass)) {
					labels.add(ds.createLiteral("pos"));
				} else {
					labels.add(ds.createLiteral("neg"));
				}
					 */
				}
			}
		}


		//capClassSize(50, seed);
		removeSmallClasses(minSize);
		createBlackList();

		Map<Value, Integer> labelMap = new HashMap<Value, Integer>();

		System.out.println(LibSVM.computeClassCounts(LibSVM.createTargets(labels, labelMap)));
		System.out.println(labelMap);
	}

}
