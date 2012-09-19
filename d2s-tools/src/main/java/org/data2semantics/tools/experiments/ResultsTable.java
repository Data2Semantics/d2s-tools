package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.List;

public class ResultsTable {
	
	private List<List<Result>> table;
	private List<String> rowLabels;
	
	public ResultsTable() {
		table = new ArrayList<List<Result>>();
		rowLabels = new ArrayList<String>();
	}
	
	public void newRow(String rowLabel) {
		rowLabels.add(rowLabel);
		table.add(new ArrayList<Result>());
	}
	
	public void addResult(Result result) {
		table.get(table.size()-1).add(result);
	}
	
	public String formatScore(double score) {
		return Double.toString(((double) Math.round(score * 100)) / 100.0);
	}
	
	public String toString() {
		StringBuffer tableStr = new StringBuffer();		
		List<Result> row1 = table.get(0);
		
		for(Result res : row1) {
			tableStr.append(res.getLabel());
			tableStr.append(" \t ");
		}
		tableStr.append("\n");
		
		for (int i = 0; i < table.size(); i++) {
			for (Result res : table.get(i)) {
				tableStr.append(formatScore(res.getScore()));
				tableStr.append(" \t ");
			}
			tableStr.append(rowLabels.get(i));
			tableStr.append("\n");
		}
		return tableStr.toString();		
	}
	
}
