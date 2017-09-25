/**
 * 
 */
package acsse.csc2b.p05.leecher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import acsse.csc2b.p05.FileChunk;
import acsse.csc2b.p05.FileItem;
import acsse.csc2b.p05.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

/**
 * @author Joel, DM, 201071264
 * Leecher class that contains all logic for leeching files
 */
public class Leecher {
	ObservableList<FileItem> files;
	ObservableMap<Integer, FileItem> fileMap; //the map is for easy lookup
	InetAddress seederAddress;
	int seederPort;
	
	File downloadLocation = new File("");
	
	ExecutorService es = Executors.newFixedThreadPool(500);
	
	Map<Integer, Boolean> lockMap; //this map is for locking the file, not used currently
	
	Map<Integer, Double> progressMap; //to keep track of the download progress for each file
	
	VBox vbProgress;
	
	/**
	 * Constructor for the leecher class
	 * @param seederAddress the seeder InetAddress
	 * @param seederPort the seeder port
	 */
	public Leecher(InetAddress seederAddress, int seederPort) {
		fileMap = FXCollections.observableHashMap();
		files = FXCollections.observableArrayList(fileMap.values());
		
		/*
		 * Need a map keeping track of when files are in use, otherwise an exception is raised
		 * if a file is in use by another process
		 * Not used currently
		 */
		lockMap = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
		progressMap = Collections.synchronizedMap(new HashMap<Integer, Double>());
		
		/*
		 * Need to make a listener for the files observableArrayList in order for changes to reflect in tableView
		 * Made with help from https://stackoverflow.com/questions/33763848/observablemap-bound-to-tableview-not-updating-new-values
		 */
		fileMap.addListener((MapChangeListener.Change<? extends Integer, ? extends FileItem> c) -> {
            if (c.wasAdded()) {
                files.add(c.getValueAdded());
            } else if (c.wasRemoved()) {
                files.remove(c.getValueRemoved());
            }
        });
		this.seederAddress = seederAddress;
		this.seederPort = seederPort;
	}
	
	/**
	 * Iterates through the list of files and requests downloads of all of them
	 */
	public void downloadFiles() {
		for(FileItem f: fileMap.values()) {
			if (f.getToDownload()) {
				downloadFile(f);
			}
		}
	}
	
	/**
	 * Initiates the download of the specified file item
	 * @param f the fileItem to be downloaded
	 */
	private void downloadFile(FileItem f) {
		es.execute(new Runnable() { //run as a thread so doesn't lock up the GUI

			@Override
			public void run() {
				/*
				 * DOWNLOAD FILE_ID is format of the download command
				 * A string is expected as the result with the foromat of:
				 * FILEDETAILS FILE_ID NUM_CHUNKS
				 */
				String message = "DOWNLOAD " + f.get_id(); 
				String response = Util.sendMessageAndReceiveString(message, seederAddress, seederPort); 
				String[] responseAry = response.split(" ");
				final HBox hbox = new HBox(10);
				final ProgressBar progress = new ProgressBar(0);
				hbox.getChildren().addAll(new Label("Downloading " + f.getFileName() + ":"), progress);
				HBox.setHgrow(progress, Priority.ALWAYS);
				synchronized (lockMap) {
					lockMap.put(f.get_id(), false);  //create the lock map entity
				}
				synchronized (progressMap) {
					progressMap.put(f.get_id(), 0.0);
				}

				//TODO regex for file download details: FILEDETAILS FILE_ID NUM_CHUNKS
				if (responseAry[0].equals("FILEDETAILS") && (Integer.parseInt(responseAry[1]) == f.get_id()) ) {
					int intChunks = Integer.parseInt(response.split(" ")[2]);
					//create file and fill up file with blank data
					RandomAccessFile raf = null;
			        try {
			        	Platform.runLater(() -> vbProgress.getChildren().add(hbox));
			        	raf = new RandomAccessFile(downloadLocation.getAbsolutePath() + "\\" + f.getFileName(), "rw");
						raf.setLength(f.getSize());
						System.out.println("File created at: " + downloadLocation.getAbsolutePath() + "\\" + f.getFileName());
			        } catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (raf != null)
							try {
								raf.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
					}
					for (int i = 0; i < intChunks; i++) {
						downloadChunk(f.get_id(), i, progress, intChunks);
					}
				}
			}
		});
	}
	
