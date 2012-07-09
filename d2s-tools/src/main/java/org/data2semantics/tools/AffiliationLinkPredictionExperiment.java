package org.data2semantics.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.experiments.DataSetFactory;
import org.data2semantics.tools.experiments.DataSetParameters;
import org.data2semantics.tools.experiments.ExperimentResults;
import org.data2semantics.tools.experiments.Experimenter;
import org.data2semantics.tools.experiments.LinkPredictionDataSet;
import org.data2semantics.tools.experiments.LinkPredictionExperiment;
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

		List<DataSetParameters> dataSetsParams = new ArrayList<DataSetParameters>();
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {0.01, 0.1, 1, 10, 100};	
		
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));

		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetA, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));

		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, false, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, false, true));

		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, false));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 1, true, true));
		dataSetsParams.add(new DataSetParameters(testSetB, "http://swrc.ontoware.org/ontology#affiliation", bl, 2, true, true));
		
		LinkPredictionDataSet dataset;
		LinkPredictionExperiment exp;

		List<ExperimentResults> resultsWL = new ArrayList<ExperimentResults>();
		List<ExperimentResults> resultsSTF = new ArrayList<ExperimentResults>();
		List<ExperimentResults> resultsSTP = new ArrayList<ExperimentResults>();
		List<ExperimentResults> resultsIGW = new ArrayList<ExperimentResults>();
		List<ExperimentResults> resultsIGP = new ArrayList<ExperimentResults>();
		
		Experimenter experimenter = new Experimenter(NUMBER_OF_PROC);
		Thread expT = new Thread(experimenter);
		expT.setDaemon(true);
		expT.start();
		


		try {
			for (DataSetParameters params : dataSetsParams) {
				dataset = DataSetFactory.createLinkPredictonDataSet(params);
				//dataset.removeSmallClasses(5);
				
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "WL" + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new WLSubTreeKernel(i), new WLSubTreeKernel(i), 0.5, 0.5, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsWL.add(exp.getResults());
					}
				}
				
				
				for (int i = 0; i < 3; i++) {
					
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionFullSubTree" + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionSubTreeKernel(i,1), new IntersectionSubTreeKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));
						experimenter.addExperiment(exp);
						resultsSTF.add(exp.getResults());
					}
				}
				
				
				for (int i = 0; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionPartialSubTree" + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionPartialSubTreeKernel(i,0.01), new IntersectionPartialSubTreeKernel(i,0.01), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsSTP.add(exp.getResults());
					}
				}
				
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphWalk" + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionGraphWalkKernel(i,1), new IntersectionGraphWalkKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsIGW.add(exp.getResults());
					}
				}				
				
				for (int i = 1; i < 3; i++) {
					if (experimenter.hasSpace()) {		
						int fileId = (int) (Math.random() * 100000000);	
						File file = new File(DATA_DIR + fileId + "_" + "IntersectionGraphPath" + "_" + i + ".txt");
						exp = new LinkPredictionExperiment(new LinkPredictionDataSet(dataset), new IntersectionGraphPathKernel(i,1), new IntersectionGraphPathKernel(i,1), 0.5, 0.5, seeds, cs, new FileOutputStream(file));	
						experimenter.addExperiment(exp);
						resultsIGP.add(exp.getResults());
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

		for (ExperimentResults res : resultsWL) {
			System.out.println(res.getLabel() + " acc=" + res.getAccuracy() + " f1=" + res.getF1());
		}
		
		for (ExperimentResults res : resultsSTF) {
			System.out.println(res.getLabel() + " acc=" + res.getAccuracy() + " f1=" + res.getF1());
		}

		for (ExperimentResults res : resultsSTP) {
			System.out.println(res.getLabel() + " acc=" + res.getAccuracy() + " f1=" + res.getF1());
		}

		for (ExperimentResults res : resultsIGW) {
			System.out.println(res.getLabel() + " acc=" + res.getAccuracy() + " f1=" + res.getF1());
		}

		for (ExperimentResults res : resultsIGP) {
			System.out.println(res.getLabel() + " acc=" + res.getAccuracy() + " f1=" + res.getF1());
		}
	}

}
