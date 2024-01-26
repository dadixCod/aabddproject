
package tools;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class myfunction {
        public static Object loadWindow(URL loc, String title, Stage parentStage, String test) {
        Object controller = null;
        try {
            FXMLLoader loader = new FXMLLoader(loc);
            Parent parent = (Parent) loader.load();
            controller = loader.getController();
            Stage stage = null;
            stage = parentStage != null ? parentStage : new Stage(StageStyle.DECORATED);
            stage.setTitle(title);
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            //System.out.println("Stylesheets: " + (Object) scene.getStylesheets());
            stage.show();
           // myfunction.setStageIcon(stage);
            if ("yes".equals(test)) {
                stage.setMaximized(true);
            } else {
                stage.setMaximized(false);
            }
        } catch (IOException ex) {
            Logger.getLogger(myfunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return controller;
    }

        public static void loadfadestage(int time, final AnchorPane ap, final String URI2, final String title, final String test) {
        FadeTransition ft = new FadeTransition();
        ft.setDuration(Duration.millis((double) time));
        ft.setNode((Node) ap);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished((EventHandler) new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                myfunction.loadWindow(this.getClass().getResource(URI2), title, null, test);
                myfunction.closeStage(ap);
            }
        });
        ft.play();
    }
         @FXML
    public static void closeStage(AnchorPane btn) {
        ((Stage) btn.getScene().getWindow()).close();
    }

    
}
