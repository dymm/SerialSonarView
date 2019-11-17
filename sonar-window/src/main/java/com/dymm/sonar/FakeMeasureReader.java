package com.dymm.sonar;

import java.util.Random;

public class FakeMeasureReader extends Thread implements MeasureReader {
	OnMeasureReadListener listener;
	
	public FakeMeasureReader() {
	}
	
	public String getDescription() {
		return "Fake measure reader";
	}
	
	public void run() {
		int i = 0;
		Random randomGenerator = new Random();
		
		while(true) {
			if(listener!=null) {
				listener.onMeasure(new Measure(10*i++, randomGenerator.nextInt(120)));
			}
			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void registerOnMeasureRead(OnMeasureReadListener listener) {
		this.listener = listener;
	}
}
