package org.data2semantics.exp.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEAR;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;

@Module(name="Experiment")
public class ExperimentModule {
	private SparseVector[] fv;
	private List<Double> target;
	private double[] targetA;
	private LibLINEARParameters parms;
	private long seed;
	private int folds;
	private Prediction[] pred;
	private int depth;
	
	public ExperimentModule(
			@In(name="featureVectors") SparseVector[] fv, 
			@In(name="target") List<Double> target,
			@In(name="parms") LibLINEARParameters parms, 
			@In(name="folds") int folds,
			@In(name="seed") int seed,
			@In(name="depth") int depth) {
		
		this.depth = depth;
		this.fv = fv;
		this.target = target;
		this.parms = parms;
		this.seed = seed;
		this.folds = folds;
	}
	
	
	public ExperimentModule(
			@In(name="featureVectors") SparseVector[] fv, 
			@In(name="target") List<Double> target,
			@In(name="parms") LibLINEARParameters parms, 
			@In(name="folds") int folds,
			@In(name="seed") int seed) {
		
		this.fv = fv;
		this.target = target;
		this.parms = parms;
		this.seed = seed;
		this.folds = folds;
	}
	
	@Main
	public List<Double> runExperiment() {
		
		List<SparseVector> fvL = Arrays.asList(fv);
		Collections.shuffle(fvL, new Random(seed));
		fv = fvL.toArray(new SparseVector[1]);
		
		List<Double> targetL = new ArrayList<Double>();
		targetL.addAll(target);
		Collections.shuffle(targetL, new Random(seed));
		
		targetA = new double[targetL.size()];
		for (int i = 0; i < targetA.length; i++) {
			targetA[i] = targetL.get(i);
		}

		pred = LibLINEAR.crossValidate(fv, targetA, parms, folds);
		
		List<Double> res = new ArrayList<Double>();
		
		for (Prediction p : pred) {
			res.add(p.getLabel());
		}
		return res;
	}
	
	
	@Out(name="accuracy")
	public double getAccuracy() {
		return new Accuracy().computeScore(targetA, pred);
	}
	
	@Out(name="f1")
	public double getF1() {
		return new F1().computeScore(targetA, pred);
	}
	
	

}
