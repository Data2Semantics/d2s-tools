package org.data2semantics.proppred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.data2semantics.proppred.kernels.Pair;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFPairKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

/**
 * This class gives an example of the use of the RDFPairKernel
 * (Note that the actual results of the task are not very good...) 
 * 
 * @author Gerben
 *
 */
public class ExamplePair {

	public static void main(String[] args) {
		// Read in data set
		RDFFileDataSet dataset = new RDFFileDataSet("src/test/resources/aifb-fixed_complete.n3", RDFFormat.N3);

		// Random settings
		long seed = 1;
		Random rand = new Random(seed);
		double fraction = 0.2;

		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

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
		List<Pair<Resource>> instances = new ArrayList<Pair<Resource>>();
		List<Value> labels = new ArrayList<Value>();
		List<Pair<Resource>> allInstances = new ArrayList<Pair<Resource>>();
		allInstances.addAll(posSet);
		allInstances.addAll(negSet);
		
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
	
		// Create the blacklist
		List<Statement> blacklist = new ArrayList<Statement>();
		for (int i = 0; i < allInstances.size(); i++) {
			blacklist.addAll(dataset.getStatements(allInstances.get(i).getFirst(), null, allInstances.get(i).getSecond(), true));
			blacklist.addAll(dataset.getStatements(allInstances.get(i).getSecond(), null, allInstances.get(i).getFirst(), true));		
		}
	
		// create a list of doubles as train target
		List<Double> target = EvaluationUtils.createTarget(labels);

		RDFPairKernel kernel = new RDFPairKernel();
		RDFWLSubTreeKernel k1 = new RDFWLSubTreeKernel(4,2,true,true);
		RDFWLSubTreeKernel k2 = new RDFWLSubTreeKernel(4,2,true,true);
		
		// Compute the kernel
		double[][] matrix = kernel.compute(dataset, instances, blacklist, k1, k2);
		
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};
		LibSVMParameters parms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		
		parms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		parms.setWeights(EvaluationUtils.computeWeights(target));
		
		// For simplicity we do CV, but kernel can also be split in train/test split, which is slightly more involved.
		Prediction[] pred = LibSVM.crossValidate(matrix, EvaluationUtils.target2Doubles(target), parms, 5);

		System.out.println("Acc: " + (new Accuracy()).computeScore(EvaluationUtils.target2Doubles(target), pred));
		System.out.println("F1:  " + (new F1()).computeScore(EvaluationUtils.target2Doubles(target), pred));
		
	}
}
