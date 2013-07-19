package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.OutputField;
import org.data2semantics.proppred.learners.evaluation.EvaluationUtils;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.openrdf.model.Value;

/**
 * Wrapper for lib linear module, suppose to accept labels, and generate the necessary stuffs for LinearKernel Experiment.
 * 
 * @author wibisono
 *
 */
@Module(name="LibLinearWrapper")
public class LibLinearWrapperModule {

		@OutputField(name="wLabels")
		public int[] wLabels ;
	
		@OutputField(name="weights")
		public double[] weights;
	
		@OutputField(name="params")
		public LibLINEARParameters linParms;
		
		@OutputField(name="target")
		public List<Double> target;
		
		@MainMethod
		public LibLINEARParameters getLibLInearParam(
				@InputParameter(name="labels") ArrayList<Value> labels, 
				@InputParameter(name="cs") ArrayList<Double> csArray){
			
			double[] cs = new double[csArray.size()];
			for(int i=0;i<cs.length;i++)cs[i] = csArray.get(i);
			target = EvaluationUtils.createTarget(labels);

			linParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
			linParms.setDoCrossValidation(true);
			linParms.setNumFolds(5);

			Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
			wLabels = new int[counts.size()];
			weights = new double[counts.size()];

			for (double label : counts.keySet()) {
				wLabels[(int) label - 1] = (int) label;
				weights[(int) label - 1] = 1 / counts.get(label);
			}
			linParms.setWeightLabels(wLabels);
			linParms.setWeights(weights);
			
			return linParms;
		}
}
