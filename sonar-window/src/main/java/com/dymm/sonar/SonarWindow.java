package com.dymm.sonar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class SonarWindow {
	
	private List<Measure> measures;
	private MeasureReader measureReader;
	private MeasureProducerFactory factory;
	
	private Timer timer;
	private JFrame frame;
	private JComboBox<String> comboBox;
	private JButton btnStartButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SonarWindow window = new SonarWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void initializeData() {
		measureReader = null;
		measures = new ArrayList<Measure>();
		
		factory = new MeasureProducerFactory();
		List<MeasureReader> sources = factory.getSources();
		for(MeasureReader source : sources) {
			comboBox.addItem(source.getDescription());
		}
	}
	
	private void initializeRefreshTimer() {
		this.timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.repaint();
			}
		});
		this.timer.setRepeats(true);
		this.timer.start();
	}

	/**
	 * Create the application.
	 */
	public SonarWindow() {
		initialize();
		initializeData();
		initializeRefreshTimer();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		measures = new CopyOnWriteArrayList<Measure>();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 712, 536);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		MyCanvas panel = new MyCanvas(measures);
		panel.setBounds(0, 0, 488, 497);
		frame.getContentPane().add(panel);
		
		comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(measureReader != null) {
					measureReader.selectPort(comboBox.getSelectedItem().toString());
				}
			}
		});
		comboBox.setBounds(519, 23, 177, 20);
		frame.getContentPane().add(comboBox);
		
		btnStartButton = new JButton("Start");
		btnStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( measureReader != null ) {
					measureReader.start();
				}
			}
		});
		btnStartButton.setBounds(529, 54, 157, 23);
		frame.getContentPane().add(btnStartButton);
		
		JButton btnStopButton = new JButton("Stop");
		btnStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( measureReader != null ) {
					measureReader.stop();
					SerialMeasureReader newMeasureReader = new SerialMeasureReader(measures);
					newMeasureReader.selectPort(comboBox.getSelectedItem().toString());
					measureReader = newMeasureReader;
				}
			}
		});
		btnStopButton.setBounds(529, 88, 157, 23);
		frame.getContentPane().add(btnStopButton);
	}
}
