/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import utilities.FXDIPUtils;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.opencv.videoio.VideoCapture;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.opencv.core.Mat;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author MQ0162246
 */
public class FrameGrabber {

    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that realizes the video capture
    private final VideoCapture capture = new VideoCapture();

    // a flag to change the button behavior
    private boolean cameraActive = false;

    // the id of the camera to be used
    private static final int CAMERA_ID = 0;

    //Color or BW capture
    //private boolean isColor = true;
    private final BooleanProperty isColor = new SimpleBooleanProperty(true);

    // Define a getter for the property's value
    public final boolean getIsColor() {
        return isColor.get();
    }

    // Define a setter for the property's value
    public final void setIsColor(boolean value) {
        isColor.set(value);
    }

    // Define a getter for the property itself
    public BooleanProperty isColorProperty() {
        return isColor;
    }
    
    // get how many camera counts!
    public int cameraCount() 
    {
    int device_counts = 0;  
        while (true) 
        {
            if (capture.open(device_counts)) {
                device_counts++;
            } else {
                break;
            }
        }
            return device_counts;
    }

    public void startStopCamera(ActionEvent event) {
        if (!this.cameraActive) {
            
            // start the video capture
            this.capture.open(CAMERA_ID);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        // convert and show the frame
                        Image imageToShow = FXDIPUtils.mat2Image(frame);
                        updateImageView(getCurrentFrame(), imageToShow);
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                if (event.getSource() != null) {
                    ((Button) event.getSource()).setText("Stop Live Camera");
                }
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
                System.err.println("Check your camera or I am out...");

                //Show Dialog
                HBox hb = new HBox();
                Label lb = new Label("Hardware Check-up is being conducted...");
                hb.getChildren().addAll(lb);

                Dialog<Label> dialog = new Dialog<>();
                dialog.setTitle("Check Camera Presence");
                dialog.setHeaderText("Make Sure Camera is Connected \n"
                        + "press Okay (or click title bar 'X' for cancel/exit).");
                dialog.setResizable(true);
                dialog.getDialogPane().setContent(hb);

                ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                ButtonType buttonTypecancel = new ButtonType("Cancel/Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(buttonTypecancel, buttonTypeOk);

                dialog.setResultConverter((ButtonType b) -> {
                    if (b == buttonTypecancel) {
                        //return  new Label("Exiting the System...");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Exiting the System with no camera found...");
                        alert.showAndWait();
                        Platform.exit();
                    } else if (b == buttonTypeOk) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("I will check for the camera again wheny you click start Capture Again...");
                        alert.showAndWait();
                        return new Label("Good to go..");
                    }
                    return new Label("Done...");
                });
                dialog.showAndWait();
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            this.stopAcquisition();
            // update again the button content
            if (event.getSource() != null) {
                ((Button) event.getSource()).setText("Start Live Stream");
            }
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                //if the frame is not empty, process it
                if (!frame.empty()) {
                    if (getIsColor() == false) {
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    }
                }
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        FXDIPUtils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }

    /**
     * @return the currentFrame
     */
    public ImageView getCurrentFrame() {
        return currentFrame;
    }

    /**
     * @param currentFrame the currentFrame to set
     */
    public void setCurrentFrame(ImageView currentFrame) {
        this.currentFrame = currentFrame;
    }

    public VideoCapture getVideoCapture() {
        return capture;
    }
}
