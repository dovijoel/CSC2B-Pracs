import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EZFILEServer
{
	private static final int port = 2016;
	static boolean isRunning = true;
	static ServerSocket ss = null;
    public static void main(String[] argv)
    {
    	try {
			ss = new ServerSocket(port);
			while (isRunning) {
				Socket s = ss.accept();
				System.out.println("connection accepted");
				Thread t = new Thread(new EZFILEHandler(s));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
}
