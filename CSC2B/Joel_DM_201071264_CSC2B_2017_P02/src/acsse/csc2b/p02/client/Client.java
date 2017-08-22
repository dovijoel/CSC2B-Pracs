/**
 * 
 */
package acsse.csc2b.p02.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;

/**
 * @author Joel, DM, 201071264
 *
 */
public class Client {
	private final TextArea txtIn;
	private final WebEngine webEngine;
	//constants
	private static final int serverPort = 8888;
	private static final String serverAddress = "localhost";
	
	private Socket socket;
	
	//streams
	PrintWriter pw;
	Scanner sc;
	
	SimpleBooleanProperty responseFlag = new SimpleBooleanProperty(true);
	StringBuffer responseString = new StringBuffer("");
	
	//public method to create a connection
	public Client(TextArea txtIn, WebEngine webEngine) throws UnknownHostException, IOException { //throws to the caller method, so can display errors in text area
		socket = new Socket(serverAddress, serverPort);
		pw = new PrintWriter(socket.getOutputStream());
		sc = new Scanner(socket.getInputStream());
		this.txtIn = txtIn;
		this.webEngine = webEngine;
		responseFlag.set(true);
		
		//thread to continually scan for new responses from server
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (responseFlag.get()) {
					responseString.append(sc.nextLine());
					try {
						parseResponse(responseString.toString());
					} catch (IOException ex) {
						txtIn.setText(txtIn.getText() + ex.getMessage());
					}
					responseString.delete(0, responseString.length());
				}
				
			}
			
		});
		t.start();
	}
	
	/**
	 * Checks the response and responds to the GUI accordingly
	 * @param response
	 * @throws IOException
	 */
	private void parseResponse(String response) throws IOException  {
		if (response.startsWith("DEFINITION:")) { //if its a definition, parse it
			StringTokenizer st = new StringTokenizer(response, "\t");
			StringBuilder sb = new StringBuilder();
			//add entire response to a long string
			while (st.hasMoreTokens()) {
				sb.append(st.nextToken() + " ");
			}
			
			//parse the tags
			String formatted = "<HTML><BODY>" + parseTags(sb.toString()) + "</BODY></HTML>";
			//needs to run later in the javaFX thread
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					webEngine.loadContent(formatted);
				}
			});
		} else if (response.startsWith("GOODBYE")) { //shuts down the thread if server sends GOODBYE
			txtIn.setText(txtIn.getText() + response + "\n");
			pw.close();
			sc.close();
			socket.close();
			responseFlag.set(false);
		} else { //other
			txtIn.setText(txtIn.getText() + response + "\n");
		}
	}
	
	/**
	 * Parses the raw body of the definition to a formatted HTML response
	 * @param body raw body to parse
	 * @return parsed body with tags formatted
	 */
	private String parseTags(String body) {
		StringBuilder sb = new StringBuilder();
		StringTokenizer stWord = new StringTokenizer(body);
		while (stWord.hasMoreTokens()) {
			String word = stWord.nextToken(" ");
			if (word.startsWith("DEFINITION")) {
				sb.append("<h2>" + word + "</h2>");
			} else if (word.startsWith("{")) {
				if ((word.contains("}"))) { //one word cases
					String[] result = word.split("}");
					if (result.length > 1) { //one word results will only ever have two parts, before and after the end tag terminator
						sb.append("<b>" + result[0].substring(1, result[0].length()) + "</b>" + result[1] + " "); 
					} else {
						sb.append("<b>" + result[0].substring(1, result[0].length()) + "</b> "); 
					}
				} else {
					String nextWord = stWord.nextToken("}");
					word = word.substring(1, word.length()) + nextWord.substring(0, nextWord.length());
					sb.append("<b>" + word + "</b> ");
				}
				
			} else if (word.startsWith("}")) { 
				//ignore trailing tag
			} else if (word.startsWith("<")) {
				if ((word.contains(">"))) { //one word cases
					String[] result = word.split(">");
					if (result.length > 1) { //one word results will only ever have two parts, before and after the end tag terminator
						sb.append("<i>" + result[0].substring(1, result[0].length()) + "</i>" + result[1] + " "); 
					} else {
						sb.append("<i>" + result[0].substring(1, result[0].length()) + "</i> "); 
					}
				} else {
					String nextWord = stWord.nextToken(">");
					word = word.substring(1, word.length()) + nextWord.substring(0, nextWord.length());
				}
				sb.append("<i>" + word + "</i> ");
			} else if (word.startsWith(">")) { 
				//ignore trailing tag
			} else {
				sb.append(word + " ");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Send a command to the server
	 * @param command command to send to the server
	 * @throws IOException thrown exception
	 */
	public void sendCommand(String command) throws IOException  {
		pw.println(command);
		pw.flush();
	}
	
	/**
	 * Closes connection to the server
	 * @throws IOException thrown exception
	 */
	public void close() throws IOException {
		sendCommand("DONE");
	}

	/**
	 * @return the responseFlag
	 */
	public SimpleBooleanProperty getResponseFlag() {
		return responseFlag;
	}

}
