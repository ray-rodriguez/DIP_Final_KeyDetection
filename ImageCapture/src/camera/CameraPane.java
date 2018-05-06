/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import utilities.FXDIPUtils;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javax.imageio.ImageIO;

/**
 *
 * @author MQ0162246
 * @version 1.00
 * 
 */
// How to create a background image
// BackgroundImage myBI= new BackgroundImage(new Image("vaq.png",32,32,false,true),BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
// BackgroundSize.DEFAULT);
// label1.setBackground(new Background(myBI));
public class CameraPane extends BorderPane {

    //Frame Grabber from OpenCV
    private final FrameGrabber frameGrabber;
    private TabPane tabsPane;
    //Data Structure used for captured images
    private final ObservableList<Image> capturedImages = FXCollections.observableArrayList();

    //Captured Images pane
    private final TilePane tilePaneCapturedImages = new TilePane();
    private ScrollPane spCapturedImages;

    //Live Image View
    private Image liveImage;
    private ImageView liveImageView;

    // Capture/Cropping
    private final Image capturedImage;
    private final ImageView capturedImageView;

    private final Group imageLayer;
    private final ScrollPane cropScrollPane;

    private ImageCrop cropObject;

    //Images Size Property
    ObjectProperty<Point> imageSize = new SimpleObjectProperty<>(new Point(120, 120));

    //Saving ifo
    private final SavedImageInformation savedImageParams = new SavedImageInformation();

    /**
     * 
     * @return ObjectProperty Point an object property of type point
     */
    public ObjectProperty<Point> imageSizeProperty() {
        return imageSize;
    }

