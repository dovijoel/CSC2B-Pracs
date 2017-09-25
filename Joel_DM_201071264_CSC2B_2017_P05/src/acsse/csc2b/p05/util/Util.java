/**
 * 
 */
package acsse.csc2b.p05.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author Joel, DM, 201071264
 * Utility methods
 */
public class Util {
	/**
	 * Algorithm to generate an MD5 hash for a given file
	 * Adapted from: https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
	 * @param file file to compute hash for
	 * @return the byte array of the hash
	 */
	public static byte[] generateHash(File file) {
		MessageDigest md = null;
		InputStream is = null;
		try {
			//instantiate MessageDigest instance type
			md = MessageDigest.getInstance("MD5");
			//instantiate file stream
			is = new FileInputStream(file);
			//stream the file to the message digest
			DigestInputStream dis = new DigestInputStream(is, md);
			//dis.read from the stream, thereby computing the hash
			while (dis.read() != -1) {
				//do nothing
			}
			dis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		//compute the digest and return it
		return md.digest();
	}
	
	/**
	 * Generates the MD5 hash of the chunk
	 * @param chunk chunk to be hashed
	 * @return the byte array of the hash
	 */
	public static byte[] generateChunkHash(byte[] chunk) {
		MessageDigest md = null;
		InputStream is = null;
		try {
			//instantiate MessageDigest instance type
			md = MessageDigest.getInstance("MD5");
			md.update(chunk);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		//compute the digest and return it
		return md.digest();
	}
	
	/**
	 * Send a string message without waiting for a response through UDP
	 * @param message message to be sent
	 * @param address InetAddress of destination
	 * @param port port of destination
	 */
	public static void sendMessage(String message, InetAddress address, int port) {
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		try {
			ds = new DatagramSocket();
			byte[] data = message.getBytes();
			dp = new DatagramPacket(data, data.length, address, port);
			ds.send(dp);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (ds != null) ds.close();
		}
	}
	
	/**
	 * Send an object through UDP
	 * @param object object to be sent
	 * @param address InetAddress of destination
	 * @param port port of destination
	 */
	public static void sendObject(Object object, InetAddress address, int port) {
		//declare the streams
		ByteArrayOutputStream byteOs = null;
		DatagramSocket clientSocket = null;
		ObjectOutputStream objOs = null;
		
		try {
			clientSocket = new DatagramSocket();
			
			/*
			 * stream the fitnessData object into a bytestream by reading it from an object stream
			 * and then converting it to a byte array for sending
			 */
			byteOs = new ByteArrayOutputStream();
			objOs = new ObjectOutputStream(byteOs);
			objOs.writeObject(object);
			objOs.flush();
			objOs.close();
			byte[] data = byteOs.toByteArray();
			
			//declare the datagram packet
			DatagramPacket dgPacket = new DatagramPacket(data, data.length, address, port);
			//send the data
			clientSocket.send(dgPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close all the streams
			try {
				if (byteOs != null) byteOs.close();
				if (clientSocket != null) clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * send a message and await a string response through UDP
	 * @param message String message to be sent
	 * @param address InetAddress of destination
	 * @param port port of destination
	 * @return String response received
	 */
	public static String sendMessageAndReceiveString(String message, InetAddress address, int port) {
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		String response = "";
		try {
			ds = new DatagramSocket();
			byte[] data = message.getBytes();
			dp = new DatagramPacket(data, data.length, address, port);
			ds.send(dp);
			data = new byte[1024];
			dp = new DatagramPacket(data, data.length);
			ds.receive(dp);
			response = new String(data, 0, dp.getLength());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (ds != null) ds.close();
		}
		return response;
	}
	
	/**
	 * send a string message and await an object as a response through UDP
	 * @param message String message to be sent
	 * @param address InetAdress of destination
	 * @param port port of destination
	 * @return Object received as response
	 */
	public static Object sendMessageAndReceiveObject(String message, InetAddress address, int port) {
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		Object response = null;
		try {
			ds = new DatagramSocket();
			byte[] data = message.getBytes();
			dp = new DatagramPacket(data, data.length, address, port);
			ds.send(dp);
			data = new byte[2048]; //for some reason the byte array of a 1024 size chunk is 1166, found upon inspection when receiving a EOF exception
			dp = new DatagramPacket(data, data.length);
			ds.receive(dp);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(dp.getData()));
			response = in.readObject();
			
			in.close();
			
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		} finally {
			if (ds != null) ds.close();
		}
		return response;
	}
}
