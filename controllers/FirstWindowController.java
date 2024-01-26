package controllers;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import static managedb.FXMLDocumentController.psw;
import static managedb.FXMLDocumentController.usn;
import tools.TableComboBoxManager;
import tools.myconnection;
import static tools.myconnection.openUpdateDialog;
import static tools.myconnection.promptDeleteConfirmation;
import tools.myfunction;

public class FirstWindowController implements Initializable {

    @FXML
    private StackPane rootStackPane;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private VBox leftColumn;
    @FXML
    private Pane usercontainer;
    @FXML
    public Text userText;
    @FXML
    private Button addBtn;
    @FXML
    private JFXComboBox<String> listComboBox;
    @FXML
    private JFXTabPane tabPane;

    Stage stage = new Stage();
    public static String selectedTable = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userText.setText(usn);
        TableComboBoxManager.populateComboBox(listComboBox, usn, psw);

        listComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.getTabs().clear();

            selectedTable = newValue;

            if (selectedTable != null) {
                Tab newTab = new Tab(selectedTable);

                TableView<Map<String, String>> tableView = new TableView<>();

                ResultSet resultSet = myconnection.queryTableData(selectedTable);

                try {
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        final int columnIndex = i - 1;

                        TableColumn<Map<String, String>, String> column = new TableColumn<>(resultSet.getMetaData().getColumnName(i));
                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(column.getText())));
                        column.setMinWidth(160);
                        column.setMaxWidth(180);
                        tableView.getColumns().add(column);
                    }

                    while (resultSet.next()) {
                        Map<String, String> rowData = new HashMap<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            String columnName = resultSet.getMetaData().getColumnName(i);
                            rowData.put(columnName, resultSet.getString(i));
                        }
                        tableView.getItems().add(rowData);
                    }

                    newTab.setContent(tableView);
                    tabPane.getTabs().add(newTab);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                TableColumn<Map<String, String>, Void> updateColumn = new TableColumn<>("Update");
                TableColumn<Map<String, String>, Void> deleteColumn = new TableColumn<>("Delete");

                updateColumn.setCellFactory(param -> new TableCell<Map<String, String>, Void>() {
                    private final Button updateButton = new Button("Update");

                    {
                        updateButton.setOnAction(event -> {
                            Map<String, String> rowData = getTableView().getItems().get(getIndex());

                            // Open a new window/dialog for updating
                            openUpdateDialog(rowData);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(updateButton);
                        }
                    }
                });

                deleteColumn.setCellFactory(param -> new TableCell<Map<String, String>, Void>() {
                    private final Button deleteButton = new Button("Delete");

                    {
                        deleteButton.setOnAction(event -> {
                            Map<String, String> rowData = getTableView().getItems().get(getIndex());
                            promptDeleteConfirmation(rowData);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                });

                tableView.getColumns().addAll(updateColumn, deleteColumn);
            }
        });
    }

    @FXML
    private void addRow(ActionEvent event) {
        String selectedTable = listComboBox.getValue();

        boolean hasInsertPrivilege = myconnection.checkInsertPrivilege(selectedTable);
        if (hasInsertPrivilege) {
            AddRowController addRowController = (AddRowController) myfunction.loadWindow(
                    getClass().getResource("/views/viewAddRow.fxml"),
                    "Add Row",
                    stage,
                    "no"
            );
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission Denied");
            alert.setHeaderText(null);
            alert.setContentText("You don't have the privilege to add rows.");
            alert.showAndWait();
        }
    }

   
}
