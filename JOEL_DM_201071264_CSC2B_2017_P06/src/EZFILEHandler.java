import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class EZFILEHandler implements Runnable
{
    private static final String users = ".\\data\\server\\users.txt";
    private static final String imgList = ".\\data\\server\\ImageList.txt";
    private static final String imgFolder = ".\\data\\server\\";
    
    //streams
    Scanner sc = null;
    PrintWriter pw = null;
    DataOutputStream dos = null;
    Socket s = null;
    
    boolean isAuthenticated;
    boolean isConnected;
    
    public EZFILEHandler(Socket newConnectionToClient)
    {
    	s = newConnectionToClient;
		try {
			
			sc = new Scanner(newConnectionToClient.getInputStream());
			pw = new PrintWriter(newConnectionToClient.getOutputStream());
			dos = new DataOutputStream(new BufferedOutputStream(newConnectionToClient.getOutputStream()));
			

	    	isConnected = true;
	    	isAuthenticated = false;
			
		} catch (IOException e) {
			e.printStackTrace();
			pw.println("ERR " + e.getMessage());
			pw.flush();
		}
    }
    
    public void run()
    {
    	String request = "";
    	while (isConnected) {
    		if (sc.hasNextLine()) { //should cause scanner to wait for input
    			System.out.println("there si a nextline");
    			request = sc.nextLine(); //client should only be sending one line at a time 
    			System.out.println("Next line is: " + request);
    			parseRequest(request);
    		}
    	}
    }
    
    private void parseRequest(String request) {
    	Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				String[] requests = request.split(" ");
		    	System.out.println("request 0 :" + requests[0]);
		    	switch (requests[0].trim()) {
		    	case("LOGIN"):
		    		System.out.println("login case");
		    		if (!isAuthenticated) {
			    		String username = requests[1];
			    		String password = requests[2];
			    		System.out.println("u&p: " + username + " " + password);
			    		if (matchUser(username, password)) {
			    			isAuthenticated = true;
			    			pw.println("OK LOGIN SUCCESSFUL. WELCOME " + username + " TO EZFILE!");
			    			pw.flush();
			    			pw.println("...");
							pw.flush();
			    		} else {
			    			pw.println("ERR LOGIN UNSUCCESSFUL. USERNAME OR PASSWORD IS INCORRECT.");
			    			pw.flush();
			    			pw.println("...");
							pw.flush();
			    		}
		    		} else {
		    			pw.println("ERR USER IS ALREADY LOGGED IN.");
		    			pw.flush();
		    			pw.println("...");
						pw.flush();
		    		}
		    		break;
		    	case("LIST"):
		    		if (isAuthenticated) {
		    			System.out.println("sending list");
		    			ArrayList<String> fileList = getImageList();
		    			if (fileList.size() > 0) {
		    				pw.println("OK SENDING LIST");
		    				pw.flush();
		    				for (String s : fileList) {
		    					System.out.println(s);
		    					pw.println(s);
		    					pw.flush();
		    				}
		    				pw.println("..."); //indicates end of transmission
		    				pw.flush();
		    			} else {
		    				pw.println("ERR NO FILES IN LIST");
		    				pw.flush();
		    				pw.println("...");
							pw.flush();
		    			}
		    		} else {
		    			pw.println("ERR NOT LOGGED IN.");
		    			pw.flush();
		    			pw.println("...");
						pw.flush();
		    		}
		    		break;
		    	case("IMGRET"):
		    		if (isAuthenticated) {
		    			String imgLoc = idToFile(requests[1]);
		    			if (!imgLoc.equals("")) {
		    				File file = new File(imgFolder + imgLoc);
		    				FileInputStream fis = null;
		    				String message = String.format("OK SIZE %d %n", file.length());
		    				pw.println(message);
		    				pw.flush();
		    				pw.println("DATA");
		    				pw.flush();
		    				try {
		    					fis = new FileInputStream(file);
		    					byte[] buffer = new byte[1024];
		    					while (fis.read(buffer) != -1) {
		    						dos.write(buffer);
		    						dos.flush();
		    					}
		    					System.out.println("done sending");
		    					pw.println("...");
								pw.flush();
							} catch (IOException e) {
								e.printStackTrace();
								pw.println("ERR " + e.getMessage());
								pw.flush();
								pw.println("...");
								pw.flush();
							} finally {
								try {
									if (fis != null) fis.close();
								} catch (IOException e) {
									e.printStackTrace();
									pw.println("ERR " + e.getMessage());
									pw.flush();
									pw.println("...");
									pw.flush();
								}
							}
		    			} else {
		    				pw.println("ERR IMAGE ID NOT FOUND");
		    				pw.flush();
		    				pw.println("...");
							pw.flush();
		    			}
		    		} else {
		    			pw.println("ERR NOT LOGGED IN.");
		    			pw.flush();
		    			pw.println("...");
						pw.flush();
		    		}
		    		break;
		    	case("LOGOUT"):
		    		if (isAuthenticated) {
		    			isConnected = false;
		    			pw.println("OK LOGGING OFF.");
		    			pw.flush();
						pw.println("...");
						pw.flush();
		    			try {
		    				if (pw != null) pw.close();
		        			if (sc!=null) sc.close();
							if (s!=null) s.close();
						} catch (IOException e) {
							e.printStackTrace();
							pw.println("ERR " + e.getMessage());
							pw.flush();
							pw.println("...");
							pw.flush();
						}
		    		} else {
		    			pw.println("ERR NOT LOGGED IN.");
		    			pw.flush();
		    			pw.println("...");
						pw.flush();
		    		}
		    		break;
		    	default:
		    		pw.println("ERR NO SUCH REQUEST.");
		    		pw.flush();
		    		pw.println("...");
					pw.flush();
		    		break;
		    	}
				
			}
    		
    	});
    	t.start();
    }
    
    private boolean matchUser(String username,String password)
    {
	boolean found = false;
	File userFile = new File(users);
	try
	{
	    Scanner scan = new Scanner(userFile);
	    while(scan.hasNextLine()&&!found)
	    {
		String line = scan.nextLine();
		String lineSec[] = line.split("\\s");
    		
		if (username.equals(lineSec[0]) && password.equals(lineSec[1])) found = true;
		
	    }
	    scan.close();
	}
	catch(IOException ex)
	{
	    ex.printStackTrace();
	    pw.println("ERR " + ex.getMessage());
		pw.flush();
	}
	
	return found;
    }
    
    private ArrayList<String> getImageList()
    {
		ArrayList<String> result = new ArrayList<String>();
		File lstFile = new File(imgList);
		try
		{
		    Scanner scan = new Scanner(lstFile);
	
		    while (scan.hasNextLine()) {
		    	String fileLine = scan.nextLine();
		    	result.add(fileLine);
		    }
		    
		    scan.close();
		}	    
		catch(IOException ex)
		{
		    ex.printStackTrace();
		    pw.println("ERR " + ex.getMessage());
			pw.flush();
		}
		
		return result;
    }
    
    private String idToFile(String ID)
    {
    	String result = "";
    	File lstFile = new File(imgList);
    	if (lstFile.exists()) System.out.println("file exists");
    	try
    	{
    		Scanner scan = new Scanner(lstFile);
    		boolean found = false;
    		while (scan.hasNextLine() && !found) {
    			String line = scan.nextLine();
    			System.out.println(line);
    			String[] lineSplit = line.split(" ");
    			if (lineSplit[0].equals(ID)) {
    				found = true;
    				result = lineSplit[1];
    			}
    		}
    		scan.close();
    	}
    	catch(IOException ex)
    	{
    		ex.printStackTrace();
    		pw.println("ERR " + ex.getMessage());
			pw.flush();
    	}
    	return result;
    }
}
