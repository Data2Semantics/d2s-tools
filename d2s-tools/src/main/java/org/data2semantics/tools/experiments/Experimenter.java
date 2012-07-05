package org.data2semantics.tools.experiments;

import java.util.LinkedList;
import java.util.Queue;

public class Experimenter implements Runnable {
	private Queue<Runnable> experiments;
	private Thread[] activeExp;
	private boolean stop;

	public Experimenter(int numberOfThreads) {
		super();
		this.stop = false;
		experiments = new LinkedList<Runnable>();
		activeExp = new Thread[numberOfThreads];
	}

	public void addExperiment(Runnable exp) {
		experiments.add(exp);
	}

	public void run() {
		while (!experiments.isEmpty() || !stop || !finished()) {
			for (int i = 0; i < activeExp.length; i++) {
				if ((activeExp[i] == null || !activeExp[i].isAlive()) && !experiments.isEmpty()) {
					activeExp[i] = new Thread(experiments.poll());
					activeExp[i].setDaemon(true);
					activeExp[i].start();
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasSpace() {
		while (true) {
			for (int i = 0; i < activeExp.length; i++) {
				if (activeExp[i] == null || !activeExp[i].isAlive()) {
					return true;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		this.stop = true;
	}

	private boolean finished() {
		for (int i = 0; i < activeExp.length; i++) {
			if (activeExp[i] != null && activeExp[i].isAlive()) {
				return false;
			}
		}
		return true;
	}

}
