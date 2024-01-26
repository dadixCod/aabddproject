package controllers;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import static managedb.FXMLDocumentController.psw;
import static managedb.FXMLDocumentController.usn;
import tools.TableComboBoxManager;
import tools.myconnection;
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

    private void openUpdateDialog(Map<String, String> rowData) {
    // Create a new dialog for updating
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Update Row");

    // Create TextFields for each column in the selected row
    TextField[] textFields = createTextFields(rowData);

    // Add the TextFields to the dialog
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);

    for (int i = 0; i < textFields.length; i++) {
        gridPane.add(new Label(rowData.keySet().toArray()[i].toString()), 0, i);
        gridPane.add(textFields[i], 1, i);
    }

    dialog.getDialogPane().setContent(gridPane);

    // Add buttons to the dialog
    ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

    // Set the result converter
    dialog.setResultConverter(buttonType -> {
        if (buttonType == updateButtonType) {
            Map<String, String> updatedData = new HashMap<>();
            for (int j = 0; j < textFields.length; j++) {
                updatedData.put(rowData.keySet().toArray()[j].toString(), textFields[j].getText());
            }
            return updatedData;
        }
        return null;
    });

    // Show the dialog and handle the result
    Optional<Map<String, String>> result = dialog.showAndWait();

    result.ifPresent(updatedData -> {
        performUpdateOperation(updatedData,rowData);
    });
}

private TextField[] createTextFields(Map<String, String> rowData) {
    TextField[] textFields = new TextField[rowData.size()];
    int i = 0;

    for (Map.Entry<String, String> entry : rowData.entrySet()) {
        TextField textField = new TextField(entry.getValue());
        textFields[i] = textField;
        i++;
    }

    return textFields;
}

    private void promptDeleteConfirmation(Map<String, String> rowData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this row?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteOperation(rowData);
        }
    }
    private String getPrimaryKeyColumn(String tableName) {
    String primaryKeyColumn = "";

    // Your logic to determine the primary key column based on the table name
    // For demonstration purposes, assuming the primary key column is the first column
    try {
        Statement statement = myconnection.cnx.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNM" + tableName);
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Assuming the primary key is the first column
        primaryKeyColumn = metaData.getColumnName(1);

        resultSet.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return primaryKeyColumn;
}

    private void performUpdateOperation(Map<String, String> updatedData, Map<String, String> rowData) {
    // Retrieve the selected table name from the global variable or pass it to this controller
    String selectedTableName = FirstWindowController.selectedTable;
    String primaryKeyColumn = getPrimaryKeyColumn(selectedTableName);

    // Create the SQL UPDATE statement
    StringBuilder updateQuery = new StringBuilder("UPDATE SYNM");
    updateQuery.append(selectedTableName).append(" SET ");

    // Append the updated values to the SET clause
    for (Map.Entry<String, String> entry : updatedData.entrySet()) {
        updateQuery.append(entry.getKey()).append(" = '").append(entry.getValue()).append("', ");
    }

    // Remove the trailing comma and space
    if (updateQuery.length() > 2) {
        updateQuery.setLength(updateQuery.length() - 2);
    }

    // Add the WHERE clause to identify the row to update
    updateQuery.append(" WHERE ");

    // Identify the primary key column (replace "ID" with your actual primary key column)
 

    updateQuery.append(primaryKeyColumn).append(" = '").append(rowData.get(primaryKeyColumn)).append("'");

    // Execute the UPDATE statement
    try {
        Statement statement = myconnection.cnx.createStatement();
        int rowsUpdated = statement.executeUpdate(updateQuery.toString());

        if (rowsUpdated > 0) {
            System.out.println("Row updated successfully!");
        } else {
            System.out.println("No rows were updated.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error updating row");
    }
}



private void performDeleteOperation(Map<String, String> rowData) {
    String tableName = selectedTable;

    // Create the SQL DELETE statement
    StringBuilder deleteQuery = new StringBuilder("DELETE FROM SYNM");
    deleteQuery.append(tableName).append(" WHERE ");

    // Append values for the WHERE clause
    for (Map.Entry<String, String> entry : rowData.entrySet()) {
        deleteQuery.append(entry.getKey()).append(" = '").append(entry.getValue()).append("' AND ");
    }

    // Remove the trailing "AND"
    if (deleteQuery.length() > 4) {
        deleteQuery.setLength(deleteQuery.length() - 4);
    }

    // Execute the DELETE statement
    try {
        Statement statement = myconnection.cnx.createStatement();
        statement.executeUpdate(deleteQuery.toString());
        System.out.println("Row deleted successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error deleting row");
    }
}
}
