package org.data2semantics.proppred;

import java.util.List;
import java.util.Map;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * Interface defining the methods that a PropertyPredictor should implement. These are train and predict methods.
 * Both methods optionally taking a Map from Resource's to a List of Statement's which indicate for each instance (i.e. the Resource) 
 * which RDF Statements in the graph should be ignored, e.g. to avoid overfitting on the training data. 
 * Typically these Statements describe the relations between the instance Resource and the label Value.
 * 
 * @author Gerben
 *
 */
public interface PropertyPredictor {

	/**
	 * train a Predictor on the RDF dataset dataset for the given instances and labels.
	 * 
	 * @param dataset, RDF dataset
	 * @param instances, List of instances to train on, these instances should exist in the given RDF dataset
	 * @param labels, List of labels corresponding 1-to-1 the list of instances
	 */
	public void train(RDFDataSet dataset, List<Resource> instances, List<Value> labels);
	
	/**
	 * Train a predictor and for each instance Resource ignore the Statements belong to that Resource as described by the Map blackLists.
	 * 
	 * @param dataset
	 * @param instances
	 * @param labels
	 * @param blackLists
	 */
	public void train(RDFDataSet dataset, List<Resource> instances, List<Value> labels, Map<Resource, List<Statement>> blackLists);
	
	/**
	 * Use a trained predictor to predict labels for a set of instances. Note that the train method should be called first.
	 * 
	 * @param dataset
	 * @param instances
	 * @return a list of Value's, which are predictions for the given instances
	 */
	public List<Value> predict(RDFDataSet dataset, List<Resource> instances);
	
	/**
	 * Predict labels for a set of instances. Like the train method a blackLists Map can be used to indicate which Statements to ignore for each instance.
	 * This is useful in a testing scenario, in which the RDF dataset actually contains the Statements that give the label for each Resource.
	 * 
	 * @param dataset
	 * @param instances
	 * @param blackLists
	 * @return a list of Value's, which are predictions for the given instances
	 */
	public List<Value> predict(RDFDataSet dataset, List<Resource> instances, Map<Resource, List<Statement>> blackLists);

}

