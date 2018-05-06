/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import camera.FrameGrabber;
import mainGui.GUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.WindowEvent;

/**
 *
 * @author MQ0162246
 */
public class DIP_FaceRecog_JavaFXMainApp extends Application {
    @Override
    public void start(Stage primaryStage) { 
        // Initialize the Camera/Opencv Lib
        System.loadLibrary("opencv_java341");
        FrameGrabber frameGrabber = new FrameGrabber();

        // Handle closing the windows
        primaryStage.setOnHiding((WindowEvent event) -> {
            Platform.runLater(() -> {
                System.out.println("Application Closed by click to Close Button(X)");
                frameGrabber.getVideoCapture().release();
                System.exit(0);
            });
        });

        //Get GUI
        GUI gui = new GUI(frameGrabber);

        //Prepare the Scene
        Scene scene = new Scene(gui.getrootNode());
        primaryStage.setScene(scene);

        //Set Windows propertied
        primaryStage.setMaximized(true);
        primaryStage.setTitle("I M A G E  C A P T U R E  W i t h   J a v a  FX");
        primaryStage.getIcons().addAll(new Image("images/vaq.png"));
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
