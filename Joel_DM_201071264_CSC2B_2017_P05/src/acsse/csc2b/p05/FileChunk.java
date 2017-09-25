/**
 * 
 */
package acsse.csc2b.p05;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Arrays;


import acsse.csc2b.p05.util.Util;

/**
 * File chunk for sending and receiving from seeder to leecher
 * @author Joel, DM, 201071264
 *
 */
public class FileChunk implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int _id;
	int fileId;
	byte[] chunk;
	byte[] hash;
	
	public static int CHUNKSIZE = 1024;
	
	/**
	 * Constructor for a file chunk
	 * @param _id the chunk number
	 * @param fileId the id of the file
	 * @param filePath the path of file
	 */
	public FileChunk(int _id, int fileId, String filePath) {
		this._id = _id;
		this.fileId = fileId;
		
		readChunk(filePath);
		
		//hash will be used to ensue no error in transfer, but not yet implented
		hash = Util.generateChunkHash(this.chunk);
		//System.out.format("chunk hash no %d, file %d is %s", _id, fileId, new HexBinaryAdapter().marshal(hash));
	}
	
	/**
	 * Read the chunk from the file and store it in a byte array
	 * @param filePath path of the file
	 */
	private void readChunk(String filePath) {
		//get chunk byte array
		RandomAccessFile raf = null;
		try {
			/*
			 * by checking returned size, and copying it into the chunk, 
			 * this ensures there's no extra padding when the end of the file is reached
			 */
			raf = new RandomAccessFile(filePath, "r");
			byte[] temp = new byte[1024];
			int off = _id * 1024;
			raf.seek(off);
			int size = raf.read(temp, 0, temp.length);
			if (size != 1024) System.out.println("array size is " + size);
			chunk = Arrays.copyOf(temp, size);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @return the _id
	 */
	public int get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(int _id) {
		this._id = _id;
	}

	/**
	 * @return the chunk
	 */
	public byte[] getChunk() {
		return chunk;
	}

	/**
	 * @param chunk the chunk to set
	 */
	public void setChunk(byte[] chunk) {
		this.chunk = chunk;
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
}
