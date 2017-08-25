/**
 * 
 */
package acsse.csc2b.p04.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import acsse.csc2b.p04.FitnessData;
import acsse.csc2b.p04.server.gui.ServerFrame;

/**
 * @author Joel, DM, 201071264
 *
 */
public class PositionServer {
	//constant variables
	private static final String fileLoc = "./data/data.txt";
	private static final int port = 1989;
	
	//class variables
	private List<Student> data;
	private boolean isRunning = true;
	DatagramSocket serverSocket = null;
	int intRxCount;
	
	ServerFrame frame;
	
	/**
	 * Default constructor
	 */
	public PositionServer() {
		data = Collections.synchronizedList(new ArrayList<>());
		intRxCount = 0;
		isRunning = true;
		loadData();
		startServer();
	}
	
	/**
	 * @param frame the frame to set
	 */
	public void setFrame(ServerFrame frame) {
		this.frame = frame;
		if (data.size() > 0) frame.refreshPlot(data);	
	}

	/**
	 * loads the data from the data.txt file or creates a new one if it doesn't exist
	 */
	private void loadData() {
		Pattern pattern = Pattern.compile("\\d+\\s\\d+\\s\\d+");
		File file = new File(fileLoc);
		Scanner sc = null;
		if (file.exists()) { //file exists, fill up the dataset, otherwise data is empty
			try {
				//scans the file for data
				sc = new Scanner(file);
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (pattern.matcher(line).matches()) {
						StringTokenizer st = new StringTokenizer(line);
						String studentNumber = st.nextToken();
						int steps = Integer.parseInt(st.nextToken());
						int hr = Integer.parseInt(st.nextToken());
						//create a temporary data object and then add it to the dataset
						FitnessData tempData = new FitnessData(studentNumber, steps, hr);
						addData(tempData);
					}
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} finally {
				sc.close();
			}
		}	
	}
	
	/**
	 * add the data to the correct student
	 * and ensure each student keeps only last five data entries
	 * @param fd the FitnessData to add
	 */
	private void addData(FitnessData fd) {
		synchronized (data) {
			//loop through the data set to see if student exists and add if so
			Iterator<Student> iterator = data.iterator();
			boolean isFound = false;
			while (iterator.hasNext() && !isFound) {
				Student currentStudent = iterator.next();
				if (currentStudent.getStudentNumber().equals(fd.getStudentNumber())) { //if the student is already in the data
					if (currentStudent.getData().size() == 5) { //keep onbly five records
						currentStudent.getData().remove(0);
					}
					currentStudent.getData().add(fd);
					isFound = true;
				}
			}
			//if student is not found, then create new student and add to dataset
			if (!isFound) {
				Student s = new Student(fd.getStudentNumber());
				s.getData().add(fd);
				data.add(s);
			}
		}
	}
	
	/**
	 * saves the data to file
	 */
	private void saveData() {
		File file = new File(fileLoc);
		file.delete(); //deletes the file if it exists to make way for new file
		PrintWriter pw = null;
		try {
			file.createNewFile();
			pw = new PrintWriter(file);
			synchronized (data) {
				//iterate through all the data and prints to the file
				Iterator<Student> iterator = data.iterator();
				while (iterator.hasNext()) {
					Student s = iterator.next();
					Iterator<FitnessData> fIterator = s.getData().iterator();
					while (fIterator.hasNext()) {
						FitnessData fd = fIterator.next();
						pw.println(fd.getStudentNumber() + " " + fd.getStepCount() + " " + fd.getHeartRate());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
	
	/**
	 * starts the server in it's own thread so that it can listen for new data
	 */
	public void startServer() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					serverSocket = new DatagramSocket(port);
					byte[] socketData = new byte[1024];
					DatagramPacket socketPacket = new DatagramPacket(socketData, socketData.length);
					while (isRunning) {
						serverSocket.receive(socketPacket);
						intRxCount++;
						
						/*
						 * Reads the byte array into a bytestream 
						 * Then read the deserialised bytes into the FitnessData object
						 */
						ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(socketPacket.getData()));
						FitnessData temp = (FitnessData) in.readObject();
						addData(temp);
						in.close();
						
						//check if 10 messages have been received and if so refresh the plot line
						if (intRxCount == 10) {
							frame.refreshPlot(data);
							intRxCount = 0;
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (serverSocket != null) serverSocket.close();
				}
				
			}
		});
		t.start();
	}
	
	/**
	 * Shuts down the server thread and saves the data to file
	 */
	public void stopServer() {
		isRunning = false;
		serverSocket.close();
		saveData();
	}

	/**
	 * @return the data
	 */
	public List<Student> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<Student> data) {
		this.data = data;
	}
}
