package org.data2semantics.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.openrdf.rio.RDFFormat;

public class AffiliationPredictionExperiment {
	private final static String DATA_DIR = "D:\\workspaces\\datasets\\aifb\\";
	private final static int NUMBER_OF_PROC = 4;



	public static void main(String[] args) {
		RDFDataSet testSetA = new RDFFileDataSet(DATA_DIR + "aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		RDFDataSet testSetB = new RDFFileDataSet(DATA_DIR + "aifb-fixed_no_schema.n3", RDFFormat.N3);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.001, 0.01, 0.1, 1, 10, 100, 1000};	

		PropertyPredictionDataSet dataset;
		PropertyPredictionExperiment exp;
		
		Experimenter experimenter = new Experimenter(NUMBER_OF_PROC);
		Thread expT = new Thread(experimenter);
		expT.setDaemon(true);
		expT.start();

		ResultsTable resultsWL = new ResultsTable();
		ResultsTable resultsSTF = new ResultsTable();
		ResultsTable resultsSTP = new ResultsTable();
		ResultsTable resultsIGW = new ResultsTable();
		ResultsTable resultsIGP = new ResultsTable();

		ResultsTable resultsWLadd = new ResultsTable();
		ResultsTable resultsSTFadd = new ResultsTable();
		ResultsTable resultsSTPadd = new ResultsTable();
		ResultsTable resultsIGWadd = new ResultsTable();
		ResultsTable resultsIGPadd = new ResultsTable();

		
		/**  
		 * FIRST EXPERIMENT, STANDARD SETTINGS
		 * 
		 */		
		List<PropertyPredictionDataSetParameters> dataSetsParams = new ArrayList<PropertyPredictionDataSetParameters>();

		
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, true));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, true));
		
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, true, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, true, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, true, true));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, true, true));
		
		/*
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, true));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, true));
		
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, true, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, true, false));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, true, true));
		dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, true, true));
		*/
		
		
	

		try {
			for (PropertyPredictionDataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createPropertyPredictionDataSet(params);
				dataset.removeSmallClasses(5);
				dataset.removeVertexAndEdgeLabels();

				resultsWL.newRow(dataset.getLabel() + " WLSubTreeKernel");
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {	
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "WL" + "_" + i + ".txt");
						WLSubTreeKernel kernel = new WLSubTreeKernel(i, true, false);
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), kernel, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWL.addResult(exp.getResults().getAccuracy());
						resultsWL.addResult(exp.getResults().getF1());
						
						System.out.println("Running WL, it " + i + " on " + dataset.getLabel());
					}
				}

				
				resultsSTF.newRow(dataset.getLabel() + " IntersectionFullSubTree");
				for (int i = 0; i < 3; i++) {

					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionFullSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionSubTreeKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTF.addResult(exp.getResults().getAccuracy());
						resultsSTF.addResult(exp.getResults().getF1());
						
						System.out.println("Running STF, it " + i + " on " + dataset.getLabel());
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
						
						System.out.println("Running STP, it " + i + " on " + dataset.getLabel());
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
				
			}
			

			/******
			 * ADDITIONAL EXPERIMENTS
			 */
			dataSetsParams = new ArrayList<PropertyPredictionDataSetParameters>();
			
			
			
			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, false));
			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, false));
			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 3, false, false));

			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 1, false, true));
			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 2, false, true));
			dataSetsParams.add(new PropertyPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 3, false, true));
			

			
			for (PropertyPredictionDataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createPropertyPredictionDataSet(params);
				dataset.removeSmallClasses(5);
				dataset.removeVertexAndEdgeLabels();

				resultsWLadd.newRow(dataset.getLabel() + " WLSubTreeKernel");
				for (int i = 0; i < 4; i++) {
					if (experimenter.hasSpace()) {	
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "WL" + "_" + i + ".txt");
						WLSubTreeKernel kernel = new WLSubTreeKernel(i, true, false);
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), kernel, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWLadd.addResult(exp.getResults().getAccuracy());
						resultsWLadd.addResult(exp.getResults().getF1());
					}
				}

				
				resultsSTFadd.newRow(dataset.getLabel() + " IntersectionFullSubTree");
				for (int i = 0; i < 4; i++) {

					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionFullSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionSubTreeKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTFadd.addResult(exp.getResults().getAccuracy());
						resultsSTFadd.addResult(exp.getResults().getF1());
					}

				}

				resultsSTPadd.newRow(dataset.getLabel() + " IntersectionPartialSubTree");
				for (int i = 0; i < 4; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionPartialSubTree" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionPartialSubTreeKernel(i, 0.01), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTPadd.addResult(exp.getResults().getAccuracy());
						resultsSTPadd.addResult(exp.getResults().getF1());
					}
				}
				
				resultsIGPadd.newRow(dataset.getLabel() + " IntersectionGraphPath");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphPath" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphPathKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGPadd.addResult(exp.getResults().getAccuracy());
						resultsIGPadd.addResult(exp.getResults().getF1());
					}
				}				

				resultsIGWadd.newRow(dataset.getLabel() + " IntersectionGraphWalk");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphWalk" + "_" + i + ".txt");
						exp = new PropertyPredictionExperiment(new PropertyPredictionDataSet(dataset), new IntersectionGraphWalkKernel(i, 1), seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsIGWadd.addResult(exp.getResults().getAccuracy());
						resultsIGWadd.addResult(exp.getResults().getF1());
					}
				}
			}
			

			
			
			
		/***********
		 * END OF EXPERIMENTER
		 * 
		 *  
		 */
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

		/********************************
		 * PRINT OUT OF RESULTS
		 * 
		 **/
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
			
			bestResults = resultsWLadd.getBestResults(bestResults);
			bestResults = resultsSTFadd.getBestResults(bestResults);
			bestResults = resultsSTPadd.getBestResults(bestResults);
			bestResults = resultsIGWadd.getBestResults(bestResults);
			bestResults = resultsIGPadd.getBestResults(bestResults);
			
			
			resultsWL.addCompResults(bestResults);
			resultsSTF.addCompResults(bestResults);
			resultsSTP.addCompResults(bestResults);
			resultsIGW.addCompResults(bestResults);
			resultsIGP.addCompResults(bestResults);
			
			resultsWLadd.addCompResults(bestResults);
			resultsSTFadd.addCompResults(bestResults);
			resultsSTPadd.addCompResults(bestResults);
			resultsIGWadd.addCompResults(bestResults);
			resultsIGPadd.addCompResults(bestResults);
			
			
			fileOut.println(resultsWL);
			fileOut.println(resultsSTF);
			fileOut.println(resultsSTP);
			fileOut.println(resultsIGW);
			fileOut.println(resultsIGP);

			fileOut.println(resultsWLadd);
			fileOut.println(resultsSTFadd);
			fileOut.println(resultsSTPadd);
			fileOut.println(resultsIGWadd);
			fileOut.println(resultsIGPadd);
			
			
			fileOut.println(resultsWL.allScoresToString());
			fileOut.println(resultsSTF.allScoresToString());
			fileOut.println(resultsSTP.allScoresToString());
			fileOut.println(resultsIGW.allScoresToString());
			fileOut.println(resultsIGP.allScoresToString());

			fileOut.println(resultsWLadd.allScoresToString());
			fileOut.println(resultsSTFadd.allScoresToString());
			fileOut.println(resultsSTPadd.allScoresToString());
			fileOut.println(resultsIGWadd.allScoresToString());
			fileOut.println(resultsIGPadd.allScoresToString());
			
			
			fileOut.close();

			System.out.println(resultsWL);
			System.out.println(resultsSTF);
			System.out.println(resultsSTP);
			System.out.println(resultsIGW);
			System.out.println(resultsIGP);

			System.out.println(resultsWLadd);
			System.out.println(resultsSTFadd);
			System.out.println(resultsSTPadd);
			System.out.println(resultsIGWadd);
			System.out.println(resultsIGPadd);
			
			System.out.println(resultsWL.allScoresToString());
			System.out.println(resultsSTF.allScoresToString());
			System.out.println(resultsSTP.allScoresToString());
			System.out.println(resultsIGW.allScoresToString());
			System.out.println(resultsIGP.allScoresToString());

			System.out.println(resultsWLadd.allScoresToString());
			System.out.println(resultsSTFadd.allScoresToString());
			System.out.println(resultsSTPadd.allScoresToString());
			System.out.println(resultsIGWadd.allScoresToString());
			System.out.println(resultsIGPadd.allScoresToString());
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


