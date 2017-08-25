/**
 * 
 */
package acsse.csc2b.p04;

import java.io.Serializable;

/**
 * Fitness data object
 * needs to be serializable so that the object can be sent and received
 * needs to be comparable so that it can be sorted
 * @author Joel, DM, 201071264
 *
 */
public class FitnessData implements Serializable, Comparable<FitnessData> { 
	private static final long serialVersionUID = 1L;
	
	private String studentNumber;
	private int stepCount;
	private int heartRate;
	
	/**
	 * @param studentNumber the student number
	 * @param stepCount the step count
	 * @param heartRate the heart rate
	 */
	public FitnessData(String studentNumber, int stepCount, int heartRate) {
		super();
		this.studentNumber = studentNumber;
		this.stepCount = stepCount;
		this.heartRate = heartRate;
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
	 * @return the stepCount
	 */
	public int getStepCount() {
		return stepCount;
	}

	/**
	 * @param stepCount the stepCount to set
	 */
	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}

	/**
	 * @return the heartRate
	 */
	public int getHeartRate() {
		return heartRate;
	}

	/**
	 * @param heartRate the heartRate to set
	 */
	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	/**
	 * compares the different fitness data to ensure plotting makes sense
	 * @param fd data to compare
	 * @return sortedness
	 */
	@Override
	public int compareTo(FitnessData fd) {
		if (this.getStepCount() < fd.getStepCount()) return -1;
		if (this.getStepCount() == fd.getStepCount()) return 0;
		if (this.getStepCount() > fd.getStepCount()) return 1;
		return 0;
	}
	
	

}
