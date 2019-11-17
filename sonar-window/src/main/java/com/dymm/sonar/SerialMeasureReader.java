package com.dymm.sonar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import com.fazecast.jSerialComm.*;

//http://fazecast.github.io/jSerialComm/
public class SerialMeasureReader extends Thread  implements MeasureReader {
	List<Measure> measures;
	private volatile boolean threadSuspended;
	private SerialPort selectedPort;
	private boolean portOpened;
	
	public SerialMeasureReader(List<Measure> measures) {
		this.measures = measures;
		threadSuspended = false;
		selectedPort = null;
		portOpened = false;
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


	public List<Measure> getMeasures() {
		return measures;
	}


	public String[] getSources() {
		 SerialPort[] ports = SerialPort.getCommPorts();
		 String[] names = new String[ports.length];
		 
		 int i=0;
		 for(SerialPort port : ports) {
			 names[i++] = port.getDescriptivePortName();
		 }
		 return names;
	 }
	 
	 public void selectPort(String portDescriptor) {
		 SerialPort[] ports = SerialPort.getCommPorts();
		 for(SerialPort port : ports) {
			 if(port.getDescriptivePortName().equals(portDescriptor)) {
				 selectedPort = port;
				 break;
			 }
		 }
	 }
	 
	 public SerialPort getSelectedPort() {
		 return selectedPort;
	 }
	
	private boolean openPort() {
		if(selectedPort != null && portOpened == false) {
			if (selectedPort.openPort()) {
				portOpened = true;
				selectedPort.setBaudRate(115200);
				selectedPort.setNumDataBits(8);
				selectedPort.setNumStopBits(1);
				selectedPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
			}
		}
		return portOpened;
	}
	private void closePort() {
		if(selectedPort != null && portOpened==true) {
			selectedPort.closePort();
			portOpened = false;
		}
	}
	public void startMeasure() {
		if(selectedPort != null && portOpened == true) {
			int wrote = selectedPort.writeBytes(new byte[] {'3', '1'}, 2);
			System.out.printf("Starting measure (%d)\n", wrote);
		}
	}
	public void stopMeasure() {
		if(selectedPort != null) {
			int wrote = selectedPort.writeBytes(new byte[] {'3', '0'}, 2);
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
		
		while(threadSuspended == false && selectedPort != null && portOpened) {
			//Thread.sleep(1000);
			  byte[] temporaryBuffer = new byte[1024];
		      int numRead = selectedPort.readBytes(temporaryBuffer, temporaryBuffer.length);
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
				measures.clear();
			}
			String[] values = str.split(";");
			if(values.length >= 2) {
				measures.add( new Measure( (Double.parseDouble(values[0])*360.0)/2048.0, Double.parseDouble(values[1]) /10.0) );
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
