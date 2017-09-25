/**
 * 
 */
package acsse.csc2b.p05.gui;

import java.io.File;

import acsse.csc2b.p05.FileItem;
import acsse.csc2b.p05.seeder.Seeder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * @author Joel, DM, 201071264
 *GUI class for the seeder mode
 */
public class SeederGUI {
	Stage stage;
	int port;
	Seeder seeder;
	
	/**
	 * Constructor for the seeder GUI
	 * @param stage the stage to show the control on
	 * @param port the port which the seeder is running on
	 */
	public SeederGUI(Stage stage, int port) {
		this.stage = stage;
		this.port = port;
		seeder = new Seeder(port);
		
		Label lblPort = new Label("The seeder is running on port " + this.port);
		
		Button btnAddFile = new Button("Add File");
		btnAddFile.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			File file = fc.showOpenDialog(this.stage);
			seeder.addFile(file);
		});
		
		//tableview of the files, and the columns
		TableView<FileItem> tv = new TableView<FileItem>();
		TableColumn<FileItem, Integer> colId = new TableColumn<FileItem, Integer>("ID");
		colId.setCellValueFactory(new PropertyValueFactory<FileItem, Integer>("_id"));
		TableColumn<FileItem, String> colName = new TableColumn<FileItem, String>("File Name");
		colName.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileName"));
		TableColumn<FileItem, Long> colSize = new TableColumn<FileItem, Long>("File Size (bytes)");
		colSize.setCellValueFactory(new PropertyValueFactory<FileItem, Long>("size"));
		/*TableColumn<FileItem, String> colHash = new TableColumn<FileItem, String>("File Hash");
		colHash.setCellValueFactory(new PropertyValueFactory<FileItem, String>("hashString"));*/
		
		
		/*
		 * Code to remove a file from the table view
		 * Adapted from https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
		 * It creates a custom cell with a button that removes the row that cell is in when clicked
		 * 
		 * A callback is an interface in JavaFX allowing the cell to call back and return a new cell when updateItem is called
		 * i.e. when the row is updated, the updateItem function is called, and overridden here to put the remove file button inside
		 */
		
		TableColumn<FileItem, String> colRemove = new TableColumn<FileItem, String>("");
		colRemove.setCellValueFactory(new PropertyValueFactory<FileItem, String>("nothing")); //needs some cellvaluefactory to render, hence the meaningless value
		Callback<TableColumn<FileItem, String>, TableCell<FileItem, String>> cellRemove = new Callback<TableColumn<FileItem, String>, TableCell<FileItem, String>>() {
			@Override
			public TableCell<FileItem, String> call(final TableColumn<FileItem, String> param) {
				final TableCell<FileItem, String> cell = new TableCell<FileItem, String>() {
					final Button btnRemove = new Button("Remove");
					@Override
					public void updateItem(String item, boolean empty) { //returns (null, true) if empty
						super.updateItem(item, empty);
						if (empty) { //if it is empty, ie file removed, set the text and graphic back to null
							setGraphic(null);
							setText(null);
						} else { //otherwise, if not empty, create the button to remove the file
							btnRemove.setOnAction(event -> {
								FileItem file = getTableView().getItems().get(getIndex());
								seeder.getFileMap().remove(file.get_id());
							});
							setGraphic(btnRemove); //setGraphic(...) puts the rendered button inside the cell
							setText(null);
						}
					}
				};
				return cell;
			}
		};
		
		colRemove.setCellFactory(cellRemove);
		
		//bind the file list to the table view
		tv.setItems(seeder.getFiles());
		tv.getColumns().add(colRemove);
		tv.getColumns().add(colId);
		tv.getColumns().add(colName);
		tv.getColumns().add(colSize);
		//tv.getColumns().add(colHash);
		
		VBox pane = new VBox();
		
		pane.getChildren().addAll(lblPort, btnAddFile, tv);
		Scene scene = new Scene(pane, 720, 480);
		pane.prefHeightProperty().bind(scene.heightProperty());
		pane.prefWidthProperty().bind(scene.widthProperty());
		stage.setScene(scene);
		stage.show();
		
		/*
		 * Need to close the listening thread when terminating application
		 */
		stage.setOnCloseRequest(e -> {
			seeder.stopListening();
		});
	}

}
