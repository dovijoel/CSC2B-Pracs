import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * EZFILE client frame class
 * @author 201071264, Joel, DM
 *
 */
public class EZFILEClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	//default download location
	private static final String downloadLoc = ".\\data\\client\\";
	
	//declare socket and streams
	Socket s = null;
	Scanner sc = null;
	PrintWriter pw = null;
	DataInputStream dis = null;
	
	//list for images
	Vector<String> imageList = new Vector<>();
	
	//image data
	String id = "-1";
	String imgName;
	
	//output area
	JTextArea output;
	
	/**
	 * Default constructor
	 */
    public EZFILEClientFrame()
    {
    	//buttons
    	JButton btnConnect = new JButton("Connect");
    	JButton btnList = new JButton("List Images");
    	btnList.setEnabled(false);
    	JButton btnDownload = new JButton("Download Image");
    	btnDownload.setEnabled(false);
    	JButton btnLogout = new JButton("Log Out");
    	btnLogout.setEnabled(false);
    	
    	
    	//server details //not going to show this
    	JTextField txtServerAddress = new JTextField("localhost");
    	JTextField txtServerPort = new JTextField("2016");
    	
    	//username and password
    	JTextField txtUsername = new JTextField("Bob");
    	JTextField txtPassword = new JTextField("123");
    	
    	//image detail list
    	JList<String> lstImages = new JList<String>();
    	lstImages.setListData(imageList);
    	
    	//listener to set the image to download when selected
    	lstImages.addListSelectionListener(e -> {
    		
    		if (lstImages.isSelectionEmpty()) {
    			btnDownload.setEnabled(false);
    			id = "-1"; //-1 indicates nothing is selected
    		} else {
    			btnDownload.setEnabled(true);
        		String selection = lstImages.getSelectedValue();
        		String[] ary = selection.split(" ");
        		id = ary[0];
        		imgName = ary[1];
    		}
    	});
    	
    	//server responses
    	output = new JTextArea();
    	output.setEditable(false);
    	
    	//panel for buttons
    	JPanel buttons = new JPanel();
    	buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    	buttons.add(btnConnect);
    	buttons.add(btnList);
    	buttons.add(btnDownload);
    	buttons.add(btnLogout);
    	buttons.setAlignmentY(LEFT_ALIGNMENT);
    	
    	//panel for username
    	JPanel userPane = new JPanel();
    	userPane.add(new JLabel("Username:"));
    	userPane.add(txtUsername);
    	userPane.setAlignmentY(LEFT_ALIGNMENT);
    	
    	//panel for password
    	JPanel passPane = new JPanel();
    	passPane.add(new JLabel("Password:"));
    	passPane.add(txtPassword);
    	
    	
    	//button events
    	btnConnect.addActionListener(e -> {
    		try {
    			//first try connect to server
				s = new Socket(txtServerAddress.getText(), Integer.parseInt(txtServerPort.getText()));
				System.out.println("succesffully connected");
				
				//initialise all streams
				pw = new PrintWriter(s.getOutputStream());
				sc = new Scanner(s.getInputStream());
				dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
				
				//try login
				pw.format("LOGIN %s %s%n", txtUsername.getText(), txtPassword.getText());
				pw.flush();
				
				//initialise flag for receiving data loop
				//sc.hasNextLine() blocks the application
				boolean receiving = true;
				System.out.println("sent login");
				while (receiving) {
					String response = sc.nextLine();
					System.out.println("got response");
					
					//if responds with OK, then logged in and enable functions
					if (response.startsWith("OK")) {
						btnConnect.setEnabled(false);
						btnList.setEnabled(true);
						btnLogout.setEnabled(true);
					}
					
					if (response.startsWith("...")) receiving = false;
					addToOutPut(response);
				}
				
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    		});
    	
    	btnList.addActionListener(e -> {
    		boolean receiving = true; //flag for receivng from server
    		boolean firstTime = true; //flag to know whether it is the first loop
    		pw.println("LIST");
    		pw.flush();
    		lstImages.removeAll();
    		while (receiving) {
    			String response = sc.nextLine();
    			addToOutPut(response);
    			System.out.println(response);
    			if (firstTime) { //if it is the first loop, just the OK response is expected
    				if (response.startsWith("OK")) {
    					receiving = true; //technically unnecessary as it is already true
    				} else {
    					receiving = false; //if ok was not received on first loop thann an error has happened
    				}
    				firstTime = false;
    			} else {
    				if (response.startsWith("...")) { //temrinating stirng
    					receiving = false;
    				} else {
    					imageList.add(response);
    					System.out.println("image added");
    				}
    			}
    		}
    		//update the JList component
    		lstImages.setListData(imageList);
    	});
  
    	btnDownload.addActionListener(e -> {
    		if (!id.startsWith("-1")) { //checks if a valid id is selected
    			boolean receiving = true;
    			pw.println("IMGRET " + id);
    			pw.flush();
    			while (receiving) {
    				String response = sc.nextLine();
    				addToOutPut(response);
    				System.out.println(response);
    				if (response.startsWith("...")) receiving = false;
    				if (response.startsWith("OK")) {
    					//nothing to do really
    				}
    				if (response.startsWith("DATA")) {
    					//open the file at the download location and declare the file output stream
    					File file = new File(downloadLoc + imgName);
    					FileOutputStream fos = null;
    					byte[]  buffer = new byte[1024]; //buffer size must be 1024 at both sides
    					try {
    						
							fos = new FileOutputStream(file);
							boolean dataSending = true;
							int counter = 0;
							
							/*
							 * In order to know when the file sending is done, the number of bytes received is checked.
							 * If it is less than 1024, it could be either the last bit of the image, or the image could 
							 * have been exactly divisible by 1024, and the last bit is not part of the image
							 * So that last bit is converted to a string and checked. If it starts with the terminating
							 * string "...", than end the outer thread, otherwise add it to the image file, and loop back
							 * and receive the final expected response from the server of the terminating string.
							 */
							while (dataSending) {
								int received = dis.read(buffer);
								counter += received;
								if (received < 1024) {
									String r = new String(buffer, 0, received);
									if (r.startsWith("...")) {
										System.out.println(r);
										receiving = false;
									} else {
										fos.write(buffer);
									}
									dataSending = false;
								} else {
									System.out.println(received);
		    						fos.write(buffer);
								}
	    					}
							System.out.println(counter);
							System.out.println("done receiving");
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} finally {
							if (fos != null)
								try {
									fos.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
						}
    					
    				}
    			}
    		}
    	});
    	
    	btnLogout.addActionListener(e -> {
    		boolean receiving = true;
    		pw.println("LOGOUT");
    		pw.flush();
    		while (receiving) {
    			String response = sc.nextLine();
    			System.out.println(response);
    			addToOutPut(response);
    			if (response.startsWith("...")) receiving = false;
    		}
				try {
					//close all streams
					if (s != null) s.close();
					if (pw != null) pw.close();
		    		if (sc != null) sc.close();
		    		if (dis != null) dis.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			//once logged out, change buttons to original status
    		btnConnect.setEnabled(true);
    		btnList.setEnabled(false);
    		btnDownload.setEnabled(false);
    		btnLogout.setEnabled(false);
    	});

    	//root pane with all the components
    	JRootPane root  = new JRootPane();
    	root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    	root.add(buttons);
    	root.add(userPane);
    	root.add(passPane);
    	root.add(lstImages);
    	root.add(output);
    	
    	//add the root pane
    	add(root);
    	pack();
    }
    
    /**
     * add a String message to the output textArea
     * @param message message to add
     */
    private void addToOutPut(String message) {
    	output.setText(output.getText() + message + "\n");
    }
}
