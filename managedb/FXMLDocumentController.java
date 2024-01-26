/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedb;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import controllers.FirstWindowController;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static tools.myconnection.connecterDB;
import tools.myfunction;


public class FXMLDocumentController implements Initializable {

    private Label label;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private JFXPasswordField txtPsw;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private JFXTextField txtUsername;
    Connection cnx;
    Stage stage = new Stage();
    public static String usn, psw;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO 
        txtUsername.setText("user1");
        txtPsw.setText("pass1");
    }

    @FXML
    private void login(ActionEvent event) {
        // Get user inputs
        usn = txtUsername.getText();
        psw = txtPsw.getText();

        cnx = connecterDB(usn, psw);
        if (cnx != null) {
            System.out.println("i m connected");
            try {
                // Load the main window FXML using the existing loadWindow method
                FirstWindowController firstWindowController = (FirstWindowController) myfunction.loadWindow(
                        getClass().getResource("/views/viewFirstWindow.fxml"),
                        "Main",
                        stage,
                        "yes"
                );

                // Call the setUserText method to set the userText value
                ///firstWindowController.setUserText(usn, psw);

                myfunction.closeStage(rootAnchorPane);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("managedb.FXMLDocumentController.login()");
        }

    }

}
