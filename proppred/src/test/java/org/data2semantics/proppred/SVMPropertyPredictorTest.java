package org.data2semantics.proppred;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class SVMPropertyPredictorTest {

	@Test
	public void test() {
		RDFFileDataSet dataset = new RDFFileDataSet("src/test/resources/aifb-fixed_complete.n3", RDFFormat.N3);
		
		System.out.println("File read.");
		
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();
		
		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}
		Collections.shuffle(instances, new Random(1));
		Collections.shuffle(labels, new Random(1));
		
		
		System.out.print("Removing label statements");
		for (Resource instance : instances) {
			dataset.removeStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null);
			dataset.removeStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString());
			
			System.out.print(".");
		}
		System.out.println();
		
		
		
		SVMPropertyPredictor pred = new SVMPropertyPredictor();
		pred.train(dataset, instances.subList(0, 100), labels.subList(0, 100));
		List<Value> predictions = pred.predict(dataset, instances.subList(100,instances.size()));
		List<Value> testLabels = labels.subList(100, labels.size());
		
		int correct = 0;
		
		for (int i = 0; i < predictions.size(); i++) {
			System.out.println("Label: " + testLabels.get(i).toString() + ", Predicted: " + predictions.get(i).toString());
			
			correct = (testLabels.get(i).toString().equals(predictions.get(i).toString())) ? correct + 1 : correct;
		}
		
		System.out.println("Accuracy: " + ((double) correct) / (double) predictions.size());
		
		//fail("Not yet implemented");
	}

}
