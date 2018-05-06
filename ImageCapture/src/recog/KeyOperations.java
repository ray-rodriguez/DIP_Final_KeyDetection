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
import javafx.scene.control.Slider;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import opencv.OpenCVProcessor;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
    private Image originalImage;
    private ImageView originalImageview;
    private BufferedImage originalBufferedimage;
    
    Mat graylImageMat;
    private Image graylImage;
    private ImageView graylImageView = new ImageView();
    
    Mat binarylImageMat;
    private Image binarylImage;
    private ImageView binarylImageView = new ImageView();
    private Slider binarySlider = new Slider(0, 255, 1);
    
    Mat contourImageMat;
    private Image contourImage;
    private ImageView contourImageView = new ImageView();
    private Slider featureSlider = new Slider(0, 512, 1);
    
    private ImageView currentReconstructedimageview = new ImageView();
    private Image currentReconstructedimage = null;
    Mat currentReconstructedMat;
    
    Mat currentImageMat;
    Mat currentContourMat;
    
    FDGraph fdGraph = new FDGraph();

    // Capture/Cropping
    private final Image processedimage = null;
    private final ImageView processedimageview = new ImageView();;
    
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
        originalImage = new Image("images/defaultLiveImage.jpg");
        originalImageview = new ImageView(originalImage);
        //currentimageview.setFitHeight(400);
        //currentimageview.setFitWidth(400);
        originalImageview.setPreserveRatio(true);

