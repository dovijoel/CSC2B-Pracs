/**
 * 
 */
package acsse.csc2b.p04.server.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.math.plot.*;

import acsse.csc2b.p04.FitnessData;
import acsse.csc2b.p04.server.PositionServer;
import acsse.csc2b.p04.server.Student;

/**
 * @author Joel, DM, 201071264
 * Custom JFrame for the Server GUI
 */
public class ServerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	//class variables
	Plot2DPanel plot;
	PositionServer server;
	
	/**
	 * Constructor with title parameter
	 * @param server the server that's listening
	 */
	public ServerFrame(PositionServer server) {
		this.server = server;
		this.setTitle("Practical 04 - UDP Server");
		
		//decalre and initialise the JMathPlot panel
		plot = new Plot2DPanel("SOUTH");
		plot.setBackground(Color.WHITE);
		plot.setAxisLabels("Step Count", "Heart Rate");
		
		setLayout(new BorderLayout());
		
		//buttons
		JButton btnRefresh = new JButton("Refresh");
		JButton btnClose = new JButton("Close");
		
		//button events
		btnRefresh.addActionListener(e -> {
			refreshPlot(server.getData());
		});
		
		btnClose.addActionListener(e -> {
			server.stopServer();
			System.exit(0);
		});
		
		//panel for buttons and add to the frame
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new FlowLayout());
		pnlButtons.add(btnRefresh);
		pnlButtons.add(btnClose);
		add(plot);
		add(pnlButtons, BorderLayout.SOUTH);
		pack();
	}
	
	
	/**
	 * Refresh the plotPanel
	 * @param data the fitness data to plot
	 */
	public void refreshPlot(List<Student> data) {
		synchronized (data) {
			//first remove all plots
			plot.removeAllPlots();
			//loop through all students and add the plot lines
			for (Student s : data) {
				//initialise x and y
				double x[] = new double[s.getData().size()];
				double y[] = new double[s.getData().size()];
				ArrayList<FitnessData> sorted = s.getData();
				Collections.sort(sorted);
				//loop through all student's data
				for (int i = 0; i < sorted.size(); i++) {
					x[i] = sorted.get(i).getStepCount();
					y[i] = sorted.get(i).getHeartRate();
				}
				//add the line to the plot, and assign a random color
				plot.addLinePlot(s.getStudentNumber(), s.getColor(), x, y);
			}
		}
		
	}
}
