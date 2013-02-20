package org.data2semantics.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.experiments.BinaryPropertyPredictionDataSetParameters;
import org.data2semantics.tools.experiments.GeneralPredictionDataSetParameters;
import org.data2semantics.tools.experiments.PropertyPredictionExperiment;
import org.data2semantics.tools.experiments.DataSetFactory;
import org.data2semantics.tools.experiments.PropertyPredictionDataSetParameters;
import org.data2semantics.tools.experiments.ExperimentResults;
import org.data2semantics.tools.experiments.Experimenter;
import org.data2semantics.tools.experiments.PropertyPredictionDataSet;
import org.data2semantics.tools.experiments.Result;
import org.data2semantics.tools.experiments.ResultsTable;
import org.data2semantics.tools.kernels.IntersectionGraphPathKernel;
import org.data2semantics.tools.kernels.IntersectionGraphWalkKernel;
import org.data2semantics.tools.kernels.IntersectionPartialSubTreeKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;

public class BibleReligionPredictionExperiment {
	private final static String DATA_DIR = "D:\\workspaces\\datasets\\bible\\";
	private final static int NUMBER_OF_PROC = 3;

	public static void main(String[] args) {
		RDFFileDataSet testSetA = new RDFFileDataSet(DATA_DIR + "NTN-individuals.owl", RDFFormat.RDFXML);
		testSetA.addFile(DATA_DIR + "NTNames.owl", RDFFormat.RDFXML);
		
		List<URI> instancesJ = new ArrayList<URI>();
		List<URI> instancesC = new ArrayList<URI>();
		List<String> labels = new ArrayList<String>();
		Map<URI, List<Statement>> blacklists = new HashMap<URI, List<Statement>>();
		
		
		List<Statement> triples = testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#ethnicity", "http://semanticbible.org/ns/2006/NTNames#Jewish", true);			
		for (Statement triple: triples) {
			List<Statement> triples2 = testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#religiousBelief", "http://semanticbible.org/ns/2006/NTNames#Christianity", true);
			
			if (triples2.size() == 0) {
				instancesJ.add((URI)triple.getSubject());
				labels.add("J");
				
				List<Statement> bl = testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#ethnicity", null, true);
				bl.addAll(testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#religiousBelief", null, true));
				bl.addAll(testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#ethnicityOf", triple.getSubject().toString(), true));
				
				blacklists.put((URI)triple.getSubject(), bl);
			}
		}
		
		triples = testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#religiousBelief", "http://semanticbible.org/ns/2006/NTNames#Christianity", true);			
		for (Statement triple: triples) {
			List<Statement> triples2 = testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#ethnicity", "http://semanticbible.org/ns/2006/NTNames#Jewish", true);
			
			if (triples2.size() == 0) {
				instancesC.add((URI)triple.getSubject());
				labels.add("C");
				
				List<Statement> bl = testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#ethnicity", null, true);
				bl.addAll(testSetA.getStatementsFromStrings(triple.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#religiousBelief", null, true));
				bl.addAll(testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#ethnicityOf", triple.getSubject().toString(), true));
				blacklists.put((URI)triple.getSubject(), bl);
		
			}
		}
		
		List<URI> instances = new ArrayList<URI>();
		instances.addAll(instancesJ);
		instances.addAll(instancesC);
		
		System.out.println("J and C: " + instancesJ.size() + " " + instancesC.size());
		



		List<GeneralPredictionDataSetParameters> dataSetsParams = new ArrayList<GeneralPredictionDataSetParameters>();

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	
		int maxClassSize = 50;


	
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 1, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 2, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 3, false, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 4, false, false));
		
		/*
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 1, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 2, false, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 3, false, true));
		
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 1, true, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 2, true, false));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 1, true, true));
		dataSetsParams.add(new GeneralPredictionDataSetParameters(testSetA, blacklists, instances, 2, true, true));
		*/
		


		

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
			for (GeneralPredictionDataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createPropertyPredictionDataSet(params);
				//dataset.removeSmallClasses(5);
				dataset.setLabels(labels);
				//dataset.removeVertexAndEdgeLabels();

				
				/*
				resultsWL.newRow(dataset.getLabel() + " WLSubTreeKernel");
				for (int i = 0; i < 7; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "WL" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new WLSubTreeKernel(i), seeds, cs, maxClassSize, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWL.addResult(exp.getResults().getAccuracy());
						resultsWL.addResult(exp.getResults().getF1());
					}
				}


				
				resultsSTF.newRow(dataset.getLabel() + " IntersectionFullSubTree");
				for (int i = 0; i < 5; i++) {

					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionFullSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionSubTreeKernel(i, 1), seeds, cs, maxClassSize, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTF.addResult(exp.getResults().getAccuracy());
						resultsSTF.addResult(exp.getResults().getF1());
					}
				}

				resultsSTP.newRow(dataset.getLabel() + " IntersectionPartialSubTree");
				for (int i = 0; i < 5; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionPartialSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionPartialSubTreeKernel(i, 0.01), seeds, cs, maxClassSize, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTP.addResult(exp.getResults().getAccuracy());
						resultsSTP.addResult(exp.getResults().getF1());
					}
				}

				//*/


				
				resultsIGP.newRow(dataset.getLabel() + " IntersectionGraphPath");
				for (int i = 1; i < 5; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphPath" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphPathKernel(i, 1), seeds, cs, maxClassSize, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGP.addResult(exp.getResults().getAccuracy());
						resultsIGP.addResult(exp.getResults().getF1());
					}
				}				

				resultsIGW.newRow(dataset.getLabel() + " IntersectionGraphWalk");
				for (int i = 1; i < 5; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphWalk" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphWalkKernel(i, 1), seeds, cs, maxClassSize, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGW.addResult(exp.getResults().getAccuracy());
						resultsIGW.addResult(exp.getResults().getF1());
					}
				}
				//*/				

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

		try {
			int fileId = (int) (Math.random() * 100000000);	
			File file = new File(DATA_DIR + fileId + "_" + "all_results" + ".txt");
			PrintWriter fileOut = new PrintWriter(new FileOutputStream(file));

			List<Result> bestResults = new ArrayList<Result>();
			
			bestResults = resultsWL.getBestResults(bestResults);
			bestResults = resultsSTF.getBestResults(bestResults);
			bestResults = resultsSTP.getBestResults(bestResults);
			bestResults = resultsIGW.getBestResults(bestResults);
			bestResults = resultsIGP.getBestResults(bestResults);
			
			resultsWL.addCompResults(bestResults);
			resultsSTF.addCompResults(bestResults);
			resultsSTP.addCompResults(bestResults);
			resultsIGW.addCompResults(bestResults);
			resultsIGP.addCompResults(bestResults);
						
			
			fileOut.println(resultsWL);
			fileOut.println(resultsSTF);
			fileOut.println(resultsSTP);
			fileOut.println(resultsIGW);
			fileOut.println(resultsIGP);

			fileOut.println(resultsWL.allScoresToString());
			fileOut.println(resultsSTF.allScoresToString());
			fileOut.println(resultsSTP.allScoresToString());
			fileOut.println(resultsIGW.allScoresToString());
			fileOut.println(resultsIGP.allScoresToString());

			System.out.println(resultsWL);
			System.out.println(resultsSTF);
			System.out.println(resultsSTP);
			System.out.println(resultsIGW);
			System.out.println(resultsIGP);

			System.out.println(resultsWL.allScoresToString());
			System.out.println(resultsSTF.allScoresToString());
			System.out.println(resultsSTP.allScoresToString());
			System.out.println(resultsIGW.allScoresToString());
			System.out.println(resultsIGP.allScoresToString());


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

	}

}


