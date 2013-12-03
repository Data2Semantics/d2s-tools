package org.data2semantics.exp.molecules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.data2semantics.exp.utils.RDFOldKernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.exp.utils.ResultsTable;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLBiSubTreeKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.nodes.UGraph;

public class MoleculeExperiment {
	public static final String MUTAG_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\mutag\\";
	public static final String ENZYMES_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\graph_molecule_data\\enzymes\\";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<UGraph<String>> graphs = new ArrayList<UGraph<String>>();
		List<Double> labels = new ArrayList<Double>();

		//createDataSetMUTAG(graphs, labels);
		createDataSetENZYMES(graphs, labels);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		//double[] cs = {0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000};	
		double[] cs = {1,10,100};	


		
		int[] iterations = {0,1,2,3,4,5,6,7,8,9,10};
	
		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);
		
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());


		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);

		
		for (int it : iterations) {
			resTable.newRow("WL MUTAG, it: " + it);
			MoleculeGraphExperiment exp = new MoleculeGraphExperiment(new WLUSubTreeKernel(it, true), seeds, svmParms, graphs, labels, evalFuncs);


			System.out.println("Running WL MUTAG: " + it);
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}

		System.out.println(resTable);
	}

	private static void createDataSetENZYMES(List<UGraph<String>> graphs, List<Double> labels) {
		try {
			File dir = new File(ENZYMES_DIR);
			for (String fileName : dir.list()) {
				graphs.add(GraphUtils.readMoleculeGraph(ENZYMES_DIR + fileName));
				labels.add(getGraphLabel(ENZYMES_DIR + fileName));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void createDataSetMUTAG(List<UGraph<String>> graphs, List<Double> labels) {
		try {
			File dir = new File(MUTAG_DIR);
			for (String fileName : dir.list()) {
				graphs.add(GraphUtils.readMoleculeGraph(MUTAG_DIR + fileName));
				labels.add(getGraphLabel(MUTAG_DIR + fileName));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Double getGraphLabel(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();
			while (!line.startsWith("#c")) {
				line = reader.readLine();
			}
			String ret = reader.readLine(); // next line is class
			reader.close();

			return Double.parseDouble(ret);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}
}