//        // Cropped Image && Cropping operations
//        processedimage     = new Image("images/defaultcaptureImage.jpg");
//        processedimageview = new ImageView(processedimage);
//        //processedimageview.setFitHeight(400);
//        //processedimageview.setFitWidth(400);
//        processedimageview.setPreserveRatio(true);

        // Generate GUI
        getGUI();
    }

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
        
        Separator separator2 = new Separator();
        separator2.setMinSize(20, 20);
        separator2.setOrientation(Orientation.VERTICAL);
        
        Separator separator3 = new Separator();
        separator3.setMinSize(20, 20);
        separator3.setOrientation(Orientation.VERTICAL);
        
        Separator separator4 = new Separator();
        separator4.setMinSize(20, 20);
        separator4.setOrientation(Orientation.HORIZONTAL);
        
        Separator separator5 = new Separator();
        separator4.setMinSize(20, 20);
        separator4.setOrientation(Orientation.HORIZONTAL);
        
        VBox vb = new VBox();
        vb.setAlignment(Pos.CENTER);
        HBox hb  = new HBox();
        HBox hb2 = new HBox();
        hb.getChildren().addAll(originalImageview, separator1,graylImageView,separator2,binarylImageView );
        hb2.getChildren().addAll(contourImageView, separator3, currentReconstructedimageview );
        
        vb.getChildren().addAll(hb,separator4, hb2, separator5, fdGraph);
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
        
        //GrayScale
        Button grayKeyButton = new Button("Convert to GrayScale");
        grayKeyButton.setMaxWidth(Double.MAX_VALUE);
        grayKeyButton.setOnAction(new Operation5Handler());

        // Negative if needed
        Button NegateButton = new Button("Negative Image");
        NegateButton.setMaxWidth(Double.MAX_VALUE);
        NegateButton.setOnAction(new Operation2Handler());
        
        // Process
        //Binarize Button
        Button binarizeKeyButton = new Button("Binarize-OpenCV");
        binarizeKeyButton.setMaxWidth(Double.MAX_VALUE);
        binarizeKeyButton.setOnAction(new Operation4Handler());
        binarySlider.setValue(0);
        binarySlider.setShowTickLabels(true);
        binarySlider.setShowTickMarks(true);
        binarySlider.setMajorTickUnit(50);
        binarySlider.setMinorTickCount(10);
        binarySlider.setBlockIncrement(5);
        
        //Edge Detection Button
        Button processKeyButton = new Button("Edge Detection-OpenCV");
        processKeyButton.setMaxWidth(Double.MAX_VALUE);
        processKeyButton.setOnAction(new Operation3Handler());
        
        //Compare Button
        Button compareKeyButton = new Button("Get Features & Compare...");
        compareKeyButton.setMaxWidth(Double.MAX_VALUE);
        compareKeyButton.setOnAction(new Operation6Handler());
        featureSlider.setValue(0);
        featureSlider.setShowTickLabels(true);
        featureSlider.setShowTickMarks(true);
        featureSlider.setMajorTickUnit(128);
        featureSlider.setMinorTickCount(16);
        featureSlider.setBlockIncrement(4);

        // Final layout
        VBox vb = new VBox(10);
        vb.getChildren().addAll(selectKeyButton, grayKeyButton, NegateButton, binarySlider, binarizeKeyButton, processKeyButton, featureSlider, compareKeyButton);

        dipOperations.getChildren().addAll(vb);
        return dipOperations;
    }
       
    /**
     * @return the liveImage
     */
    public Image getQueryImage() {
        return originalImage;
    }

    /**
     * @param queryImage the liveImage to set
     */
    public void setQueryImage(Image queryImage) {
        this.originalImage = queryImage;
        originalImageview.imageProperty().set(queryImage);
    }

    /**
     * @return the liveImageView
     */
    public ImageView getQueryImageView() {
        return originalImageview;
    }

    /**
     * @param queryImageView the liveImageView to set
     */
    public void setQueryImageView(ImageView queryImageView) {
        this.originalImageview = queryImageView;
    }

    /**
     * @return the originalBufferedimage
     */
    public BufferedImage getOriginalBufferedimage() {
        return originalBufferedimage;
    }

    /**
     * @param originalBufferedimage the originalBufferedimage to set
     */
    public void setOriginalBufferedimage(BufferedImage originalBufferedimage) {
        this.originalBufferedimage = originalBufferedimage;
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
                originalBufferedimage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(originalBufferedimage, null);
                originalImageview.setImage(image);
            }
            catch (Exception e)
            { }             
        }
    }
    
    class Operation2Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            graylImageMat =   OpenCVProcessor.doNegative(graylImageMat);  
            Image resultImg = FXDIPUtils.mat2Image(graylImageMat); 
            graylImageView.setImage(resultImg);
        }        
    }
    
    class Operation3Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            MatOfPoint matOfPoint = OpenCVProcessor.doContour(binarylImageMat);
            currentContourMat = OpenCVProcessor.drawContourMat(matOfPoint, binarylImageMat);
            
            // Show contour
            Image resultImg2 = FXDIPUtils.mat2Image(currentContourMat); 
            contourImageView.setImage(resultImg2);
            
            // There is an error here...
            // For fun show the reconstructed based on a subset of the total features
            currentReconstructedMat = OpenCVProcessor.doFDDescriptorsComplexDistanceReconstruction(binarylImageMat,50); 
            Image currentReconstructedimage = FXDIPUtils.mat2Image(currentReconstructedMat); 
            currentReconstructedimageview.setImage(currentReconstructedimage);
            
        }        
    }
    
    class Operation4Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            int value = (int) binarySlider.getValue();
            System.out.println("Binarizing value: " + value);
            
            // OpenCV Stuff... Mat in and Mat out
            binarylImageMat = OpenCVProcessor.doThreshold(graylImageMat,value);
            Image resultImg = FXDIPUtils.mat2Image(binarylImageMat); 
            binarylImageView.setImage(resultImg);
        }        
    }
    
    class Operation5Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            // Convert to Gray
            BufferedImage bufferedImage2 = ImageIoFX.toGray(originalBufferedimage);
            currentImageBytes = ImageIoFX.getGrayByteImageArray2DFromBufferedImage(bufferedImage2);
            graylImageMat =  FXDIPUtils.byteToGrayMat(currentImageBytes, CV_8UC1); 

            Image graylImage = FXDIPUtils.mat2Image(graylImageMat); 
            graylImageView.setImage(graylImage);
        }
    }
    
    class Operation6Handler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event){
            int value = (int) featureSlider.getValue();
            
            // Extract Features
            double[] features = OpenCVProcessor.doFDDescriptorsComplexDistance(binarylImageMat,value);
            
            //Graph the FDs... they are tiny in mgd?! first is always one (you can remove it if you want to).
            fdGraph = fdGraph.graphFD(features, value);
            
            //Compare image with database...
            
        }
    }
}
