/**
 * 
 */

import acsse.csc2b.sockets.SocketScan;

import acsse.csc2b.sockets.Tasks;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * @author Joel, DM, 201071264
 * Main entry point for the application
 */
public class Main extends Application {
	//create an observable list and a table view, for all the ports
	//used documentation from JavaDocs
	ObservableList<SocketScan> ports = FXCollections.observableArrayList();
	TableView<SocketScan> table = new TableView<SocketScan>();

	/**
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		//Display text area
		TextArea text = new TextArea();
		text.setEditable(false);
		TableColumn<SocketScan, Integer> numberCol = new TableColumn<SocketScan, Integer>("Port No.");
		numberCol.setCellValueFactory(new PropertyValueFactory<SocketScan, Integer>("remote"));
		TableColumn<SocketScan, Boolean> openCol = new TableColumn<SocketScan, Boolean>("Open Status");
		openCol.setCellValueFactory(new PropertyValueFactory<SocketScan, Boolean>("open"));
		TableColumn<SocketScan, Integer> localCol = new TableColumn<SocketScan, Integer>("Local Port");
		localCol.setCellValueFactory(new PropertyValueFactory<SocketScan, Integer>("local"));
		TableColumn<SocketScan, String> messageCol = new TableColumn<SocketScan, String>("Connection Message");
		messageCol.setCellValueFactory(new PropertyValueFactory<SocketScan, String>("message"));
		
		/*for (int i = 1; i <= 65535; i++) {
			ports.add(new SocketScan(false, i, 0));
		}*/
		
		table.setItems(ports);
		table.getColumns().add(numberCol);
		table.getColumns().add(openCol);
		table.getColumns().add(localCol);
		table.getColumns().add(messageCol);
		Button btnScanLocal = new Button ("Scan Local Ports");
		btnScanLocal.setOnAction(e -> {
			text.setText(text.getText() + "Scanning ports 1 - 65535...\n");
			Thread thread = new Thread(new Runnable () {

				@Override
				public void run() {
					Tasks.getOpenPorts(ports, text);
				}
				
			});
			
			thread.start();
			
		});
		Button btnDisplayLocalAddress = new Button("Display Local Address");
		btnDisplayLocalAddress.setOnAction(e ->	{
			text.setText(text.getText() + 
							"Local IP Adress: " + Tasks.getLocalAddress() + "\n");
		});
		
		//create a flow pane to add the buttons
		FlowPane buttonPane = new FlowPane();
		//add the buttons
		buttonPane.getChildren().addAll(btnScanLocal, btnDisplayLocalAddress);
		Text info = new Text("Once scan is completed, you can sort by \"Open Status\" column. ");
		GridPane grid = new GridPane();
		grid.add(info, 0, 0);
		grid.add(table, 0, 1);
		grid.add(text, 0, 2);
		grid.add(buttonPane, 0, 3);
		Scene scene = new Scene(grid);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Practical 0 - Sockets");
		primaryStage.show();
		
		
	}

}
