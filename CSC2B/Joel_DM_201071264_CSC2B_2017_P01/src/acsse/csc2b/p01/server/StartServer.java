/**
 * 
 */
package acsse.csc2b.p01.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Joel, DM, 201071264
 *
 */
public class StartServer implements Runnable{
	private ServerSocket ss;
	private Socket s;
	private final int serverPort = 1337;
	
	//flag whether the socket is looping for connections
	private static boolean isListening = true;
	
	private String rootDirPath;
	
	/**
	 * Constructor for the server socket
	 * @param rootDirPath root path for web server
	 */
	public StartServer(String rootDirPath) {
		super();
		this.rootDirPath = rootDirPath;
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		
		try {
			//initialise serverSocket
			ss = new ServerSocket(serverPort);
			System.out.println("waiting");
			//new thread to listen from console whether shut down command "exit" is entered
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (isListening) {
						if (sc.hasNextLine()) {
							if (sc.nextLine().equals("exit")) {
								isListening = false;
								try {
									ss.close();
									System.out.println("closing...");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					} 
					sc.close();
				}
				
			});
			thread.start();
			
			//listen for connection until isListening flag is false
			while (isListening) {
				s = ss.accept();
				//create a new runnable webSocket to run the socket and run in its own thread
				webSocket wb = new webSocket(s, rootDirPath);
				Thread t = new Thread(wb);
				t.start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				ss.close(); //close server socket
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
