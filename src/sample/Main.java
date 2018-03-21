package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int CLIENT_WIDTH = 1280;
    public static final int CLIENT_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        ((Controller)loader.getController()).setStage(primaryStage);

        primaryStage.setTitle("Редактор карт для Pac-Man 2018");
        Scene scene = new Scene(root, CLIENT_WIDTH, CLIENT_HEIGHT);
        scene.getStylesheets().add(this.getClass().getResource("/sample/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
