/**
 * 
 */
package acsse.csc2b.p01.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joel, DM, 201071264
 * This class implements the web page loading and responses for each socket
 */
public class webSocket implements Runnable {
	private Socket socket;
	private BufferedReader bf;
	private PrintWriter pw;
	private BufferedOutputStream ds;
	private FileInputStream fs;
	private Scanner sc;
	
	//regex patterns
	private static final Pattern rootPattern = Pattern.compile("GET\\s\\/\\sHTTP/1.(0|1)");
	private static final Pattern filePattern = Pattern.compile("GET\\s\\/\\w+.\\w+\\sHTTP/1.(0|1)");
	private static final Pattern otherDirPattern = Pattern.compile("GET\\s(\\/\\w+)+\\/*\\sHTTP\\/1\\.(1|0)");
	
	//directory of the root path
	private String rootDirPath;
	private static final String indexPage = "/index.html";
	private static final String endPageTerminator = "\n\n";
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			System.out.println("connected");
			//initialise readers and writers
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());
			ds = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
			
			String line = bf.readLine(); //only needs the first line
			System.out.println(line);
			String fileName = extractPath(line);
			
			//use regex to handle the response
			Matcher mRoot = rootPattern.matcher(line);
			Matcher mFile = filePattern.matcher(line);
			Matcher mOtherDir = otherDirPattern.matcher(line);
			
			if (mRoot.matches()) { //for root, just load the root index.html
				//makes sure the index file exists in the root directory
				if (new File(rootDirPath + indexPage).exists()) {
					loadHTML(indexPage);
				} else {
					send404();
				}
			} else if (mFile.matches()) {
				//check if file exists
				if (new File(rootDirPath + fileName).exists()) {
					//get the file type from the file name
					StringTokenizer st = new StringTokenizer(fileName, ".");
					st.nextToken();
					String fileType = st.nextToken();
					FileType ft;
					if (fileType.toUpperCase().equals("HTML")) {
						ft = FileType.HTML;
					} else if (fileType.toUpperCase().equals("PNG") || fileType.toUpperCase().equals("JPG") || fileType.toUpperCase().equals("JPEG")) {
						ft = FileType.IMAGE;
					} else {
						ft = FileType.OTHER;
					}
					
					fs = new FileInputStream(rootDirPath + fileName);
					switch (ft) {
					case HTML:
						loadHTML(fileName);
						break;
					case IMAGE:
					case OTHER:
						loadFile(fileName);
						break;
					}
				} else { //if file doesn't exist, send 404
					//send the 404 code if unknown directory  
					send404();
				}
			} else if (mOtherDir.matches()) {
				if (new File(rootDirPath + fileName + "/index.html").exists()) {
					loadHTML(fileName + "/index.html");
				} else {
					send404();
				}
			} else {
				//if doesn't match anything, 500 unknown error
				send500();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				//close all the streams and sockets
				if (socket != null) socket.close();				
				if (pw!= null) pw.close();
				if (bf != null) bf.close();
				if (ds != null) ds.close();
				if (fs != null) fs.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Helper functions are below
	 */

	/**
	 * Extracts the path for web server to load
	 * @param line GET request to parse
	 * @return path for web server to load
	 */
	private String extractPath(String line) {
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken(); //file name is always second element of string
		return st.nextToken();
	}
	
	/**
	 * loads the specified html document
	 * @param docName name of the document to load
	 */
	private void loadHTML(String docName) {
		try {
			//send the 200 OK response
			pw.print("HTTP/1.0 200 OK\n\n");
			pw.flush();
			//create a filestream of the html document
			fs = new FileInputStream(rootDirPath + docName);
			sc = new Scanner(fs);
			while (sc.hasNext()) { //send the html document line by line
				pw.print(sc.nextLine());
			}
			pw.print(endPageTerminator); //terminate the document
			pw.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				//close stuff
				if (fs != null)	fs.close();
				if (sc != null) sc.close();
				if (pw != null) pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 * Loads a file to the browser as specified
	 * @param fileName
	 */
	private void loadFile(String fileName) {
		try {
			//send the 200 OK response
			pw.print("HTTP/1.0 200 OK\n\n");
			pw.flush();
			//create a filestream of the file
			fs = new FileInputStream(rootDirPath + fileName);
			//write 1024 bytes at a time into the buffer and the send it
			byte[] fileChunk = new byte[1024];
			while ((fs.read(fileChunk)) >= 0) { //send the buffer chunks until the filestream is at an end
				ds.write(fileChunk);
			}
			ds.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				//close everything
				if (fs != null)	fs.close();
				if (ds != null) ds.close();
				if (pw != null) pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 * Sends a 404 response code
	 */
	private void send404() {
		//send the 404 code if unknown path  
		pw.print("HTTP/1.0 404 Unknown File or Directory\n\n");
		pw.flush();
	}
	
	/**
	 * Sends a 500 response code
	 */
	private void send500() {
		//send the 404 code if unknown path  
		pw.print("HTTP/1.0 500 Unknown Error\n\n");
		pw.flush(); 
	}
	
	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * @param socket the socket to set
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	/**
	 * Constructor for webSocket instance
	 * @param socket the socket to use
	 * @param rootDirPath the root directory path
	 */
	public webSocket(Socket socket, String rootDirPath) {
		this.socket = socket;
		this.rootDirPath = rootDirPath;
	}

}
