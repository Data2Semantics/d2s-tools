package org.data2semantics.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.experiments.BinaryPropertyPredictionDataSetParameters;
import org.data2semantics.tools.experiments.PropertyPredictionExperiment;
import org.data2semantics.tools.experiments.DataSetFactory;
import org.data2semantics.tools.experiments.PropertyPredictionDataSetParameters;
import org.data2semantics.tools.experiments.ExperimentResults;
import org.data2semantics.tools.experiments.Experimenter;
import org.data2semantics.tools.experiments.PropertyPredictionDataSet;
import org.data2semantics.tools.experiments.ResultsTable;
import org.data2semantics.tools.kernels.IntersectionGraphPathKernel;
import org.data2semantics.tools.kernels.IntersectionGraphWalkKernel;
import org.data2semantics.tools.kernels.IntersectionPartialSubTreeKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class CommitteeMemberPredictionExperiment {
	private final static String DATA_DIR = "D:\\workspaces\\datasets\\";
	private final static int NUMBER_OF_PROC = 6;

	public static void main(String[] args) {
		RDFDataSet testSetA = new RDFFileDataSet(DATA_DIR + "eswc-2012-complete.rdf", RDFFormat.RDFXML);

		List<BinaryPropertyPredictionDataSetParameters> dataSetsParams = new ArrayList<BinaryPropertyPredictionDataSetParameters>();

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.01, 0.1, 1, 10, 100};	

		///*
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 1, false, false));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, false, false));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 3, false, false));
		
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 1, false, true));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, false, true));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 3, false, true));
		
		//*/

		///*
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 1, true, false));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, true, false));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 3, true, false));
		
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 1, true, true));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, true, true));
		dataSetsParams.add(new BinaryPropertyPredictionDataSetParameters(testSetA, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 3, true, true));
		
		//*/


		PropertyPredictionDataSet dataset;
		PropertyPredictionExperiment exp;

		ResultsTable resultsWL  = new ResultsTable();
		ResultsTable resultsSTF = new ResultsTable();
		ResultsTable resultsSTP = new ResultsTable();
		ResultsTable resultsIGW = new ResultsTable();
		ResultsTable resultsIGP = new ResultsTable();

		Experimenter experimenter = new Experimenter(NUMBER_OF_PROC);
		Thread expT = new Thread(experimenter);
		expT.setDaemon(true);
		expT.start();



		try {
			for (BinaryPropertyPredictionDataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createPropertyPredictionDataSet(params);
				dataset.removeSmallClasses(5);
				//dataset.removeVertexAndEdgeLabels();
				
				resultsWL.newRow(dataset.getLabel() + " WLSubTreeKernel");
				for (int i = 0; i < 4; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "WL" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new WLSubTreeKernel(i), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWL.addResult(exp.getResults().getAccuracy());
						resultsWL.addResult(exp.getResults().getF1());
					}
				}
				
				/*
				resultsSTF.newRow(dataset.getLabel() + " IntersectionFullSubTree");
				for (int i = 0; i < 3; i++) {
					
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionFullSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionSubTreeKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTF.addResult(exp.getResults().getAccuracy());
						resultsSTF.addResult(exp.getResults().getF1());
					}
				}
				
				resultsSTP.newRow(dataset.getLabel() + " IntersectionPartialSubTree");
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionPartialSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionPartialSubTreeKernel(i, 0.01), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTP.addResult(exp.getResults().getAccuracy());
						resultsSTP.addResult(exp.getResults().getF1());
					}
				}
				
				resultsIGP.newRow(dataset.getLabel() + " IntersectionGraphPath");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphPath" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphPathKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGP.addResult(exp.getResults().getAccuracy());
						resultsIGP.addResult(exp.getResults().getF1());
					}
				}				
				
				resultsIGW.newRow(dataset.getLabel() + " IntersectionGraphWalk");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphWalk" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphWalkKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGW.addResult(exp.getResults().getAccuracy());
						resultsIGW.addResult(exp.getResults().getF1());
					}
				}
				*/
				
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		experimenter.stop();

		while (expT.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println(resultsWL);
		//System.out.println(resultsSTF);
		//System.out.println(resultsSTP);
		//System.out.println(resultsIGW);
		//System.out.println(resultsIGP);




	}

}

