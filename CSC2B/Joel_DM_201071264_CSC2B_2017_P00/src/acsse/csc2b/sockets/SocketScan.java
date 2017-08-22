/**
 * 
 */
package acsse.csc2b.sockets;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Joel, DM, 201071264
 * Class for socket scan results to be used in an observable list for a JavaFX Table view
 */
public class SocketScan {
	private SimpleBooleanProperty open;
	private SimpleIntegerProperty remote;
	private SimpleIntegerProperty local;
	private SimpleStringProperty message;
	
	public SocketScan (boolean open, Integer remote, int local) {
		this.open = new SimpleBooleanProperty(open);
		this.remote = new SimpleIntegerProperty(remote);
		this.local = new SimpleIntegerProperty(local);;
		message = new SimpleStringProperty("Not yet scanned.");
	}

	/**
	 * @return whether it is open
	 */
	public boolean isOpen() {
		return open.get();
	}

	/**
	 * @param open whether port is open
	 */
	public void setOpen(boolean open) {
		this.open.set(open);
	}

	/**
	 * @return the remote port
	 */
	public int getRemote() {
		return remote.get();
	}

	/**
	 * @param remote the remote port to set
	 */
	public void setRemote(int remote) {
		this.remote.set(remote);
	}

	/**
	 * @return the local port
	 */
	public int getLocal() {
		return local.get();
	}

	/**
	 * @param local the local port to set
	 */
	public void setLocal(int local) {
		this.local.set(local);
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message.get();
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message.set(message);
	}

}
