package com.dymm.sonar;


public interface MeasureReader extends Runnable {
	String getDescription();
	
	public interface OnMeasureReadListener {
		void onMeasure(Measure measure);
	}
	void registerOnMeasureRead(OnMeasureReadListener listener);
	void start();
	void stop();
	
}
