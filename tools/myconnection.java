/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

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
//    public static void fillTable(String table, TableView tv, int size) {
//        try {
//            data = FXCollections.observableArrayList();
//            int r = -1;
//            ResultSet rst = inst(table);
//            while (rst.next()) {
//                ObservableList row = FXCollections.observableArrayList();
//                for (int i = 1; i <= size; ++i) {
//                    row.add((Object) ("" + rst.getString(i)));
//                }
//                data.add((Object) row);
//            }
//            tv.setItems(data);
//        } catch (SQLException ex) {
//            
//        }
//    }
//     public static void fillculms(TableColumn myclm, final int i) {
//        myclm.setCellValueFactory((Callback) new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
//
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
//                return new SimpleStringProperty(((ObservableList) param.getValue()).get(i).toString());
//            }
//        });
//    }
    
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
 public static void fillculms(TableColumn myclm, final int i) {
        myclm.setCellValueFactory((Callback) new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {

            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                return new SimpleStringProperty(((ObservableList) param.getValue()).get(i).toString());
            }
        });
    }
    public static ResultSet queryTableData(String tableName) {
        
        try {
            PreparedStatement preparedStatement = cnx.prepareStatement("SELECT * FROM SYNM" + tableName);
            rst = preparedStatement.executeQuery();
//            while (rst.next()) {
//                for (int i = 1; i <= rst.getMetaData().getColumnCount(); i++) {
//               System.out.print(rst.getString(i) + "\t");
//               
//                }
////                System.out.println();  // Move to the next line for the next row
//            }
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