	/**
	 * Downloads the specified chunk of the specified file
	 * @param fileID id of file of chunk
	 * @param chunkID id of chunk
	 * @param progress progress thus far of download
	 * @param numChunks number of total chunks to be downloaded
	 */
	private void downloadChunk(int fileID, int chunkID, ProgressBar progress, int numChunks) {
		es.execute(new Runnable() {

			@Override
			public void run() {
				/*
				 * DLCHUNK FILE_ID CHUNK_ID is format of the download chunk command
				 * An object is expected as the result, which is a serialised FileChunk object
				 */
				String message = String.format("DLCHUNK %d %d", fileID, chunkID);
				Object response = Util.sendMessageAndReceiveObject(message, seederAddress, seederPort);
				if (response != null) {
					//is this a multithreading issue?
					FileChunk chunk = (FileChunk) response;
					byte[] chunkData = chunk.getChunk();
					RandomAccessFile raf = null;
					//byte[] hash = Util.generateChunkHash(chunkData);
					//if (Arrays.equals(hash, chunk.getHash())) { //not implemented checking hsh of chunks
						try {
							//synchronized (lockMap) { //locking file was found to be unnecessary
/*								while (lockMap.get(fileID)) {
									//do nothing
								}*/
								lockMap.put(fileID, true); //lock the file for use
								raf = new RandomAccessFile(downloadLocation.getAbsolutePath() + "\\" + fileMap.get(fileID).getFileName(), "rw");
								raf.seek(chunk.get_id() * 1024); //place position at correct point in file
								//System.out.println("chunk size: " + chunkData.length);
								raf.write(chunkData, 0, chunkData.length);
								raf.close();
								lockMap.put(fileID, false); //writing is done, unlock the file
							//}
							synchronized (progressMap) { //this slows down the download, but not sure how else to do it with multithreading
								double currentProgress = progressMap.get(fileID) + (1.0/numChunks);
								progressMap.put(fileID, currentProgress);
								Platform.runLater(() -> progress.setProgress(currentProgress));
								//System.out.println(progress.getProgress());
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					//} else { //if hash doesn't match try redownload
						//downloadChunk(fileID, chunkID); 
						//TODO implement redownload of chunk
					//}
					
				}
			}
			
		});
	}
	
	/**
	 * Shuts down the thread pool
	 */
	public void terminate() {
		es.shutdown();
	}
	
	/**
	 * refreshes the list of files from the seeder
	 */
	public void updateFiles() {
		DatagramSocket requestSocket = null;
		DatagramPacket dp = null;
		DatagramPacket responsePacket = null;
		byte[] data;
		
		try {
			/*
			 * LST is format of the command to list files from seeder
			 * A string is expected as the result with the format of:
			 * FILELIST EMPTY when there are no files
			 * FILELIST NUM_FILES when there are files
			 */
			requestSocket = new DatagramSocket();
			String message = "LST";
			data = message.getBytes();
			dp = new DatagramPacket(data, data.length, seederAddress, seederPort);
			requestSocket.send(dp);
			System.out.format("local port: %d%n", requestSocket.getLocalPort());
			System.out.format("Address: %s, port: %d, message: %s%n", dp.getAddress(), dp.getPort(), new String(dp.getData(), 0, dp.getData().length));
			byte[] responseData = new byte[1024];
			responsePacket = new DatagramPacket(responseData, responseData.length);
			requestSocket.receive(responsePacket);
			System.out.println("leecher: response received");
			String response = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim(); //trim removes any possible whitespace at the end, avoiding casting errors
			if (response.equals("FILELIST EMPTY")) {
				//show an alert that there are no files to download
				Alert alert = new Alert(AlertType.INFORMATION, "Seeder has no files to send.", ButtonType.OK);
				alert.show();
				fileMap.clear(); //no files in list
			} else if (response.startsWith("FILELIST")) {
				fileMap.clear(); //empty the list
				System.out.println(response);
				
				System.out.println(response.split(" ")[1]);
				int numfiles = Integer.parseInt(response.split(" ")[1]); //the file quantity is 10 characters into the response
				System.out.println("Number of files: "+numfiles);
				
				//get the file list
				for(int i = 0; i < numfiles; i++) {
					requestSocket.receive(responsePacket);
					
					/**
					 * Note: when sending multiple files, weird data was coming through as part of the string
					 * Upon investigation, this was found to be because I was setting the string length to be the data buffers length (data.length)
					 * and not the packets length (responsePacket.getLength()). 
					 * When using the correct length vale from the packet, this issue was corrected.
					 * Yippee.
					 */
					
					response = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
					String[] fDetails = response.split(";"); //";" is used as delimiter
					FileItem fi = new FileItem(fDetails[0], fDetails[1], fDetails[2], fDetails[3]);
					fileMap.put(fi.get_id(), fi);
					System.out.println("File added");
				}
			}
			
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (requestSocket != null) requestSocket.close();
		}
	}

	/**
	 * @return the files
	 */
	public ObservableList<FileItem> getFiles() {
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(ObservableList<FileItem> files) {
		this.files = files;
	}

	/**
	 * @return the downloadLocation
	 */
	public File getDownloadLocation() {
		return downloadLocation;
	}

	/**
	 * @param downloadLocation the downloadLocation to set
	 */
	public void setDownloadLocation(File downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	/**
	 * @return the vbProgress
	 */
	public VBox getVbProgress() {
		return vbProgress;
	}

	/**
	 * @param vbProgress the vbProgress to set
	 */
	public void setVbProgress(VBox vbProgress) {
		this.vbProgress = vbProgress;
	}
}
