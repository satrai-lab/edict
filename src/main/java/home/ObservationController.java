package home;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import customControls.LabeledCheckComboBox;
import customControls.LabeledTextField;
import dataParser.DataParser;
import guimodel.Appl;
import guimodel.ApplicationCategory;
import guimodel.Device;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import guimodel.Topic;

public class ObservationController implements Initializable{

    @FXML
    private VBox FormBox;

    @FXML
    private Pane FormPane;

    @FXML
    private Button SubmitButton;

	LabeledTextField id;
	LabeledTextField name;
	LabeledCheckComboBox<String> CapturedBy;
	LabeledCheckComboBox<String> RecievedBy;
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		id= new LabeledTextField("id",LabeledTextField.TYPE_TEXT);
		name= new LabeledTextField("name",LabeledTextField.TYPE_TEXT);
		ArrayList<Object> devices = DataParser.readModelFromCSv("devices",Device.class);
		ObservableList<String> devicesIds = FXCollections.observableArrayList();
		for(Object device:devices) {
			if(device instanceof Device) {
				devicesIds.add(((Device) device).getDeviceId());	
			}
			
		}
		CapturedBy= new LabeledCheckComboBox<String>("Captured by",FXCollections.observableArrayList(devicesIds));
		ArrayList<Object> applications = DataParser.readModelFromCSv("applications",Appl.class);
		ObservableList<String> applicationsIds = FXCollections.observableArrayList();
		for(Object application:applications) {
			if(application instanceof Appl) {
				applicationsIds.add(((Appl) application).getAppId());	
			}
			
		}
		RecievedBy= new LabeledCheckComboBox<String>("Recieved by",FXCollections.observableArrayList(applicationsIds));
		FormBox.getChildren().addAll(id,name,CapturedBy,RecievedBy);
	}
	public void initData(Topic top) {
		id.setText(top.getId());
		name.setText(top.getName());
		id.setDisable(true);
		
		SubmitButton.setText("Edit");

		
	}


	@FXML 
	public void saveTopic() { 
		String topic="";
		for (Node node : FormBox.getChildren()) {
			if(node instanceof LabeledTextField) {
				System.out.println(((LabeledTextField) node).getText());
				topic+=((LabeledTextField) node).getText()+",";
			
			}
			if(node instanceof LabeledCheckComboBox) {
				System.out.println(((LabeledCheckComboBox) node).getCheckedItems());
				for(Object o:((LabeledCheckComboBox) node).getCheckedItems()) {
					topic+=o.toString()+";";
				}
				if(((LabeledCheckComboBox) node).getCheckedItems().size()==0) {
					topic+=";";
				}
				
				topic+=",";
			}
		}
		DataParser.addModeltoCsv("observations",topic );
		
		  Stage stage = (Stage) SubmitButton.getScene().getWindow();
		  stage.close();


	}
}
