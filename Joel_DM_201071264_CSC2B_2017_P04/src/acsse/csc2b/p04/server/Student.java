/**
 * 
 */
package acsse.csc2b.p04.server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import acsse.csc2b.p04.FitnessData;

/**
 * @author Joel, DM, 201071264
 * Student object, to keep track of data per student
 */
public class Student {
	private String studentNumber;
	private ArrayList<FitnessData> data;
	private Color color;
	
	private static Random r = new Random ();
	 
	/**
	 * Constructor
	 * @param studentNumber the student number
	 */
	public Student(String studentNumber) {
		this.studentNumber = studentNumber;
		data = new ArrayList<>();
		color = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()); //each student has their own color for the plot
	}
	
	/**
	 * @return the studentNumber
	 */
	public String getStudentNumber() {
		return studentNumber;
	}
	
	/**
	 * @param studentNumber the studentNumber to set
	 */
	public void setStudentNumber(String studentNumber) {
		this.studentNumber = studentNumber;
	}
	
	/**
	 * @return the data
	 */
	public ArrayList<FitnessData> getData() {
		return data;
	}
	
	/**
	 * @param data the data to set
	 */
	public void setData(ArrayList<FitnessData> data) {
		this.data = data;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
}