    public CameraPane(FrameGrabber frameGrabber,TabPane tabsPane) 
    {
        this.frameGrabber = frameGrabber;
        this.tabsPane = tabsPane;  
        
        // everytime the tab is selected, check the databases for changes        
        tabsPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
                System.out.println("Tab Selection changed to: " + newTab.idProperty().get());
                if ((newTab.idProperty().get() != null) && newTab.idProperty().get().equalsIgnoreCase("Camera Capture")) {
                    // change where the live camera will go
                    setCurrentFrameViewForFrameGrabber();
                }
            }
        });        

        this.setPadding(new Insets(10, 10, 10, 10));
        tilePaneCapturedImages.setPrefColumns(6);

        // Live image
        liveImage = new Image("images/defaultLiveImage.jpg");
        liveImageView = new ImageView();
        liveImageView.setFitHeight(400);
        liveImageView.setFitWidth(400);
        liveImageView.setPreserveRatio(true);

        // Cropped Image && Cropping operations
        capturedImage = new Image("images/defaultcaptureImage.jpg");
        capturedImageView = new ImageView(capturedImage);
        capturedImageView.setFitHeight(400);
        capturedImageView.setFitWidth(400);
        capturedImageView.setPreserveRatio(true);

        imageLayer = new Group();
        cropScrollPane = new ScrollPane();
        // Add image to layer
        imageLayer.getChildren().add(capturedImageView);
        // use scrollpane for image view in case the image is large
        cropScrollPane.setContent(imageLayer);

        // Cropping Class
        // Used for rectangle marking and cropping
        cropObject = new ImageCrop(capturedImage, capturedImageView, imageLayer, cropScrollPane);

        // bind size of cropped image
        this.cropObject.imageSizeProperty().bind(imageSize);

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
        this.setCenter(getCapturedImageOperation());
    }

    private void createUIbottomPanel() {
        this.setBottom(getIOSavingsOperations());
    }

    private VBox generateTopPanel() {
        //set insets
        this.setPadding(new Insets(20, 10, 10, 10));
        this.setBorder(new Border(new BorderStroke(Color.NAVY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(5))));

        // Top Border
        Label label1 = new Label("Camera Capture Operations ");
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
        // Camera live stream
        Button streamImageButton = new Button("Live Stream");
        streamImageButton.setMaxWidth(Double.MAX_VALUE);

        // Show captured image
        Button captureImageButton = new Button("Captur Image");
        captureImageButton.setMaxWidth(Double.MAX_VALUE);

        streamImageButton.setOnAction(new StreamImageHandler());
        captureImageButton.setOnAction(new ShowCapturedImageHandler());

        // color/bw choice 
        GridPane colorBGp = new GridPane();
        colorBGp.setVgap(5);
        colorBGp.setHgap(5);
        colorBGp.setPadding(new Insets(5, 5, 5, 5));
        final ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Color");
        rb1.setUserData("Color");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        //rb.setGraphic(new ImageView(image));
        RadioButton rb2 = new RadioButton("Black/White");
        rb2.setToggleGroup(group);
        rb2.setUserData("Gray");
        rb2.setSelected(false);

        colorBGp.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
        colorBGp.add(new Label("Set Capture Mode:"), 0, 0);
        colorBGp.add(rb1, 0, 1);
        colorBGp.add(rb2, 0, 2);

        // Attach handler
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    if (group.getSelectedToggle().getUserData().toString().equalsIgnoreCase("Color")) {
                        FXDIPUtils.onFXThread2(frameGrabber.isColorProperty(), true);
                    } else {
                        FXDIPUtils.onFXThread2(frameGrabber.isColorProperty(), false);
                    }
                }
            }
        });
        // Add allowable image sizes
        ObservableList<String> imageSizesOptions = FXCollections.observableArrayList(
                "120 x 120",
                "125 x 150",
                "200 x 200");
        GridPane imageSizesGrid = new GridPane();
        colorBGp.setVgap(5);
        colorBGp.setHgap(5);
        colorBGp.setPadding(new Insets(5, 5, 5, 5));
        ToggleGroup imageSizesgroup = new ToggleGroup();
        imageSizesGrid.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
        imageSizesGrid.add(new Label("Set Image Size :"), 0, 0);
        int colorSizeIndex = 1;
        for (String imageSizesOption : imageSizesOptions) {
            RadioButton rb = new RadioButton(imageSizesOption);
            rb.setUserData(imageSizesOption);
            rb.setToggleGroup(imageSizesgroup);
            if (imageSizesOption.equals("120 x 120")) {
                rb.setSelected(true);
            }
            imageSizesGrid.add(rb, 0, colorSizeIndex++);
        }
        // Handle
        imageSizesgroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (imageSizesgroup.getSelectedToggle() != null) {
                    if (imageSizesgroup.getSelectedToggle().getUserData().toString().equalsIgnoreCase("120 x 120")) {
                        System.out.println("120x 120 selected...");
                        imageSize.set(new Point(120, 120));

                    } else if (imageSizesgroup.getSelectedToggle().getUserData().toString().equalsIgnoreCase("125 x 150")) {
                        System.out.println("125x 150 selected...");
                        imageSize.set(new Point(125, 150));

                    } else if (imageSizesgroup.getSelectedToggle().getUserData().toString().equalsIgnoreCase("200 x 200")) {
                        System.out.println("200x 200 selected...");
                        imageSize.set(new Point(200, 200));
                    }
                }

            }
        });

        // Final layout
        VBox vb = new VBox(10);
        vb.getChildren().addAll(streamImageButton, captureImageButton, colorBGp, imageSizesGrid);

        dipOperations.getChildren().addAll(vb);
        return dipOperations;
    }

    private VBox getIOSavingsOperations() {
        //set insets
        this.setPadding(new Insets(20, 10, 10, 10));
        this.setBorder(new Border(new BorderStroke(Color.NAVY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(5))));

        // Bottom Border
        Label label1 = new Label("Image I/O Saving Operations ");
        label1.setFont(Font.font("Verdana", FontPosture.ITALIC, 14));
        Image image = new Image("images/vaq.png");
        ImageView iv = new ImageView(image);
        iv.setFitHeight(32);
        iv.setFitWidth(32);
        label1.setGraphic(iv);
        label1.setTextFill(Color.web("#0870a3"));
        label1.setWrapText(true);

        label1.setBorder(new Border(new BorderStroke(Color.NAVY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
        label1.setTextAlignment(TextAlignment.LEFT);
        Separator separator1 = new Separator();
        separator1.setMinSize(20, 20);
        separator1.setOrientation(Orientation.HORIZONTAL);

        // Save Captured Images
        GridPane savingIOGrid = new GridPane();
        savingIOGrid.setVgap(5);
        savingIOGrid.setHgap(5);
        savingIOGrid.setPadding(new Insets(5, 5, 5, 5));

        Button directoryButton = new Button("Select Training Directory:");
        TextField trainingDirectoryText = new TextField();

        // Set Max and Min Width to PREF_SIZE so that the TextField is always PREF
        trainingDirectoryText.setMinWidth(Region.USE_PREF_SIZE);
        trainingDirectoryText.setMaxWidth(Region.USE_PREF_SIZE);
        trainingDirectoryText.textProperty().addListener((ov, prevText, currText) -> {
            // Do this in a Platform.runLater because of Textfield has no padding at first time and so on
            Platform.runLater(() -> {
                Text text = new Text(currText);
                text.setFont(trainingDirectoryText.getFont()); // Set the same font, so the size is the same
                double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
                        + trainingDirectoryText.getPadding().getLeft() + trainingDirectoryText.getPadding().getRight() // Add the padding of the TextField
                        + 2d; // Add some spacing
                trainingDirectoryText.setPrefWidth(width); // Set the width
                trainingDirectoryText.positionCaret(trainingDirectoryText.getCaretPosition()); // If you remove this line, it flashes a little bit
                savedImageParams.setDirName(currText);
            });
        });
        trainingDirectoryText.setText("keyDatabase");
        trainingDirectoryText.setMaxWidth(Double.MAX_VALUE);
        trainingDirectoryText.setStyle("-fx-text-fill:gray;-fx-font-size: 15px;");
        trainingDirectoryText.setEditable(false);

        // directory Chooser Code
        directoryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory
                        = directoryChooser.showDialog(null);
                try {
                    if (selectedDirectory == null) {
                        trainingDirectoryText.setText("keyDatabase");
                    } else {
                        trainingDirectoryText.setText(selectedDirectory.getCanonicalPath());
                    }
                } catch (Exception e) {
                }
            }
        });

        //
        Label trainingSubjectFileNameLabel = new Label("Subject Filename Base:");
        trainingSubjectFileNameLabel.setStyle("-fx-text-fill:gray;-fx-font-size: 15px;-fx-font-weight: bold;");
        TextField trainingSubjectFileNameText = new TextField();
        trainingSubjectFileNameText.textProperty().addListener((observable, oldValue, newValue)
                -> {
            savedImageParams.setBaseName(newValue);
        });

        Label trainingSubjectStartNumber = new Label("Subject Image base number:");
        trainingSubjectStartNumber.setStyle("-fx-text-fill:red;-fx-font-size: 15px;-fx-font-weight: bold;");
        TextField trainingSubjectStartNumberText = new TextField("0");
        trainingSubjectStartNumberText.textProperty().addListener((observable, oldValue, newValue)
                -> {
            if(!newValue.equals(""))
                savedImageParams.setBaseNumer(Integer.valueOf(newValue));
            else
                savedImageParams.setBaseNumer(0);
        });
        trainingSubjectStartNumberText.setText("1");

        Button saveButton = new Button("Save Image(s):");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        // Saved Images Handler
        saveButton.setOnAction(new SaveImageHandler());

        savingIOGrid.add(directoryButton, 0, 0);
        savingIOGrid.add(trainingDirectoryText, 1, 0, 3, 1);

        savingIOGrid.add(trainingSubjectFileNameLabel, 0, 1);
        savingIOGrid.add(trainingSubjectFileNameText, 1, 1);

        savingIOGrid.add(trainingSubjectStartNumber, 2, 1);
        savingIOGrid.add(trainingSubjectStartNumberText, 3, 1);

        savingIOGrid.add(saveButton, 0, 2);

        VBox vb = new VBox();
        vb.setAlignment(Pos.BOTTOM_LEFT);
        vb.getChildren().addAll(label1, separator1, savingIOGrid);
        BorderPane.setAlignment(vb, Pos.BASELINE_LEFT);
        return vb;
    }
    
    public void setCurrentFrameViewForFrameGrabber()
    {
        frameGrabber.setCurrentFrame(liveImageView);
    }
        
    private VBox getCapturedImageOperation() {
        HBox inputOutputResults = new HBox();
        inputOutputResults.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(1))));
        liveImageView.setImage(liveImage);
        frameGrabber.setCurrentFrame(liveImageView);

        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);

        Separator separator2 = new Separator();
        separator2.setOrientation(Orientation.HORIZONTAL);

        //Crop image add/clear
        Button AddCapturedImageButton = new Button("Add Captured Image");
        AddCapturedImageButton.setMaxWidth(Double.MAX_VALUE);
        AddCapturedImageButton.setOnAction(new AddCapturedImageHandler());

        //Clear Captured Images
        Button clearAllCapturedImagesButton = new Button("Clear All Captured");
        clearAllCapturedImagesButton.setMaxWidth(Double.MAX_VALUE);
        clearAllCapturedImagesButton.setOnAction(new ClaerAllCapturedImagesHandler());

        //Clear Captured Images
        Button clearLastCapturedImageButton = new Button("Clear Last Captured");
        clearLastCapturedImageButton.setMaxWidth(Double.MAX_VALUE);
        clearLastCapturedImageButton.setOnAction(new UndoAddLastCapturedImageHandler());

        String cropText = "You can crop an image by marking a rectangular area with the left mouse, right click context menu and select crop";
        TextArea cropTextArea = new TextArea(cropText);
        cropTextArea.setFont(Font.font("Verdana", FontPosture.ITALIC, 14));
        //cropTextArea.setStyle("-fx-control-inner-background: #0870a3");
        //System.out.println("color is "+ Color.DARKBLUE.toString());
        cropTextArea.setStyle("-fx-control-inner-background: #" + Color.SLATEGRAY.toString().substring(2));
        cropTextArea.setPrefRowCount(5);
        cropTextArea.setPrefColumnCount(12);
        cropTextArea.setWrapText(true);
        cropTextArea.setEditable(false);

        VBox vboxButtons = new VBox();
        vboxButtons.getChildren().addAll(AddCapturedImageButton, clearAllCapturedImagesButton, clearLastCapturedImageButton, cropTextArea);
        inputOutputResults.getChildren().addAll(liveImageView, separator, cropScrollPane, vboxButtons);
        inputOutputResults.setAlignment(Pos.CENTER);

        //Populate Tile of Captured images
        generateTilePaneOfCapturedImages();

        //Captured image scroll pane
        spCapturedImages = new ScrollPane(tilePaneCapturedImages);
        spCapturedImages.setHbarPolicy(ScrollBarPolicy.NEVER);
        spCapturedImages.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        spCapturedImages.setBorder(new Border(new BorderStroke(Color.DARKOLIVEGREEN, BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(1))));

        //Separator
        VBox vboxCenter = new VBox();
        Separator separator3 = new Separator();
        separator3.setOrientation(Orientation.HORIZONTAL);

        vboxCenter.getChildren().addAll(inputOutputResults, separator3, spCapturedImages);
        return vboxCenter;
    }

    /**
     * @return the liveImage
     */
    public Image getQueryImage() {
        return liveImage;
    }

    /**
     * @param queryImage the liveImage to set
     */
    public void setQueryImage(Image queryImage) {
        this.liveImage = queryImage;
        liveImageView.imageProperty().set(queryImage);
    }

    /**
     * @return the liveImageView
     */
    public ImageView getQueryImageView() {
        return liveImageView;
    }

    /**
     * @param queryImageView the liveImageView to set
     */
    public void setQueryImageView(ImageView queryImageView) {
        this.liveImageView = queryImageView;
    }

    class StreamImageHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            frameGrabber.startStopCamera(event);
        }
    }

    class ShowCapturedImageHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            capturedImageView.imageProperty().set(frameGrabber.getCurrentFrame().imageProperty().get());
            //capturedImages.add(frameGrabber.getCurrentFrame().imageProperty().get());       
        }
    }

    class AddCapturedImageHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            //capturedImageView.imageProperty().set(frameGrabber.getCurrentFrame().imageProperty().get());
            //capturedImages.add(frameGrabber.getCurrentFrame().imageProperty().get());
            capturedImages.add(capturedImageView.getImage());

            // Update Captured image list
            tilePaneCapturedImages.getChildren().clear();
            updateCaptureImagesList();
        }
    }

    class CropCapturedImageHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            Image toCropImage = capturedImageView.imageProperty().get();
            if (toCropImage != null && cropObject == null) {
                System.out.println("Image is not null...cropping time");

                //create crop object and set its hadle in the constructor
                cropObject = new ImageCrop(toCropImage, capturedImageView, imageLayer, cropScrollPane);
            }
        }
    }

    public void updateCaptureImagesList() {
        if (capturedImages.isEmpty()) //load default image
        {
            capturedImageView.imageProperty().setValue(new Image("images/defaultcaptureImage.jpg"));
        }
        generateTilePaneOfCapturedImages();
    }

    public void generateTilePaneOfCapturedImages() {
        // Captured image list
        for (int i = 0; i < capturedImages.size(); i++) {
            ImageView currentCapturedImageView = new ImageView();
            currentCapturedImageView.setFitHeight(200);
            currentCapturedImageView.setFitWidth(200);
            currentCapturedImageView.setPreserveRatio(true);
            currentCapturedImageView.imageProperty().set(capturedImages.get(i));
            Separator separator1 = new Separator();
            separator1.setOrientation(Orientation.VERTICAL);
            HBox hb = new HBox();
            hb.getChildren().addAll(currentCapturedImageView, separator1);
            tilePaneCapturedImages.getChildren().addAll(hb);
        }
    }

    public void clearImageCollection(ObservableList<Image> capturedImages) {
        capturedImages.clear();
    }

    public void clearImageCollection(ObservableList<Image> capturedImages, int i) {
        capturedImages.remove(i);
    }

    class ClaerAllCapturedImagesHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            clearImageCollection(capturedImages);
            tilePaneCapturedImages.getChildren().clear();
            if (capturedImages.isEmpty()) //load default image
            {
                capturedImageView.imageProperty().setValue(new Image("images/defaultcaptureImage.jpg"));
            }
        }
    }

    class UndoAddLastCapturedImageHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            // remove last image
            if (capturedImages.size() >= 1) {
                clearImageCollection(capturedImages, capturedImages.size() - 1);
                //update list
                tilePaneCapturedImages.getChildren().clear();
                updateCaptureImagesList();
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {

        public SaveImageHandler() {
        }

        @Override
        public void handle(ActionEvent event) {

            int base = savedImageParams.getBaseNumer();
            String baseName = savedImageParams.getBaseName();
            String databaseDirectory = savedImageParams.getDirName();
            File databaseDirectoryFile = new File(databaseDirectory);
            databaseDirectoryFile.mkdir();
            if(!databaseDirectoryFile.exists() && databaseDirectory.equals("keyDatabase"))
            {
                try {
                    databaseDirectory = new File("./keyDatabase").getCanonicalPath();
                } catch (IOException ex) {
                    Logger.getLogger(CameraPane.class.getName()).log(Level.SEVERE, null, ex);
                }

            }


            // first, create subject directory
            String subjectDir = databaseDirectory + File.separator + baseName;
            File subjectDirFile = new File(subjectDir);
            subjectDirFile.mkdir();
            subjectDirFile.setExecutable(true);
            subjectDirFile.setReadable(true);
            subjectDirFile.setWritable(true);

            for (Image capturedImage1 : capturedImages) {
                String fname = databaseDirectory + File.separator  + baseName + File.separator  + baseName + Integer.toString(base++);
                System.out.println("fname to save is: " + fname);
                imageSavewithSlectedSize(capturedImage1, fname);
            }
        }
    }

    public void imageSavewithSlectedSize(Image croppedUnscaledImage, String fname) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, imageSize.get().x, imageSize.get().y));

        ImageView savedImageView = new ImageView(croppedUnscaledImage);

        savedImageView.fitWidthProperty().set(imageSize.get().x);
        savedImageView.fitHeightProperty().set(imageSize.get().y);
        WritableImage croppedScaledImage = new WritableImage(imageSize.get().x, imageSize.get().y);

        savedImageView.snapshot(parameters, croppedScaledImage);
        savedImageView.imageProperty().set(croppedScaledImage);

        Image croppedsizedimage = savedImageView.imageProperty().get();
        BufferedImage bimage = SwingFXUtils.fromFXImage(croppedsizedimage, null);
        BufferedImage imageRGB = new BufferedImage(bimage.getWidth(), bimage.getHeight(), BufferedImage.OPAQUE);
        Graphics2D graphics = imageRGB.createGraphics();
        graphics.drawImage(bimage, 0, 0, null);
        try {
            File outputfile = new File(fname + ".jpg");
            System.out.println("Canonical file name to save is: "+outputfile.getCanonicalPath());
            ImageIO.write(imageRGB, "jpg", outputfile);
        } catch (IOException ee) {
            System.out.println("Could not open the file for writing");
            System.exit(-1);
        }
    }

    public class SavedImageInformation {

        private String baseName;
        private String dirName;
        private int baseNumer;

        /**
         * @return the baseName
         */
        public String getBaseName() {
            return baseName;
        }

        /**
         * @param baseName the baseName to set
         */
        public void setBaseName(String baseName) {
            this.baseName = baseName;
        }

        /**
         * @return the dirName
         */
        public String getDirName() {
            return dirName;
        }

        /**
         * @param dirName the dirName to set
         */
        public void setDirName(String dirName) {
            this.dirName = dirName;
        }

        /**
         * @return the baseNumer
         */
        public int getBaseNumer() {
            return baseNumer;
        }

        /**
         * @param baseNumer the baseNumer to set
         */
        public void setBaseNumer(int baseNumer) {
            this.baseNumer = baseNumer;
        }

    }
}

