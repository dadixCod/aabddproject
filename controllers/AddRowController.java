package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import tools.myconnection;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import static tools.myconnection.isDateColumn;

public class AddRowController implements Initializable {

    @FXML
    private StackPane tootStackPane;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private Button insertButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Retrieve the selected table name from the global variable or pass it to this controller
        String selectedTableName = FirstWindowController.selectedTable;

        // Query the metadata of the selected table to get column names
        ResultSet resultSet = myconnection.queryTableMetadata(selectedTableName);

        Set<String> addedColumnNames = new HashSet<>();
        try {
            int textFieldCount = 0;
            // Iterate through the result set to create TextFields for each column
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");

                // Check if the column name is already added
                if (!addedColumnNames.contains(columnName)) {
                    // Create a TextField for each column
                    TextField textField = new TextField();
                    textField.setPromptText(columnName); // Set column name as prompt text

                    // Set the position of the TextField (you may need to adjust this based on your layout)
                    textField.setLayoutX(10);
                    textField.setLayoutY(textFieldCount * 30 + 10);

                    // Add the TextField to the view
                    rootAnchorPane.getChildren().add(textField);

                    textFieldCount++;

                    // Add the column name to the set
                    addedColumnNames.add(columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void insertRow(ActionEvent event) {
        // Retrieve the selected table name from the global variable or pass it to this controller
        String selectedTableName = FirstWindowController.selectedTable;

        // Create the SQL INSERT statement
        StringBuilder insertQuery = new StringBuilder("INSERT INTO SYNM");
        insertQuery.append(selectedTableName).append(" (");

        // Gather the column names from TextFields
        for (Node node : rootAnchorPane.getChildren()) {
            if (node instanceof TextField) {
                insertQuery.append(((TextField) node).getPromptText()).append(", ");
            }
        }

        // Remove the trailing comma and space
        if (insertQuery.length() > 2) {
            insertQuery.setLength(insertQuery.length() - 2);
        }

        insertQuery.append(") VALUES (");

        // Gather the values from TextFields
        for (Node node : rootAnchorPane.getChildren()) {
            if (node instanceof TextField) {
                String columnName = ((TextField) node).getPromptText();
                String columnValue = ((TextField) node).getText();

                // Check if the column is of type DATE
                if (isDateColumn(selectedTableName, columnName)) {
                    // Format the date as TO_DATE('user's inputed date', 'format')
                    insertQuery.append("TO_DATE('").append(columnValue).append("', 'yyyy-MM-dd')");
                } else {
                    // Non-date column, treat as string
                    insertQuery.append("'").append(columnValue).append("'");
                }

                insertQuery.append(", ");
            }
        }

        // Remove the trailing comma and space
        if (insertQuery.length() > 2) {
            insertQuery.setLength(insertQuery.length() - 2);
        }

        insertQuery.append(")");

        // Execute the INSERT statement
        executeUpdateQuery(insertQuery.toString());
    }
     private void executeUpdateQuery(String query) {
        // Execute the INSERT or UPDATE statement
        try {
            Statement statement = myconnection.cnx.createStatement();
            int rowsUpdated = statement.executeUpdate(query);

            if (rowsUpdated > 0) {
                System.out.println("Row updated/inserted successfully!");
                // Close the current window/dialog
                Stage stage = (Stage) rootAnchorPane.getScene().getWindow();
                stage.close();

                // Refresh the data in the parent window
                FirstWindowController controller = new FirstWindowController();
                controller.fillData();
            } else {
                System.out.println("No rows were updated/inserted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating/inserting row");
        }
    }

}
