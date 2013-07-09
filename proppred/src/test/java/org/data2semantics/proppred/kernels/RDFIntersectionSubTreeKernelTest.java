package org.data2semantics.proppred.kernels;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFIntersectionPartialSubTreeKernel;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

import cern.colt.Arrays;

public class RDFIntersectionSubTreeKernelTest {

	@Test
	public void test() {
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
		List<Statement> blackList = new ArrayList<Statement>();

		// For each instance we add the triples that give the label of the instance (i.e. the URI of the affiliation)
		// In this case this is the affiliation triple and the reverse relation triple, which is the employs relation.
		for (Resource instance : instances) {
			blackList.addAll(dataset.getStatementsFromStrings(instance.toString(), "http://swrc.ontoware.org/ontology#affiliation", null));
			blackList.addAll(dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#employs", instance.toString()));
		}
		RDFIntersectionPartialSubTreeKernel kernel = new RDFIntersectionPartialSubTreeKernel();
		double[][] matrix = kernel.compute(dataset, instances, blackList);
		
		for (int i = 0; i < matrix.length; i++) {
			System.out.println(Arrays.toString(matrix[i]));
		}
	}

}
