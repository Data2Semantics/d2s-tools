package org.data2semantics.exp.modules;

import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;

@Module(name="LibLINEARParms")
public class LibLINEARParmsModule {
	private boolean cv;
	private List<Double> cs;
	private List<Double> target;
	private int nrFolds;
	private double splitFraction;
	private LibLINEARParameters parms;


	public LibLINEARParmsModule(
			@In(name="cs") List<Double> cs, 
			@In(name="nrFolds") int folds, 
			@In(name="target") List<Double> target
			) {
		cv = true;
		this.cs = cs;
		this.target = target;
		this.nrFolds = folds;
	}
	
	public LibLINEARParmsModule(
			@In(name="cs") List<Double> cs, 
			@In(name="splitFraction") double fraction, 
			@In(name="target") List<Double> target
			) {
		cv = false;
		this.cs = cs;
		this.target = target;
		this.splitFraction = fraction;
	}

	@Main
	public LibLINEARParameters createParms() {
		double[] csA = new double[cs.size()];
		for (int i=0;i<csA.length;i++) {
			csA[i] = cs.get(i);
		}

		parms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, csA);
		parms.setDoCrossValidation(cv);
		if (cv) {
			parms.setNumFolds(nrFolds);
		} else {
			parms.setSplitFraction((float) splitFraction);
		}
		
		//parms.setDoWeightLabels(true);
		
		parms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		parms.setWeights(EvaluationUtils.computeWeights(target));

		return parms;
	}

	@Out(name="parameters")
	public LibLINEARParameters getParms() {
		return parms;
	}

}
