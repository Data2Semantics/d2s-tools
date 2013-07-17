package org.data2semantics.proppred;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.liblinear.LibLINEAR;
import org.data2semantics.proppred.learners.liblinear.LibLINEARModel;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.proppred.predictors.SVMPropertyPredictor;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

/**
 * This class shows how to build a classifier using RDFFeatureVectorKernel's
 * We do this without using the PropertyPredictor interface.
 * Since Example.java uses the AIFB dataset, we use the BGS data here.
 * Note that in this example all our instances already have labels, which would not be the case in a real world train/test scenario. 
 * In such a scenario both the train and test instances should all be in the list instances, but the list labels would only contain the labels of the train instances.
 * The current RDF graph kernels do not have a separate computeTestFeatureVectors method, which is a TODO.
 * 
 * @author Gerben
 *
 */
public class Example2 {
	private static String dataDir = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	

	public static void main(String[] args) {
		
		// Read in data set
		RDFFileDataSet dataset = new RDFFileDataSet(dataDir, RDFFormat.NTRIPLES);
		System.out.println("Files read.");

		// Extract all triples with the lithogenesis predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis", null, true);
		
		// initialize the lists of instances and labels
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		// The subjects of the triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}
		// Shuffle them, just to be sure
		Collections.shuffle(instances, new Random(1));
		Collections.shuffle(labels, new Random(1));

		List<Statement> blackList = new ArrayList<Statement>();
		
		// Create the blackList, we contains all triples giving information about the label of instances
		for (int i = 0; i < instances.size(); i++) {
			blackList.addAll(dataset.getStatements(instances.get(i), null, labels.get(i), true));
			if (labels.get(i) instanceof Resource) {
				blackList.addAll(dataset.getStatements((Resource) labels.get(i), null, instances.get(i), true));
			}
		}
		
		// Create the RDFFeatureVectorKernel that we are going to use
		RDFFeatureVectorKernel kernel = new RDFWLSubTreeKernel(6,3,true,true);
		
		// Compute feature vectors
		SparseVector[] featureVectors = kernel.computeFeatureVectors(dataset, instances, blackList);
		
		// Create a list of doubles as target
		List<Double> target = EvaluationUtils.createTarget(labels);

		// Initialize parameters object for LibLINEAR
		double[] cs = {1,10,100,1000}; // C values to optimize over.
		LibLINEARParameters linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		linParms.setDoCrossValidation(false);
		
		// Set the weights of the different classes, for the first 100 instances
		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target.subList(0,100));
		int[] wLabels = new int[counts.size()];
		double[] weights = new double[counts.size()];

		for (double label : counts.keySet()) {
			wLabels[(int) label - 1] = (int) label;
			weights[(int) label - 1] = 1 / counts.get(label);
		}
		linParms.setWeightLabels(wLabels);
		linParms.setWeights(weights);
		
		// Train model on the first 100 instances.
		LibLINEARModel model = LibLINEAR.trainLinearModel(Arrays.copyOfRange(featureVectors, 0, 100), EvaluationUtils.target2Doubles(target.subList(0, 100)), linParms);

		// Test on the rest of the data
		Prediction[] predictions = LibLINEAR.testLinearModel(model, Arrays.copyOfRange(featureVectors, 100, featureVectors.length));
		
		// Print out the predictions and compute the accuracy on the test set.
		List<Double> testLabels = target.subList(100, target.size());
		int correct = 0;
		for (int i = 0; i < predictions.length; i++) {
			System.out.println("Label: " + testLabels.get(i) + ", Predicted: " + predictions[i].getLabel());
			correct = (testLabels.get(i).equals(predictions[i].getLabel())) ? correct + 1 : correct;
		}
		System.out.println("Accuracy: " + ((double) correct) / (double) predictions.length);	
		

	}

}
