package org.data2semantics.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.experiments.DataSetFactory;
import org.data2semantics.tools.experiments.LinkPredictionDataSetParameters;
import org.data2semantics.tools.experiments.PropertyPredictionDataSetParameters;
import org.data2semantics.tools.experiments.ExperimentResults;
import org.data2semantics.tools.experiments.Experimenter;
import org.data2semantics.tools.experiments.LinkPredictionDataSet;
import org.data2semantics.tools.experiments.LinkPredictionExperiment;
import org.data2semantics.tools.experiments.ResultsTable;
import org.data2semantics.tools.kernels.IntersectionGraphPathKernel;
import org.data2semantics.tools.kernels.IntersectionGraphWalkKernel;
import org.data2semantics.tools.kernels.IntersectionPartialSubTreeKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.kernels.WLSubTreeKernel;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class AffiliationLinkPredictionExperiment {
	private final static String DATA_DIR = "D:\\workspaces\\datasets\\aifb\\";
	private final static int NUMBER_OF_PROC = 6;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RDFDataSet testSetA = new RDFFileDataSet(DATA_DIR + "aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		RDFDataSet testSetB = new RDFFileDataSet(DATA_DIR + "aifb-fixed_no_schema.n3", RDFFormat.N3);

		List<LinkPredictionDataSetParameters> dataSetsParams = new ArrayList<LinkPredictionDataSetParameters>();
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.01, 0.1, 1, 10, 100};	
		
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));

		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));

		/*
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));

		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new LinkPredictionDataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#Person", "http://swrc.ontoware.org/ontology#ResearchGroup", "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		*/
		
		LinkPredictionDataSet dataset;
		LinkPredictionExperiment exp;

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
			for (LinkPredictionDataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createLinkPredictonDataSet(params);
				//dataset.removeSmallClasses(5);
				
				resultsWL.newRow(dataset.getLabel() + " WLSubTreeKernel");
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + "_" + "WL" + fileId + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new WLSubTreeKernel(i), new WLSubTreeKernel(i), 0.5, 0.5, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWL.addResult(exp.getResults().getAccuracy());
						resultsWL.addResult(exp.getResults().getF1());
						resultsWL.addResult(exp.getResults().getrPrecision());
						resultsWL.addResult(exp.getResults().getAveragePrecision());
					}
				}
				
				
				resultsSTF.newRow(dataset.getLabel() + " IntersectionFullSubTreeKernel");
				for (int i = 0; i < 3; i++) {
					
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + "_" + "IntersectionFullSubTree" + fileId + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionSubTreeKernel(i,1), new IntersectionSubTreeKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTF.addResult(exp.getResults().getAccuracy());
						resultsSTF.addResult(exp.getResults().getF1());
						resultsSTF.addResult(exp.getResults().getrPrecision());
						resultsSTF.addResult(exp.getResults().getAveragePrecision());
					}
				}
				
				resultsSTP.newRow(dataset.getLabel() + " IntersectionPartialSubTreeKernel");
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + "_" + "IntersectionPartialSubTree" + fileId + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionPartialSubTreeKernel(i,0.01), new IntersectionPartialSubTreeKernel(i,0.01), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsSTP.addResult(exp.getResults().getAccuracy());
						resultsSTP.addResult(exp.getResults().getF1());
						resultsSTP.addResult(exp.getResults().getrPrecision());
						resultsSTP.addResult(exp.getResults().getAveragePrecision());
					}
				}
				
				resultsIGW.newRow(dataset.getLabel() + " IntersectionGraphWalkKernel");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + "_" + "IntersectionGraphWalk" + fileId + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionGraphWalkKernel(i,1), new IntersectionGraphWalkKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsIGW.addResult(exp.getResults().getAccuracy());
						resultsIGW.addResult(exp.getResults().getF1());
						resultsIGW.addResult(exp.getResults().getrPrecision());
						resultsIGW.addResult(exp.getResults().getAveragePrecision());
					}
				}				
				
				resultsIGP.newRow(dataset.getLabel() + " IntersectionGraphPathKernel");
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + "_" + "IntersectionGraphPath" + fileId + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionGraphPathKernel(i,1), new IntersectionGraphPathKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsIGP.addResult(exp.getResults().getAccuracy());
						resultsIGP.addResult(exp.getResults().getF1());
						resultsIGP.addResult(exp.getResults().getrPrecision());
						resultsIGP.addResult(exp.getResults().getAveragePrecision());
					}
				}
				
			
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
		System.out.println(resultsSTF);
		System.out.println(resultsSTP);
		System.out.println(resultsIGW);
		System.out.println(resultsIGP);
	}

}
