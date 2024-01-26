/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import static tools.myconnection.queryUserTables;

/**
 *
 * @author Mehdi
 */
public class TableComboBoxManager {

    public static ObservableList<String> getTableNames(String username, String password) {
        List<String> tableNames = new ArrayList<>();

        if ("user1".equals(username)) {
            tableNames = queryUserTables1(username);
        } else if ("user2".equals(username)) {
            tableNames = queryUserTables1("gestp");
        }

        return FXCollections.observableArrayList(tableNames);
    }

    private static List<String> queryUserTables1(String role) {
        List<String> tableNames = new ArrayList<>();

        try {
            ResultSet rs = queryUserTables(role);
            while (rs.next()) {
                tableNames.add(rs.getString("table_name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(TableComboBoxManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tableNames;
    }

    public static void populateComboBox(ComboBox<String> comboBox, String username, String password) {
        ObservableList<String> tableNames = getTableNames(username, password);
        comboBox.setItems(tableNames);
    }
}
