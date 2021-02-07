package net.tiagonunes.yatt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.tiagonunes.yatt.db.DbService;

import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DbService.get().init();

        Parent root = FXMLLoader.load(Main.class.getResource("/fxml/scene.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setTitle("yatt");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DbService.get().shutdown();
    }

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("prodMode")) {
            DbService.get().setIsTest(false);
        }

        launch(args);
    }
}
