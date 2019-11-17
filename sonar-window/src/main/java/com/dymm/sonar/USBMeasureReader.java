package com.dymm.sonar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.dymm.sonar.MeasureReader.OnMeasureReadListener;
import com.fazecast.jSerialComm.*;

public class USBMeasureReader extends Thread implements MeasureReader {
	
	private volatile boolean threadSuspended;
	private boolean portOpened;
	SerialPort port;
	OnMeasureReadListener listener;
	
	public USBMeasureReader(SerialPort p) {
		this.port = p;
	}

	public String getDescription() {
		return port.getDescriptivePortName();
	}

	public void registerOnMeasureRead(OnMeasureReadListener listener) {
		this.listener = listener;
	}
	
	public void start() {
		setThreadSuspended(false);
	}
	public void stop() {
		setThreadSuspended(true);
	}
	
	 public boolean isThreadSuspended() {
		return threadSuspended;
	}

	public void setThreadSuspended(boolean threadSuspended) {
		this.threadSuspended = threadSuspended;
		if (threadSuspended) {
			stopMeasure();
			closePort();
		} else {
			openPort();
			startMeasure();
		}
	}
	
	private boolean openPort() {
		if(port != null && portOpened == false) {
			if (port.openPort()) {
				port = true;
				port.setBaudRate(115200);
				port.setNumDataBits(8);
				port.setNumStopBits(1);
				port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
			}
		}
		return portOpened;
	}
	private void closePort() {
		if(port != null && portOpened==true) {
			port.closePort();
			portOpened = false;
		}
	}
	private void startMeasure() {
		if(port != null && portOpened == true) {
			int wrote = port.writeBytes(new byte[] {'3', '1'}, 2);
			System.out.printf("Starting measure (%d)\n", wrote);
		}
	}
	private void stopMeasure() {
		if(port != null) {
			int wrote = port.writeBytes(new byte[] {'3', '0'}, 2);
			System.out.printf("Stoping measure (%d)\n", wrote);
		}
	}

	public void run() {
		threadSuspended = false;
		boolean portOpened = openPort();
		if (portOpened == false) {
			System.out.println("Port not opened");
			return;
		} else {
			System.out.println("Port opened");
		}
		
		try {
			startMeasure();
			readCommInLoop();
		}
		catch(Exception ex) {
			System.out.println("Error : " + ex.toString());
		}
		
		closePort();
		threadSuspended = true;
	}
	
	private void readCommInLoop() throws IOException, InterruptedException {
		byte[] partWithoutEndOfLine = null;
		
		while(threadSuspended == false && port != null && portOpened) {
			//Thread.sleep(1000);
			  byte[] temporaryBuffer = new byte[1024];
		      int numRead = port.readBytes(temporaryBuffer, temporaryBuffer.length);
		      if(numRead<=0) continue;
		      temporaryBuffer = Arrays.copyOfRange(temporaryBuffer, 0, numRead);
		      byte[] fullBuffer = concatBuffers(partWithoutEndOfLine, temporaryBuffer);
		      partWithoutEndOfLine = parseBuffer(fullBuffer);
		}
	}
	
	private byte[] parseBuffer(byte[] buffer) {
		int from = 0;
		int to = 0;
		for(;to<buffer.length;to++) {
			if(buffer[to] == 0x0a) {
				byte[] partWithEndofLine = Arrays.copyOfRange(buffer, from, to);
				from = to+1;
				lineFound(partWithEndofLine);
			}
		}
		if(from<to) {
			return Arrays.copyOfRange(buffer, from, to);
		}
		return null;
	}
	
	private byte[] concatBuffers(byte[] partWithoutEndOfLine, byte[] partWithEndofLine) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		if(partWithoutEndOfLine != null) outputStream.write( partWithoutEndOfLine );
		if(partWithEndofLine != null) outputStream.write( partWithEndofLine );
		return outputStream.toByteArray( );
	}
	
	private void lineFound(byte[] line) {
		if(line==null || line.length==0) return;
		try {
			String str = new String(line, StandardCharsets.ISO_8859_1);
			//System.out.println("Read : '" + str + "'");
			if("EOS".equals(str)) {
				//End of sequence
			}
			String[] values = str.split(";");
			if(values.length >= 2 && listener!=null) {
				Measure measure = new Measure( (Double.parseDouble(values[0])*360.0)/2048.0, Double.parseDouble(values[1]) /10.0);
				listener.onMeasure(measure);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
