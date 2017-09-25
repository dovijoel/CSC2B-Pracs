/**
 * 
 */
package acsse.csc2b.p05;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Joel, DM, 201071264
 * A file item with the relevant details needed for transfer from the seeder to the leecher
 */
public class FileItem {
	private static AtomicInteger nextID = new AtomicInteger(0);
	
	private SimpleStringProperty filePath;
	private SimpleStringProperty fileName;
	private byte[] hash;
	private SimpleStringProperty hashString;
	private SimpleLongProperty size;
	private SimpleIntegerProperty _id;
	private boolean toDownload;
	
	/**
	 * Constructor for file item when instantiating from a File object
	 * For the seeder
	 * @param file the file to be added to the list of file items
	 */
	public FileItem(File file) {
		_id = new SimpleIntegerProperty(nextID.getAndIncrement());
		//initialise values of the fileItem for the given File
		fileName = new SimpleStringProperty(file.getName());
		filePath = new SimpleStringProperty(file.getAbsolutePath());
		size = new SimpleLongProperty(file.length());
		//error checking with hash not yet implemented, but not necessary for marks
		//hash = Util.generateHash(file);
		/*StringBuffer sb = new StringBuffer();
		for (byte bytes : hash) {
			sb.append(String.format("%02x", bytes & 0xff)); 
			
			 * % format
			 * 0 0 padding
			 * 2 decimals
			 * x hexadecimal representation
			 
		}

		hashString = new SimpleStringProperty(sb.toString()); //convert byte array to hex string representation
*/	}
	
	/**
	 * Constructor for when file details are received from seeder
	 * @param _id the id of the file
	 * @param fileName the file name
	 * @param fileHashString the hash string of the file
	 * @param size the file's size
	 */
	public FileItem(String _id, String fileName, String fileHashString, String size) {
		this._id = new SimpleIntegerProperty(Integer.parseInt(_id));
		//initialise values of the fileItem for the given File
		this.fileName = new SimpleStringProperty(fileName);
		this.filePath = new SimpleStringProperty("");
		this.size = new SimpleLongProperty(Long.parseLong(size));
		this.hash = null;
		hashString = new SimpleStringProperty(fileHashString); //convert byte array to hex string representation
		setToDownload(false);
	}
	
	/**
	 * Calculate number of chunks to be transferred of the fike
	 * @return
	 */
	public int getNumberOfChunks() {
		Double num = Math.ceil(size.get() / 1024); //size of the byte arrays is 1024
		int chunks;
		/*if ((num % 1) == 0) { //check if it is a while number. Unlikely but possible
			chunks = num.intValue();
		} else {
			chunks = num.intValue() + 1;
		}*/
		chunks = num.intValue() + 1;
		return chunks;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath.get();
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath.set(filePath);
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName.get();
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}

	/**
	 * @return the hash
	 */
	public byte[] getHash() {
		return hash;
	}

	/**
	 * @param hash the hash to set
	 */
	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size.get();
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size.set(size);
	}

	/**
	 * @return the hashString
	 */
	public String getHashString() {
		return "";//hashString.get();
	}

	/**
	 * @param hashString the hashString to set
	 */
	public void setHashString(String hashString) {
		this.hashString.set(hashString);
	}

	/**
	 * @return the _id
	 */
	public Integer get_id() {
		return _id.get();
	}

	/**
	 * @return the toDownload
	 */
	public boolean getToDownload() {
		return toDownload;
	}

	/**
	 * @param toDownload the toDownload to set
	 */
	public void setToDownload(boolean toDownload) {
		this.toDownload = toDownload;
	}
}
