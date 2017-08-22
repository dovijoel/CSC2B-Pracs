/**
 * 
 */
package acsse.csc2b.p03.smtp;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Joel, DM, 201071264
 * Client for sending emails via SMTP
 */
public class SMTPclient {
	//static variables of server and server port
	private static final String server = "csc2b.uj.ac.za";
	private static final int port = 25;
	
	//boundary for MIME parts
	private static final String boundary = "hellodarknessmyoldfriend";
	
	public static String sendEmail(Email email) {
		String response = "";
		
		//sockets, readers and writers
		Socket s = null;
		Scanner sc = null;
		PrintWriter pw = null;
		BufferedOutputStream os = null;
		FileInputStream fs = null;
		try {
			//open socket and initialise readers and writers
			s = new Socket(server, port);
			sc = new Scanner(s.getInputStream());
			pw = new PrintWriter(s.getOutputStream());
			os = new BufferedOutputStream(new DataOutputStream(s.getOutputStream()));
			
			response = sc.nextLine();
			System.out.println(response);
			//check for 220 response
			if (response.startsWith("220")) {
				pw.println("HELO " + server);
				pw.flush();
				response = sc.nextLine();
				System.out.println(response);
				if(response.startsWith("250")) { //connection successful
					pw.println("MAIL FROM:<" + email.getSender() + ">");
					pw.flush();
					
					response = sc.nextLine();
					System.out.println(response);
					if(response.startsWith("250")) { //successful response
						pw.println("RCPT TO:<" + email.getRecipient() + ">");
						pw.flush();
						
						response = sc.nextLine();
						System.out.println(response);
						if(response.startsWith("250")) { //successful response
							//Now the actual email can start being sent
							pw.println("DATA");
							pw.flush();
							
							response = sc.nextLine();
							System.out.println(response);
							if(!(response.startsWith("5"))) { //successful response - 5XX messages indicate failure
								//send message
								//MIME and email headers
								pw.print("MIME-Version: 1.0" + "\r\n"); //print MIME information
								pw.println("From: <" + email.getSender() + ">");
								pw.println("To: <" + email.getRecipient() + ">");
								pw.println("Date: " + (new Date(System.currentTimeMillis()).toString()));
								pw.println("Subject: " + email.getSubject());
								if (email.getAttachments().size() > 0) {
									pw.print("Content-Type: multipart/mixed;\r\n boundary=\"" + boundary + "\"\r\n");
									pw.print("\r\n--" + boundary + "\r\n");
								}
								pw.print("Content-Type: text/plain; charset=\"us-ascii\"" + "\r\n");
								pw.flush();
								os.write(("\r\n" + email.getMessage() + "\r\n").getBytes()); //need lots of CRLFs
								os.flush();
								
								//send attachments
								if (email.getAttachments().size() > 0) {
									
									for (int i = 0; i< email.getAttachments().size(); i++) {
										//new MIME section for attachments
										pw.print("\r\n--" + boundary + "\r\n");
										pw.print("Content-Type: application/octet-stream;" + "\r\n");
										pw.print("Content-Disposition: attachment; filename=\"" + email.getAttachments().get(i).getName() + "\"" + "\r\n");
										pw.print("Content-Transfer-Encoding: 8bit" + "\r\n\r\n");
										pw.flush();
										fs = new FileInputStream(email.getAttachments().get(i)); //create a filestream to the current attachment
										byte[] fileChunk = new byte[1024]; //initialise byte array buffer
										while ((fs.read(fileChunk) >=0 )) { //loop through byte buffer, reading the chunk into it...
											os.write(fileChunk);				//and then writing those chunks to the output stream
										}
										os.flush();
										fs.close();
									}
									pw.print("\r\n--" + boundary + "--" + "\r\n"); //terminating boundary
								}
								
								
								//send terminating . character
								pw.print("\r\n.\r\n");
								pw.flush();
								response = sc.nextLine();
								System.out.println(response);
								
								pw.println("QUIT");
								pw.flush();
								response = sc.nextLine();
								System.out.println(response);
							}
							
						}
					}
				}
			}
		} catch (IOException ex) {
			response = ex.getMessage();
		} finally {
			try {
				if (s != null) s.close();
				if (sc != null) sc.close();
				if (pw != null) pw.close();
				if (os != null) os.close();
				if (fs != null) fs.close();
			} catch (IOException ex) {
				response = ex.getMessage();
			}
		}
		
		return response;
	}
}
