import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import acsse.csc2b.p04.FitnessData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 */

/**
 * @author Joel, DM, 201071264
 * Client main application and GUI
 */
public class Client extends Application {
	private static final int port = 1989;
	private static Random r = new Random ();

	/**
	 * @param args command line args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * starts the application
	 * @param primaryStage the primaryStage
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		//labels
		Label lblHeart = new Label("Heart Rate:");
		Label lblSteps = new Label("Step Count:");
		Label lblStudentNo = new Label("Student Number:");
		
		//text fields
		TextField txtHeart = new TextField();
		TextField txtSteps = new TextField();
		TextField txtStudentNo = new TextField();
		
		//add data to data pane
		GridPane grdData  = new GridPane();
		grdData.add(lblStudentNo, 0, 0);
		grdData.add(lblSteps, 1, 0);
		grdData.add(lblHeart, 2, 0);
		grdData.add(txtStudentNo, 0, 1);
		grdData.add(txtSteps, 1, 1);
		grdData.add(txtHeart, 2, 1);
		
		//buttons
		Button btnSend = new Button("Send");
		Button btnClose = new Button("Close");
		
		//button events
		btnSend.setOnAction(e -> {
			//generateData(); //used this for testing purposes
			
			//get fitness data
			FitnessData fd = new FitnessData(txtStudentNo.getText(), Integer.parseInt(txtSteps.getText()), Integer.parseInt(txtHeart.getText()));
			//send the data
			sendData(fd);
		});
		
		btnClose.setOnAction(e -> {
			Platform.exit();
		});
		
		//pane for buttons
		HBox hbButtons = new HBox();
		hbButtons.getChildren().addAll(btnSend, btnClose);
		
		//main pane
		VBox pane = new VBox();
		pane.getChildren().addAll(grdData, hbButtons);
		 
		//start the GUI
		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Practical 04 - UDP Client");
		primaryStage.show();
	}
	
	/**
	 * For testing purposes
	 */
	private void generateData() {
		for (int j = 1; j <= 10; j++) {
			String student = String.format("%d", j*100 +r.nextInt(50));
			int [] x = new int [15];
			int [] y = new int [15];
			for (int i = 1; i < 15; i++)
			{
				x[i] = r.nextInt(1000);
				y[i] = r.nextInt (100);
				FitnessData fd = new FitnessData(student, x[i], y[i]);
				sendData(fd);
			}
		}
	}

	/**
	 * sends the fitness data over UDP
	 * @param fd the fitness data
	 */
	private void sendData(FitnessData fd) {
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
			objOs.writeObject(fd);
			byte[] data = byteOs.toByteArray();
			
			//declare the datagram packet
			DatagramPacket dgPacket = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), port);
			
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
				if (objOs != null) objOs.close();
				if (clientSocket != null) clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
