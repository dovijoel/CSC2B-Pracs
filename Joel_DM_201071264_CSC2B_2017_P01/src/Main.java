import acsse.csc2b.p01.server.StartServer;

/**
 * 
 */

/**
 * @author Joel, DM, 201071264
 * Main entry point for application
 */
public class Main {

	/**
	 * @param args directory to start in
	 */
	public static void main(String[] args) {
		String rootPath;
		if (args.length == 1) {
			rootPath = args[0];
		} else {
			rootPath = "./www";
		}
		//start server socket in new thread
		Thread t = new Thread(new StartServer(rootPath));
		t.start();
	}

}
