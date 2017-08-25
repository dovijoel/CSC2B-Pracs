/**
 * 
 */
package acsse.csc2b.p03.smtp;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Joel, DM, 201071264
 * Class for email messages
 */
public class Email {
	//variables
	private String sender;
	private String recipient;
	private String subject;
	private String message;
	private ArrayList<File> attachments;
	
	
	/**
	 * Constructor with attachments
	 * @param sender
	 * @param recipient
	 * @param message
	 * @param attachments
	 */
	public Email(String sender, String recipient, String message, String subject, ArrayList<File> attachments) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.subject = subject;
		this.attachments = attachments;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @return the recipient
	 */
	public String getRecipient() {
		return recipient;
	}

	/**
	 * @param recipient the recipient to set
	 */
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the attachments
	 */
	public ArrayList<File> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(ArrayList<File> attachments) {
		this.attachments = attachments;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

}
