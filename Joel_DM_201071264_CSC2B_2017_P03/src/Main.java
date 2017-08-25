

import java.io.File;
import java.util.ArrayList;

import acsse.csc2b.p03.smtp.Email;
import acsse.csc2b.p03.smtp.SMTPclient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 */

/**
 * @author Joel, DM, 201071264
 *
 */
public class Main extends Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);

	}

	@Override
	/**
	 * the main method for the JavaFX application
	 */
	public void start(Stage primaryStage) throws Exception {
		ArrayList<File> attachments = new ArrayList<File>();
		
		//labels
		Label lblFrom = new Label("From:");
		Label lblTo = new Label("To:");
		Label lblMessage = new Label("Email Message:");
		Label lblSubject = new Label("Subject:");
		
		//textfields and textareas
		TextField txtFrom = new TextField();
		
		txtFrom.setText("201071264@csc2b.uj.ac.za");
		TextField txtTo = new TextField();
		
		txtTo.setText("201071264@csc2b.uj.ac.za");
		
		TextField txtSubject = new TextField();
		txtSubject.setText("CSC2B Practical 3 SMTP");
		
		TextArea txtMessage = new TextArea();
		
		
		//send button
		Button btnSend = new Button("Send");
		btnSend.setOnAction(e -> {
			Email email = new Email(txtFrom.getText(), txtTo.getText(), txtMessage.getText(), txtSubject.getText(), attachments);
			String response = SMTPclient.sendEmail(email);
			Alert alert = new Alert(AlertType.INFORMATION, "Email Successfully Sent\nLast response was: " + response, ButtonType.OK);
			alert.show();
			txtMessage.setText("");
			attachments.clear();
		});
		
		//attach button
		Button btnAttach = new Button("Attach");
		btnAttach.setOnAction(e -> { 
			FileChooser fc = new FileChooser();
			attachments.add(fc.showOpenDialog(primaryStage));
			Alert alert = new Alert(AlertType.INFORMATION, "Attachment added!", ButtonType.OK);
			alert.show();
		});
		
		//adding everything to the GUI
		GridPane detailsBox = new GridPane();
		detailsBox.add(lblFrom, 0, 0);
		detailsBox.add(lblTo, 0, 1);
		detailsBox.add(lblSubject, 0, 2);
		detailsBox.add(txtFrom, 1, 0);
		detailsBox.add(txtTo, 1, 1);
		detailsBox.add(txtSubject, 1, 2);
		txtFrom.setMaxWidth(Double.MAX_VALUE);
		txtTo.setMaxWidth(Double.MAX_VALUE);
		detailsBox.setMaxWidth(Double.MAX_VALUE);
	
		VBox buttonBox = new VBox();
		buttonBox.getChildren().addAll(btnSend, btnAttach);
		
		BorderPane topBox = new BorderPane();
		topBox.setMaxWidth(Double.MAX_VALUE);
		topBox.setLeft(detailsBox);
		topBox.setCenter(buttonBox);
		
		VBox mainPane = new VBox();
		mainPane.getChildren().addAll(topBox, lblMessage, txtMessage);
		topBox.prefWidthProperty().bind(mainPane.widthProperty());
		
		
		Scene scene = new Scene(mainPane);
		
		//set the scene and show it
		primaryStage.setScene(scene);
		primaryStage.setTitle("Practical 03 - SMTP client");
		primaryStage.show();
		
		
	}

}
