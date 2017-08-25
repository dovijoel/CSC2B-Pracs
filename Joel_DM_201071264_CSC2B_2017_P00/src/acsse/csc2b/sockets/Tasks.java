/**
 * 
 */
package acsse.csc2b.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

/**
 * @author Joel, DM, 201071264
 *
 */
public class Tasks {
	
	/**
	 * Returns the local ip address 
	 * @return local ip address
	 */
	public static String getLocalAddress() {
		String localAddress = "";
		try {
			//creates an InetAddress object of the local machine
			InetAddress local = InetAddress.getLocalHost();
			//string of local machine
			localAddress = local.getHostAddress();
		} catch (UnknownHostException ex) {
			System.err.println(ex);
		}
		
		return localAddress;
	}
	
	/**
	 * fills the Observable array of ports with the information
	 */
	public static void getOpenPorts(ObservableList<SocketScan> ports, TextArea text) {
		//fill array with false ie not open
		//create a cached thread pool to check all ports
		ExecutorService tp = Executors.newCachedThreadPool();
		ports.clear();
		//loop through all ports
		for (int i = 1; i <= 65535; i++) {
			ports.add(new SocketScan(false, i, 0));
		}
		for (int i = 0; i < 65535; i++) {
			final int current = i;
			
			//execute a new thread for each port
			tp.execute(new Runnable() {
				@Override
				public void run() {
					Socket socket = null;
					//try open the port, and if successful, add to array
					try {
						socket = new Socket("localhost", current + 1);
						//System.out.printf("%d successful%n", current + 1);
						//if successful, no exception thrown
						ports.get(current).setOpen(true);
						ports.get(current).setLocal(socket.getLocalPort());
						ports.get(current).setMessage("Connection Successful.");
					} catch (UnknownHostException e) {
						ports.get(current).setMessage(e.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						ports.get(current).setMessage(e.toString());
					} finally {
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								 System.err.println(e);
							} 
						}
					}
				}
				
			});
		}
		tp.shutdown();
		
		while (!tp.isTerminated()) {
			//System.out.println("awaiting termination...");
			//wait until all ports are checked
		}
		text.setText(text.getText() + "Port Scan Completed.\n");
	}

}
