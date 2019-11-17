package com.dymm.sonar;

import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.*;

//http://fazecast.github.io/jSerialComm/

public class MeasureProducerFactory {
	
	List<MeasureReader> measureReaders;
	public MeasureProducerFactory() {
	}
	public void initialize() {
		List<MeasureReader> measureReaders = new ArrayList<MeasureReader>();
		 for(SerialPort port : ports) {
			 measureReaders.add(nBMeasureReader(port));
		 }
		 measureReaders.add(new FakeMeasureReader());
	}

	public List<MeasureReader> getSources() {
		 return measureReaders;
	}
	
}
