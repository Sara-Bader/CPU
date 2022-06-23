package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
// this is the interface screen that shows up to the user to see information at a choosen time
public class SpecificTimeDetails implements Initializable {


    @FXML
    private TextField chosenTime; // get the chosen time from the user
    @FXML
    private TableColumn<SimpleStringProperty, String> info;
    @FXML
    private TableView<SimpleStringProperty> infoTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        info.setCellValueFactory(new PropertyValueFactory("value"));
    }

    public static Map.Entry<Long, LinkedList<String>> getMaxEntryInMapBasedOnKey(Map<Long, LinkedList<String>> map) {
        Map.Entry<Long, LinkedList<String>> entryWithMaxKey = null;
        for (Map.Entry<Long, LinkedList<String>> currentEntry : map.entrySet()) {
            if (entryWithMaxKey == null || currentEntry.getKey() > entryWithMaxKey.getKey()) {
                entryWithMaxKey = currentEntry;
            }
        }
        return entryWithMaxKey;
    }

    public void getDetails(ActionEvent event){
        Map<Long, LinkedList<String>> specificTimeDetails = TimeDetailsLock.specificTimeDetails;

        for ( int i = 0; i < infoTable.getItems().size(); i++) {
            infoTable.getItems().clear();
        }
        if(chosenTime.getText() == null){ // if no value wwas eneterd pop an error message
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Value");
            alert.setContentText("Enter the time");
            alert.showAndWait();
        }
        else {
            long time = Long.parseLong(chosenTime.getText());
            LinkedList<String> details = specificTimeDetails.get(time);
            if (details == null || details.size() == 0)
                infoTable.getItems().add(new SimpleStringProperty("Nothing Changed At This Time"));
            else
                for (String detail : details)
                    infoTable.getItems().add(new SimpleStringProperty(detail));
        }
    }
}