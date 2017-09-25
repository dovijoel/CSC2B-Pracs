/**
 * 
 */
package acsse.csc2b.p05.gui;

import java.net.InetAddress;

import acsse.csc2b.p05.FileItem;
import acsse.csc2b.p05.leecher.Leecher;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * @author Joel, DM, 201071264
 * GUI class for the leecher mode
 */
public class LeecherGUI {
	Stage stage;
	int seederPort;
	InetAddress seederAddress;
	Leecher leecher;
	
	/**
	 * Constructor for the leecher gui
	 * @param stage the JavaFX stage to show controls on
	 * @param seederAddress the InetAddress of the seeder
	 * @param seederPort the port of the seeder
	 */
	public LeecherGUI(Stage stage, InetAddress seederAddress, int seederPort) {
		this.stage = stage;
		this.seederPort = seederPort;
		this.seederAddress = seederAddress;
		
		leecher = new Leecher(seederAddress, seederPort);
		Label lblDetails = new Label("Seeder details are: " + seederAddress.getHostName() + ":" + seederPort);
		
		//tableview of the remote files
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
		 * This adds a checkbox control to the table view, in order to download the files
		 */
		TableColumn<FileItem, String> colCheck = new TableColumn<FileItem, String>("");
		colCheck.setCellValueFactory(new PropertyValueFactory<FileItem, String>("nothing")); //needs some cellvaluefactory to render, hence the meaningless value
		Callback<TableColumn<FileItem, String>, TableCell<FileItem, String>> cellCheck = new Callback<TableColumn<FileItem, String>, TableCell<FileItem, String>>() {
			@Override
			public TableCell<FileItem, String> call(final TableColumn<FileItem, String> param) {
				final TableCell<FileItem, String> cell = new TableCell<FileItem, String>() {
					final CheckBox chkDownload = new CheckBox();
					@Override
					public void updateItem(String item, boolean empty) { //returns (null, true) if empty
						super.updateItem(item, empty);
						if (empty) { //if it is empty, ie file removed, set the text and graphic back to null
							setGraphic(null);
							setText(null);
						} else { //otherwise, if not empty, create the button to remove the file
							chkDownload.setSelected(false);
							chkDownload.selectedProperty().addListener(e -> {
								boolean download = chkDownload.isSelected(); //get checkbox state
								FileItem file = getTableView().getItems().get(getIndex()); //get file to download
								int index = leecher.getFiles().indexOf(file); //index of file in list
								leecher.getFiles().get(index).setToDownload(download);
							});
							setGraphic(chkDownload); //setGraphic(...) puts the rendered check box inside the cell
							setText(null);
						}
					}
				};
				return cell;
			}
		};
		
		colCheck.setCellFactory(cellCheck);
		
		//bind the file list to the table view
		tv.setItems(leecher.getFiles());
		tv.getColumns().add(colCheck);
		tv.getColumns().add(colId);
		tv.getColumns().add(colName);
		tv.getColumns().add(colSize);
		//tv.getColumns().add(colHash);
		
		//download location
		
		TextField txtDownloadLocation = new TextField();
		txtDownloadLocation.setEditable(false);
		txtDownloadLocation.setText(leecher.getDownloadLocation().getAbsolutePath());		
		
		/*
		 * buttons
		 */
		Button btnSetLocation = new Button("Set Download Location");
		Button btnRefresh = new Button("Refresh File List");
		Button btnDownload = new Button("Download Selected");
		
		ButtonBar buttons = new ButtonBar();
		buttons.getButtons().addAll(btnSetLocation, btnRefresh, btnDownload);
		ButtonBar.setButtonData(btnSetLocation, ButtonData.LEFT);
		ButtonBar.setButtonData(btnRefresh, ButtonData.LEFT);
		ButtonBar.setButtonData(btnDownload, ButtonData.LEFT);
		
		
		/*
		 * button actions
		 */
		btnSetLocation.setOnAction(e -> {
			DirectoryChooser dc = new DirectoryChooser();
			dc.setTitle("Choose download directory");
			leecher.setDownloadLocation(dc.showDialog(stage));
			txtDownloadLocation.setText(leecher.getDownloadLocation().getAbsolutePath());
		});
		
		btnRefresh.setOnAction(e -> {
			leecher.updateFiles();
		});
		
		btnDownload.setOnAction(e -> {
			leecher.downloadFiles();
		});
		
		/*
		 * Download location
		 */
		HBox hbLocation = new HBox(10);
		hbLocation.setPadding(new Insets(5));
		HBox.setHgrow(txtDownloadLocation, Priority.ALWAYS);
		hbLocation.getChildren().addAll(new Label("Download folder:"), txtDownloadLocation);
		
		/*
		 * Location for the progress bars to be placed
		 */
		VBox vbProgress = new VBox(10);
		leecher.setVbProgress(vbProgress);
		
		VBox root = new VBox(10);
		root.setPadding(new Insets(5));
		root.getChildren().addAll(lblDetails, buttons, hbLocation, tv, vbProgress);
		VBox.setVgrow(buttons, Priority.ALWAYS);
		
		Scene scene = new Scene(root, 720, 480);
		root.prefHeightProperty().bind(scene.heightProperty());
		root.prefWidthProperty().bind(scene.widthProperty());
		stage.setScene(scene);
		stage.show();
		
		/*
		 * close all threads and terminate the application
		 */
		stage.setOnCloseRequest(e -> {
			leecher.terminate();
		});
	}
}
