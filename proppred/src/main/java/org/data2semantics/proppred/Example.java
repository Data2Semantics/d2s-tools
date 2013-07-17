package org.data2semantics.proppred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.predictors.SVMPropertyPredictor;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;


/**
 * This class describes a detailed example of using the RDF property prediction library.
 *  
 * @author Gerben
 *
 */
public class Example {
	
	public static void main(String[] args) {
		
		// Read in data set
		RDFFileDataSet dataset = new RDFFileDataSet("src/test/resources/aifb-fixed_complete.n3", RDFFormat.N3);
		System.out.println("File read.");

		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);
		
		// initialize the lists of instances and labels
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}
		// Shuffle them, just to be sure
		Collections.shuffle(instances, new Random(1));
		Collections.shuffle(labels, new Random(1));

		// the blackLists data structure
		Map<Resource, List<Statement>> blackLists = new HashMap<Resource, List<Statement>>();

		// For each instance we add the triples that give the label of the instance (i.e. the URI of the affiliation)
		// In this case this is the affiliation triple and the reverse relation triple, which is the employs relation.
		for (Resource instance : instances) {
			List<Statement> bl = new ArrayList<Statement>();
			bl.addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			bl.addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
			blackLists.put(instance, bl);
		}

		// Create a new SVM property predictor
		SVMPropertyPredictor pred = new SVMPropertyPredictor();
		
		// Train on the first 100 instances
		pred.train(dataset, instances.subList(0, 100), labels.subList(0, 100), blackLists);
		
		// Predict on the rest, note that we also need the blacklists here, since the labels already occur in the test set as well,
		// because we do not use an unlabeled test set, which we would use in an application setting.
		List<Value> predictions = pred.predict(dataset, instances.subList(100,instances.size()), blackLists);
		
		// Make a sublist of the labels of the test set
		List<Value> testLabels = labels.subList(100, labels.size());

		// Print out the predictions and compute the accuracy on the test set.
		int correct = 0;
		for (int i = 0; i < predictions.size(); i++) {
			System.out.println("Label: " + testLabels.get(i).toString() + ", Predicted: " + predictions.get(i).toString());
			correct = (testLabels.get(i).toString().equals(predictions.get(i).toString())) ? correct + 1 : correct;
		}
		System.out.println("Accuracy: " + ((double) correct) / (double) predictions.size());	
	}
	

}
