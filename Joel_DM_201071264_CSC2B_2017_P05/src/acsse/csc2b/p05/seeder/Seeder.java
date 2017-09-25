/**
 * 
 */
package acsse.csc2b.p05.seeder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import acsse.csc2b.p05.FileChunk;
import acsse.csc2b.p05.FileItem;
import acsse.csc2b.p05.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * @author Joel, DM, 201071264
 * Seeder class containing logic for seeding
 */
public class Seeder {
	ObservableList<FileItem> files;
	ObservableMap<Integer, FileItem> fileMap; //file map for easy lookup
	int port;
	DatagramSocket listeningSocket;
	
	boolean isListening;
	
	ExecutorService es = Executors.newFixedThreadPool(500);
	
	public Seeder(int port) {
		fileMap = FXCollections.observableHashMap();
		files = FXCollections.observableArrayList(fileMap.values());
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
		
		this.port = port;
		isListening = true;
		startListening(); 
	}
	
	/**
	 * Adds a file to list of files availble for leeching
	 * @param file file to add to list
	 */
	public void addFile(File file) {
		FileItem fi = new FileItem(file);
		fileMap.put(fi.get_id(), fi);
	}
	
	/**
	 * Start listening for connections from leechers
	 * Multithreaded so can send multiple files at once
	 */
	private void startListening() {
		isListening = true;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					listeningSocket = new DatagramSocket(port);
					System.out.println(listeningSocket.getLocalPort());
					while (isListening()) {
						DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
						//System.out.format("seeder: local address: %s, local port: %d%n", listeningSocket.getLocalAddress(), listeningSocket.getLocalPort());
						listeningSocket.receive(dp);
						//System.out.println("Packet received");
						//System.out.println(new String(dp.getData(), 0, dp.getData().length));
						parsePacket(dp);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		
	}
	
	/**
	 * Shut down while loop bu=y setting isListening flag to false
	 * And closes the listening socket
	 */
	public void stopListening() {
		isListening = false;
		if (listeningSocket != null) listeningSocket.close();
	}

	/**
	 * Parses the received packet, and performs actions based on the result
	 * @param dp the DatagramPacket to parse
	 */
	private void parsePacket(DatagramPacket dp) {
		String response = new String(dp.getData(), 0, dp.getData().length);
		//System.out.println("response is: " + response);
		String[] responseAry = response.split(" ");
		//handle the respose requests, which are always the first word
		//System.out.println("response code is: " + responseAry[0]);
		Integer fileID;
		Integer chunkID;
		/*
		 * switch to handle all cases
		 * needs to be trimmed in order for trailing whitespaces to be removed
		 */
		switch (responseAry[0].trim()) {
		case "LST": //lisu files
			System.out.println("case LST");
			sendFileList(dp);
			break;
		case "DOWNLOAD": //send details of file to download
			System.out.println("case DOWNLOAD");
			fileID = Integer.parseInt(responseAry[1].trim());
			Util.sendMessage("FILEDETAILS " + fileMap.get(fileID).get_id() + " " + 
									fileMap.get(fileID).getNumberOfChunks(), dp.getAddress(), dp.getPort());
			break;
		case "DLCHUNK": //send a file chunk
			//TODO better error correction
			fileID = Integer.parseInt(responseAry[1].trim());
			chunkID = Integer.parseInt(responseAry[2].trim());
			sendChunk(fileID, chunkID, dp.getAddress(), dp.getPort());
			break;
		default:
			System.out.println("NO CASE");
			break;
		}
	}
	
	/**
	 * Sends the chunk
	 * @param fileID
	 * @param chunkID
	 * @param leecherAddress
	 * @param leecherPort
	 */
	private void sendChunk(int fileID, int chunkID, InetAddress leecherAddress, int leecherPort) {
		es.execute(new Runnable() { //multithreaded to enable multiple file and chunk sneding

			@Override
			public void run() {
				FileChunk chunk = new FileChunk(chunkID, fileID, fileMap.get(fileID).getFilePath());
				Util.sendObject(chunk, leecherAddress, leecherPort);
			}
			
		});
	}
	
	/**
	 * Sends a list of files available for download
	 * @param dp datagram packet of leecher
	 */
	private void sendFileList(DatagramPacket dp) {
		ByteArrayOutputStream byteOs = null;
		DatagramSocket ds = null;
		ObjectOutputStream objOs = null;
		try {
			ds= new DatagramSocket();
			
			InetAddress remoteAddress = dp.getAddress();
			
			int remotePort = dp.getPort();
			System.out.format("seeder: remote address: %s, remote port: %d%n", remoteAddress, remotePort);
			byte[] data = null;
			DatagramPacket sendPacket = null;
			if (fileMap.size() > 0) {
				String message = "FILELIST " + fileMap.size();
				data = message.getBytes();
				sendPacket = new DatagramPacket(data, data.length, remoteAddress, remotePort);
				ds.send(sendPacket);
				for (FileItem f : fileMap.values()) {
					//send the details of the file in the format required
					String fDetails = f.get_id() + ";" + f.getFileName() + ";" + f.getHashString() + ";" + f.getSize(); //using ";" as delimiter
					System.out.format("Sending file: %s%n", fDetails);
					data = fDetails.getBytes();
					sendPacket = new DatagramPacket(data, data.length, remoteAddress, remotePort);
					ds.send(sendPacket);
				}
			} else { //if no files to send
				String message = "FILELIST EMPTY";
				data = message.getBytes();
				sendPacket = new DatagramPacket(data, data.length, remoteAddress, remotePort);
				ds.send(sendPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ds != null) ds.close();
				if (objOs != null) objOs.close();
				if (byteOs != null) byteOs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the isListening
	 */
	public boolean isListening() {
		return isListening;
	}

	/**
	 * @param isListening the isListening to set
	 */
	public void setListening(boolean isListening) {
		this.isListening = isListening;
	}

	/**
	 * @return the fileMap
	 */
	public ObservableMap<Integer, FileItem> getFileMap() {
		return fileMap;
	}

	/**
	 * @param fileMap the fileMap to set
	 */
	public void setFileMap(ObservableMap<Integer, FileItem> fileMap) {
		this.fileMap = fileMap;
	}
}
