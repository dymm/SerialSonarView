package com.dymm.sonar;
import java.util.List;
import java.util.Random;

public class FakeMeasureReader extends Thread {
	List<Measure> measures;
	
	public FakeMeasureReader(List<Measure> measures) {
		this.measures = measures;
	}
	
	public void run() {
		int i = 0;
		Random randomGenerator = new Random();
		
		while(true) {
			
			if(measures.size()>26) {
				measures.clear();
				i=0;
			}
			measures.add(new Measure(10*i++, randomGenerator.nextInt(120)));
			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
