
import javax.swing.JFrame;

/**
 * Class for the EZFILE client
 * @author 201071264, Joel, DM
 *
 */
public class EZFILEClient
{
	/**
	 * main entry point
	 * @param argv command line arguments
	 */
    public static void main(String[] argv)
    {
    	//initalise and launch the frame
		EZFILEClientFrame frame = new EZFILEClientFrame();
		frame.setEnabled(true);
		frame.setSize(720, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
    }
}
