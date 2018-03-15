package pt.taskforce;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;


public class Main extends Application {
    private FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) throws Exception{

        URL location = getClass().getResource("taskforce.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load(location.openStream());

        primaryStage.setTitle("Taskforce");
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> terminate());

        primaryStage.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Q: if (event.isControlDown()) {terminate();}
            }
        });
    }

    private void terminate(){
        stop();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() {
        ((Controller) fxmlLoader.getController()).stop();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
