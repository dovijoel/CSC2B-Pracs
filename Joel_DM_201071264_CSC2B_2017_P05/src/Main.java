import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import acsse.csc2b.p05.gui.LeecherGUI;
import acsse.csc2b.p05.gui.SeederGUI;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.*;
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
	public void start(Stage primaryStage) throws Exception {
		//initial GUI to choose Leecher or Seeder mode
		Label lblChoose = new Label("Please choose a mode, either Seeder or Leecher");
		
		//Buttons for each mode, and the event handlers to launch those modes
		Button btnSeeder = new Button("Seeder Mode");
		btnSeeder.setOnAction(e -> {
			TextInputDialog input = new TextInputDialog();
			input.setHeaderText("Please enter port number.");
			Optional<String> strPort = input.showAndWait();
			int port = 0;
			if (strPort.isPresent()) {
				port = Integer.parseInt(strPort.get());
			}
			new SeederGUI(primaryStage, port);
		});
		
		Button btnLeecher = new Button("Leecher Mode");
		btnLeecher.setOnAction(e -> {
			//show dialog for ip address and port
			//adapted from http://code.makery.ch/blog/javafx-dialogs-official/
			Dialog<Pair<String, Integer>> serverDetail = new Dialog<>();
			
			//set the button
			serverDetail.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			serverDetail.setTitle("Seeder details");
			serverDetail.setHeaderText("Please enter address and port of seeder.");
			
			//add the server address and port fields
			Label lblAddress = new Label("Seeder Address");
			Label lblPort = new Label("Seeder Port");
			TextField txtAddress = new TextField();
			//txtAddress.setText("localhost"); //testing purposes
			TextField txtPort = new TextField();
			//txtPort.setText("1337"); //testing purposes
			
			GridPane fieldGrid = new GridPane();
			fieldGrid.setHgap(10);
			fieldGrid.setVgap(10);
			fieldGrid.setPadding(new Insets(20, 150, 10, 10));
			
			fieldGrid.add(lblAddress, 0, 0);
			fieldGrid.add(txtAddress, 1, 0);
			fieldGrid.add(lblPort, 0, 1);
			fieldGrid.add(txtPort, 1, 1);
			
			//place the grid into the dialog
			serverDetail.getDialogPane().setContent(fieldGrid);
			
			//return the values when ok is clicked
			serverDetail.setResultConverter(dialogButton -> {
			    if (dialogButton == ButtonType.OK) {
			        return new Pair<>(txtAddress.getText(), Integer.parseInt(txtPort.getText()));
			    }
			    return null;
			});
			
			//now the dialog can be shown to get the server details
			Optional<Pair<String, Integer>> result = serverDetail.showAndWait();
			result.ifPresent(dialogResult -> {
			    try { //if address is valid, new gui will launch, otherwise will get error
					InetAddress seederAddress = InetAddress.getByName(dialogResult.getKey());
					int port = dialogResult.getValue();
					new LeecherGUI(primaryStage, seederAddress, port);
					
				} catch (UnknownHostException e1) {
					Alert alert = new Alert(AlertType.ERROR, e1.getMessage(), ButtonType.OK);
					alert.show();
					e1.printStackTrace();
				}
			});

			
		});
		
		VBox pane = new VBox();
		pane.setPadding(new Insets(10));
		pane.getChildren().addAll(lblChoose, btnSeeder, btnLeecher);
		btnSeeder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		btnLeecher.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
