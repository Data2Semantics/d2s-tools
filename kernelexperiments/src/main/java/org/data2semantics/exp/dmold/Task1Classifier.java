package org.data2semantics.exp.dmold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.RDFMLExperiment;
import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFCombinedKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgePathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionTreeEdgeVertexPathWithTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFSimpleTextKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernelTree;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeWithTextKernel;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.evaluation.MeanAbsoluteError;
import org.data2semantics.proppred.learners.evaluation.MeanSquaredError;
import org.data2semantics.proppred.learners.evaluation.Task1Score;
import org.data2semantics.proppred.learners.evaluation.Task1ScoreForBins;
import org.data2semantics.proppred.learners.evaluation.Task1ScoreForBothBins;
import org.data2semantics.proppred.learners.liblinear.LibLINEAR;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.text.TextUtils;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.data2semantics.tools.rdf.RDFMultiDataSet;
import org.data2semantics.tools.rdf.RDFSparqlDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

public class Task1Classifier extends RDFMLExperiment {
	private static List<Resource> testInstances;
	
	public static void main(String[] args) {
		createTask1DataSet();

		//double[] bins = {-0.5, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 7.5, 9.5, 14.5, 75.5};
		//double[] bins = {0.5, 1.5, 3.5, 6.5, 22.5};
		double[] bins = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 12.5, 15.5, 18.5, 23.5};

		double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	
		double[] ps1 = {1};
		
	
		boolean inference = true;

		List<Double> targetBins = new ArrayList<Double>();	

		for (Value label : labels) {
			double val = LiteralUtil.getDoubleValue(label,0);

			for (int i=0; i < bins.length-1; i++) {
				if (val > bins[i] && val <= bins[i+1]) {
					targetBins.add(i+1.0);
				}
			}
		}


		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setEvalFunction(new Task1ScoreForBothBins(bins));
		linParms.setDoCrossValidation(true);
		linParms.setNumFolds(5);
		linParms.setEps(0.001);
		linParms.setPs(ps1);

		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(targetBins);
		int[] wLabels = new int[counts.size()];
		double[] weights = new double[counts.size()];

		for (double label : counts.keySet()) {
			wLabels[(int) label - 1] = (int) label;
			weights[(int) label - 1] = 1 / counts.get(label);
		}
		linParms.setWeightLabels(wLabels);
		linParms.setWeights(weights);

		
		RDFFeatureVectorKernel kernel = new RDFWLSubTreeWithTextKernel(4, 2, inference, false);
		
		List<Resource> allInstances = new ArrayList<Resource>(instances);
		allInstances.addAll(testInstances);
		
		System.out.println("Computing kernel....");
		SparseVector[] fv = kernel.computeFeatureVectors(dataset, allInstances, blackList);
		System.out.println("Computing TFIDF....");
		fv = TextUtils.computeTFIDF(Arrays.asList(fv)).toArray(new SparseVector[1]);
		fv = KernelUtils.normalize(fv);
		
		SparseVector[] trainFV = Arrays.copyOfRange(fv, 0, instances.size());
		SparseVector[] testFV = Arrays.copyOfRange(fv, instances.size(), allInstances.size());
		
		double[] targetA = new double[targetBins.size()];
		
		for (int i = 0; i < targetBins.size(); i++) {
			targetA[i] = targetBins.get(i);
		}
		
		Prediction[] pred = LibLINEAR.testLinearModel(LibLINEAR.trainLinearModel(trainFV, targetA, linParms), testFV);
		
		

		for (int i = 0; i < pred.length; i++) {
			double val = (bins[(int) pred[i].getLabel()] + bins[(int) pred[i].getLabel()-1]) / 2.0;
			System.out.println(testInstances.get(i) + ", " + val);
		}

	}

	private static void createTask1DataSet() {
		RDFFileDataSet train = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\LDMC_Task1_train.ttl", RDFFormat.TURTLE);
		RDFFileDataSet test = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\LDMC_Task1_test.ttl", RDFFormat.TURTLE);
		
		RDFFileDataSet d = new RDFFileDataSet("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\LDMC_Task1_train.ttl", RDFFormat.TURTLE);
		d.addFile("C:\\Users\\Gerben\\Dropbox\\D2S\\Task1\\LDMC_Task1_test.ttl", RDFFormat.TURTLE);
		dataset = d;

		List<Statement> stmts = train.getStatementsFromStrings(null, RDF.TYPE.toString(), "http://purl.org/procurement/public-contracts#Contract");
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = train.getStatementsFromStrings(stmt.getSubject().toString(), "http://purl.org/procurement/public-contracts#numberOfTenders", null);

			for (Statement stmt2 : stmts2) {
				instances.add(stmt2.getSubject());
				labels.add(stmt2.getObject());			
			}
		}

		removeSmallClasses(5);
		createBlackList();
		
		stmts = test.getStatementsFromStrings(null, RDF.TYPE.toString(), "http://purl.org/procurement/public-contracts#Contract");
		
		testInstances = new ArrayList<Resource>();
		
		for(Statement stmt: stmts) {
			testInstances.add(stmt.getSubject());
		}

	}


}
