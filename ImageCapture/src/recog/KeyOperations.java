/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recog;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import opencv.OpenCVProcessor;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;
import utilities.FXDIPUtils;
import utilities.ImageIoFX;


/**
 *
 * @author MQ0162246
 * @version 1.00
 * 
 */
//How to create a background image
//BackgroundImage myBI= new BackgroundImage(new Image("vaq.png",32,32,false,true),BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
//BackgroundSize.DEFAULT);
//label1.setBackground(new Background(myBI));
public class KeyOperations extends BorderPane {

    //Frame Grabber from OpenCV
    private TabPane tabsPane;
    //Data Structure used for captured images

    //Captured Images pane
    private final TilePane tilePaneCapturedImages = new TilePane();

    //Live Image View
    private Image     currentimage;
    private ImageView currentimageview;
    Mat currentImageMat;

    // Capture/Cropping
    private final Image processedimage;
    private final ImageView processedimageview;
    
    byte[][] currentImageBytes;
    public KeyOperations(TabPane tabsPane) 
    {
        this.tabsPane = tabsPane;  
        
        // everytime the tab is selected, check the databases for changes        
        tabsPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
                System.out.println("Tab Selection changed to: " + newTab.idProperty().get());
                if ((newTab.idProperty().get() != null) && newTab.idProperty().get().equalsIgnoreCase("Camera Capture")) {
                    // change where the live camera will go
                }
            }
        });        

        this.setPadding(new Insets(10, 10, 10, 10));
        tilePaneCapturedImages.setPrefColumns(6);

        // Live image
        currentimage = new Image("images/defaultLiveImage.jpg");
        currentimageview = new ImageView(currentimage);
        //currentimageview.setFitHeight(400);
        //currentimageview.setFitWidth(400);
        currentimageview.setPreserveRatio(true);

        // Cropped Image && Cropping operations
        processedimage = new Image("images/defaultcaptureImage.jpg");
        processedimageview = new ImageView(processedimage);
        //processedimageview.setFitHeight(400);
        //processedimageview.setFitWidth(400);
        processedimageview.setPreserveRatio(true);

        // Generate GUI
        getGUI();
    }

    /**
     * 
     */
    public void getGUI() {
        createUItopPanel();
        createUIleftPanel();
        createUIcenterPanel();
        createUIbottomPanel();
    }

    private void createUItopPanel() {
        this.setTop(generateTopPanel());
    }

    private void createUIleftPanel() {
        //get current operations
        this.setLeft(getCameraOperations());
    }

    private void createUIcenterPanel() {
        this.setCenter(generateCenterPanel());
    }

    private void createUIbottomPanel() {
        this.setBottom(null);
    }

    private VBox generateCenterPanel() {

 
        Separator separator1 = new Separator();
        separator1.setMinSize(20, 20);
        separator1.setOrientation(Orientation.VERTICAL);
        VBox vb = new VBox();
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(currentimageview, separator1,processedimageview);
        BorderPane.setAlignment(vb, Pos.CENTER);
        return vb;
    }
    private VBox generateTopPanel() {
        //set insets
        this.setPadding(new Insets(20, 10, 10, 10));
        this.setBorder(new Border(new BorderStroke(Color.NAVY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(5))));

        // Top Border
        Label label1 = new Label("Key Operations ");
        Font myFont = Font.font("Verdana", FontPosture.ITALIC, 40);
        label1.setFont(myFont);
        label1.setTextFill(Color.web("#0870a3"));
        label1.setWrapText(true);  
        Image image = new Image("images/vaq.png");
        ImageView iv = new ImageView(image);
        iv.setFitHeight(32);
        iv.setFitWidth(32);
        label1.setGraphic(iv);



        label1.setBorder(new Border(new BorderStroke(Color.NAVY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
        label1.setTextAlignment(TextAlignment.CENTER);
        //BorderPane.setAlignment(label1, Pos.CENTER);
        Separator separator1 = new Separator();
        separator1.setMinSize(20, 20);
        separator1.setOrientation(Orientation.HORIZONTAL);
        VBox vb = new VBox();
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(label1, separator1);
        BorderPane.setAlignment(vb, Pos.CENTER);
        return vb;
    }

    public VBox getCameraOperations() {
        VBox dipOperations = new VBox(10);
        //Load
        Button selectKeyButton = new Button("Load Image...");
        selectKeyButton.setMaxWidth(Double.MAX_VALUE);
        selectKeyButton.setOnAction(new Operation1Handler());

        // Negative if needed
        Button NegateButton = new Button("Negative Image");
        NegateButton.setMaxWidth(Double.MAX_VALUE);
        NegateButton.setOnAction(new Operation2Handler());
        
        // Process 
        Button processKeyButton = new Button("Edge Detection-OpenCV");
        processKeyButton.setMaxWidth(Double.MAX_VALUE);
        processKeyButton.setOnAction(new Operation3Handler());

        // Final layout
        VBox vb = new VBox(10);
        vb.getChildren().addAll(selectKeyButton, NegateButton, processKeyButton);

        dipOperations.getChildren().addAll(vb);
        return dipOperations;
    }
    

       
    /**
     * @return the liveImage
     */
    public Image getQueryImage() {
        return currentimage;
    }

    /**
     * @param queryImage the liveImage to set
     */
    public void setQueryImage(Image queryImage) {
        this.currentimage = queryImage;
        currentimageview.imageProperty().set(queryImage);
    }

    /**
     * @return the liveImageView
     */
    public ImageView getQueryImageView() {
        return currentimageview;
    }

    /**
     * @param queryImageView the liveImageView to set
     */
    public void setQueryImageView(ImageView queryImageView) {
        this.currentimageview = queryImageView;
    }

    class Operation1Handler implements EventHandler<ActionEvent> {

        final FileChooser fileChooser = new FileChooser();
        @Override
        public void handle(ActionEvent event) {
                    //Clear current stuff
                    processedimageview.setImage(null);
                    
                    // load an image....
                    File file = fileChooser.showOpenDialog(null);
                    FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Image files", "*.JPG", "*.PNG");
                    fileChooser.getExtensionFilters().addAll(extFilterJPG);
                    try
                    {
                        BufferedImage bufferedImage  = ImageIO.read(file);
                        BufferedImage bufferedImage2 = ImageIoFX.toGray(bufferedImage);
                        
                        Image image  = SwingFXUtils.toFXImage(bufferedImage2, null);
                        currentimageview.setImage(image);
                        
                        // get bytes from buffered
                        currentImageBytes = ImageIoFX.getGrayByteImageArray2DFromBufferedImage(bufferedImage2);
                        currentImageMat=  FXDIPUtils.byteToGrayMat(currentImageBytes, CV_8UC1); 
                    }
                    catch (Exception e)
                    { }             
        }
    }
    
    class Operation2Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            currentImageMat = OpenCVProcessor.doNegative(currentImageMat);  
            Image resultImg = FXDIPUtils.mat2Image(currentImageMat); 
            currentimageview.setImage(resultImg);
          
        }        
    }
    class Operation3Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            // OpenCV Stuff... Mat in and Mat out
            boolean  drawBoundingRect=true;
            currentImageMat = OpenCVProcessor.doThreshold(currentImageMat,100);
            //currentImageMat = OpenCVProcessor. doFDDescriptorsComplexCoord(currentImageMat); 
            currentImageMat = OpenCVProcessor. doFDDescriptorsComplexDistance(currentImageMat); 
            
            Image resultImg = FXDIPUtils.mat2Image(currentImageMat); 
            processedimageview.setImage(resultImg);
          
        }        
    }
    
//    class Operation2Handler implements EventHandler<ActionEvent> {
//        @Override
//        public void handle(ActionEvent event) {
//            // OpenCV Stuff... Mat in and Mat out
//            Mat negativeMat  = OpenCVProcessor.doNegative(currentImageMat);
//            // Mat result = OpenCVProcessor.doCanny(currentImageMat);
//            int blockSize=7;
//            double C= 10;
//            
//            Mat thresholdedMat = OpenCVProcessor.doThreshold(negativeMat,100);
//            Mat result         = OpenCVProcessor.doMorphGrad(thresholdedMat, 1, OpenCVProcessor.MORPH_GRAD_DOUBLE);
//            
//            //Convert Mat result to Image so it can be displayes in JavaFX
//            Image resultImg = FXDIPUtils.mat2Image(result); 
//            processedimageview.setImage(resultImg);
//        }        
//    }

}

