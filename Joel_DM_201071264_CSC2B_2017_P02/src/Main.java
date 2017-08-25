/**
 * 
 */

import java.io.IOException;
import java.net.UnknownHostException;

import acsse.csc2b.p02.client.Client;
import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
/**
 * @author Joel, DM, 201071264
 *
 */
public class Main extends Application {
	private Client client;

	@Override
	public void start(Stage primaryStage) throws Exception {
		//labels
		Label lblTxtIn = new Label("Server Output:");
		Label lblTxtOut = new Label("Your Input");
		Label lblBrowser = new Label("Definition Output:");
		
		//text areas
		TextArea txtIn = new TextArea();
		//txtIn.setEditable(false);
	
		TextField txtOut = new TextField();
		txtOut.setPromptText("Enter desired definition here");
		
		//display definitiion result in browser
		final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
		
		//buttons
		Button btnConnect = new Button("Connect");
		Button btnReady = new Button("Ready");
		btnReady.setDisable(true);
		Button btnDefine = new Button("Define");
		btnDefine.setDisable(true);
		Button btnDone = new Button("Done");
		btnDone.setDisable(true);
		
		//button events
		btnConnect.setOnAction(e -> {
			try {
				//instantiate new client
				client = new Client(txtIn, webEngine);
				//enable buttons and bind to connection state
				btnReady.setDisable(false);
				btnDone.setDisable(false);
				System.out.println("new client created");
			} catch (UnknownHostException ex) {
				txtIn.setText(txtIn.getText() + ex.getMessage() + "\n");
			} catch (IOException ex) {
				txtIn.setText(txtIn.getText() + ex.getMessage() + "\n");
			}
		});
		
		btnReady.setOnAction(e -> {
			if (client != null) {
				try {
					client.sendCommand("READY");
					//disable ready button, enable define button
					btnReady.setDisable(true);
					btnDefine.setDisable(false);
				} catch (IOException ex) {
					txtIn.setText(txtIn.getText() + ex.getMessage() + "\n");
				}
			}
		});
		
		btnDefine.setOnAction(e -> {
			if (!txtOut.getText().equals("")) {
				try {
					client.sendCommand("DEFINE " + txtOut.getText());
				} catch (IOException ex) {
					txtIn.setText(txtIn.getText() + ex.getMessage() + "\n");
				}
			} else {
				Alert alert = new Alert(AlertType.ERROR, "Please enter a definition in the text area!", ButtonType.OK);
				alert.show();
			}
			
		});
		
		btnDone.setOnAction(e -> {
			if (client != null) {
				try {
					client.close();
					btnDefine.setDisable(true);
					btnDone.setDisable(true);
				} catch (IOException ex) {
					txtIn.setText(txtIn.getText() + ex.getMessage() + "\n");
				}
			}
		});
		
		//layouts
		VBox VBtxt = new VBox(5);
		VBtxt.getChildren().addAll(lblTxtIn, txtIn, lblTxtOut, txtOut, lblBrowser, browser);
		HBox HBbuttons = new HBox(5);
		HBbuttons.getChildren().addAll(btnConnect, btnReady, btnDefine, btnDone);
		
		VBox VBlayouts = new VBox(5);
		VBlayouts.getChildren().addAll(VBtxt, HBbuttons);
		
		//scene and primarystage
		Scene scene = new Scene(VBlayouts);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Practical 2 - Client to Server Communications");
		primaryStage.setMaximized(true);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
