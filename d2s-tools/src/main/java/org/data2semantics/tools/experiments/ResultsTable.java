package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.inference.TTest;

public class ResultsTable {

	private List<List<Result>> table;
	private List<String> rowLabels;
	private List<Result> compRes;


	public ResultsTable() {
		table = new ArrayList<List<Result>>();
		rowLabels = new ArrayList<String>();
		compRes = new ArrayList<Result>();
	}

	public void newRow(String rowLabel) {
		rowLabels.add(rowLabel);
		table.add(new ArrayList<Result>());
	}

	public void addResult(Result result) {
		table.get(table.size()-1).add(result);
	}

	public void addCompResult(Result res) {
		compRes.add(res);
	}

	public void addCompResults(List<Result> res) {
		compRes.addAll(res);
	}

	public String formatScore(double score) {
		return Double.toString(((double) Math.round(score * 100)) / 100.0);
	}

	public String toString() {
		StringBuffer tableStr = new StringBuffer();		

		if(table.size() > 0) {

			List<Result> row1 = table.get(0);
			TTest ttest = new TTest();
			String signif = "";

			for(Result res : row1) {
				tableStr.append(res.getLabel());
				tableStr.append(" \t ");
			}
			tableStr.append("\n");

			for (int i = 0; i < table.size(); i++) {
				for (Result res : table.get(i)) {

					signif = "";
					for (Result comp : compRes) {
						if (comp.getLabel().equals(res.getLabel())) {
							if (!ttest.tTest(comp.getScores(), res.getScores(), 0.05)) {
								signif = "+";
							}
						}
					}

					tableStr.append(formatScore(res.getScore()) + signif);
					tableStr.append(" \t ");
				}
				tableStr.append(rowLabels.get(i));
				tableStr.append("\n");
			}
		}
		return tableStr.toString();		
	}


	public String allScoresToString() {
		StringBuffer tableStr = new StringBuffer();
		if (table.size() > 0) {

			List<Result> row1 = table.get(0);

			for(Result res : row1) {
				tableStr.append(res.getLabel());
				tableStr.append(" \t ");
			}
			tableStr.append("\n");

			for (int i = 0; i < table.size(); i++) {
				for (Result res : table.get(i)) {
					tableStr.append(Arrays.toString(res.getScores()));
					tableStr.append(" \t ");
				}
				tableStr.append(rowLabels.get(i));
				tableStr.append("\n");
			}
		}
		return tableStr.toString();		
	}

	public List<Result> getBestResults() {
		return getBestResults(new ArrayList<Result>());
	}

	public List<Result> getBestResults(List<Result> bestResults) {

		for (int i = 0; i < table.size(); i++) {
			for (Result res : table.get(i)) {

				boolean newType = true;
				for (Result bestRes : bestResults) {
					if (res.getLabel().equals(bestRes.getLabel())) {
						newType = false;
						if (res.getScore() > bestRes.getScore()) {
							bestResults.remove(bestRes);
							bestResults.add(res);
							break;
						}
					}
				}
				if (newType) {
					bestResults.add(res);
				}
			}
		}
		return bestResults;
	}
}
