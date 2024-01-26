/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import controllers.FirstWindowController;
import static controllers.FirstWindowController.selectedTable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javafx.util.Callback;
import static managedb.FXMLDocumentController.psw;
import static managedb.FXMLDocumentController.usn;

public class myconnection {

    public static Connection cnx;
    private static Statement st;
    public static ResultSet rst;
    private static ObservableList<Object> data ;

    public static ResultSet instload2things(String nom, String password) {
        try {
            if (cnx == null) {
                cnx = connecterDB(nom, password);
            }
            st = cnx.createStatement();
            // Use PreparedStatement to prevent SQL injection
            String myQuery = "select * from your_table where column_name1 = ? and column_name2 = ?";
            try (PreparedStatement preparedStatement = cnx.prepareStatement(myQuery)) {
                preparedStatement.setString(1, nom);
                preparedStatement.setString(2, password);
                rst = preparedStatement.executeQuery();
            }
        } catch (SQLException ex) {
            Logger.getLogger(myconnection.class.getName()).log(Level.SEVERE, "Error executing query", ex);
        }
        return rst;
    }

    public static ResultSet queryUserTables(String role) {
        try {
            if (cnx == null) {
                cnx = connecterDB(usn, psw);
            }
            PreparedStatement preparedStatement = cnx.prepareStatement("SELECT DISTINCT ATP.* FROM all_tab_privs ATP JOIN all_tables AT ON ATP.table_name = AT.table_name JOIN all_objects AO ON ATP.table_name = AO.object_name WHERE ATP.grantee = '" + role.toUpperCase() + "' AND ATP.privilege = 'SELECT'  AND AO.object_type = 'TABLE'"
            );
            rst = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(myconnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rst;
    }

    public static Connection connecterDB(String username, String password) {
        try {
            // Step 1: Load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Step 2: Create the connection object
            cnx = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", username, password);
            System.out.println("Connection established successfully");

        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(myconnection.class.getName()).log(Level.SEVERE, "Error connecting to the database", e);
        }
        return cnx;
    }
    public static ResultSet inst(String table) {
        try {
            if (cnx == null) {
                cnx = connecterDB(usn,psw);
            }
            st = cnx.createStatement();
            rst = st.executeQuery("SELECT * FROM " + table);
        } catch (SQLException sQLException) {
            // empty catch block
            

        }
        return rst;
    }

    public static ResultSet queryTableData(String tableName) {
        
        try {
            PreparedStatement preparedStatement = cnx.prepareStatement("SELECT * FROM SYNM" + tableName);
            rst = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return rst;
    }
    public static ResultSet queryTableMetadata(String tableName) {
        ResultSet resultSet = null;
        try {
            // Use the metadata query to get information about table columns
            String metadataQuery = "SELECT COLUMN_NAME FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ?";
            PreparedStatement preparedStatement = cnx.prepareStatement(metadataQuery);
            preparedStatement.setString(1, tableName.toUpperCase());
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return resultSet;
    }
    public static boolean checkInsertPrivilege(String tableName) {
    try {
        if (cnx == null) {
            cnx = connecterDB(usn, psw);
        }
        String query ="";
        if(usn.equals("user1")){
             query = "SELECT COUNT(*) FROM USER_TAB_PRIVS WHERE TABLE_NAME = '"+tableName+"' AND PRIVILEGE = 'INSERT'";
        }else if(usn.equals("user2")){
             query = "SELECT COUNT(*) FROM ROLE_TAB_PRIVS WHERE TABLE_NAME = '"+tableName+"' AND PRIVILEGE = 'INSERT'";
        }
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return false;
}
     public static void openUpdateDialog(Map<String, String> rowData) {
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
            performUpdateOperation(updatedData, rowData);
        });
    }

    public static TextField[] createTextFields(Map<String, String> rowData) {
        TextField[] textFields = new TextField[rowData.size()];
        int i = 0;

        for (Map.Entry<String, String> entry : rowData.entrySet()) {
            TextField textField = new TextField(entry.getValue());
            textFields[i] = textField;
            i++;
        }

        return textFields;
    }

    public static void promptDeleteConfirmation(Map<String, String> rowData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this row?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteOperation(rowData);
        }
    }

    public static String getPrimaryKeyColumn(String tableName) {
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

    public static void performUpdateOperation(Map<String, String> updatedData, Map<String, String> rowData) {
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

    public static void performDeleteOperation(Map<String, String> rowData) {
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


    public static void closeResources() {
        try {
            if (rst != null) {
                rst.close();
            }
            if (st != null) {
                st.close();
            }
            if (cnx != null) {
                cnx.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(myconnection.class.getName()).log(Level.SEVERE, "Error closing resources", ex);
        }
    }
}
