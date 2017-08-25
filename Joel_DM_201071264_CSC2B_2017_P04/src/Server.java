import javax.swing.JFrame;

import acsse.csc2b.p04.server.PositionServer;
import acsse.csc2b.p04.server.gui.ServerFrame;

/**
 * 
 */

/**
 * Main server entry point, launches the server GUI
 * @author Joel, DM, 201071264
 *
 */
public class Server {
	/**
	 * @param args command line args
	 * Main method for application start
	 */
	public static void main(String[] args) {
		//declare and initialise server instance
		PositionServer server = new PositionServer();
		//declare and initialise custom ServerFrame
		ServerFrame application = new ServerFrame(server);
		//pass the frame to the server for plot rendering and refreshing
		server.setFrame(application);
		
		//start the application
		application.setSize(1280, 720);
		application.setLocationRelativeTo(null);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.setVisible(true);
	}

}
