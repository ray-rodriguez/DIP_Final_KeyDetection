/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;
import java.awt.Point;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author MQ0162246
 */
public class ImageCrop {
    
    private RubberBandSelection rubberBandSelection;
    private ScrollPane scrollPane;
    private Group imageLayer;    
    private ImageView imageView;
    private Image image;
    
    private Image  croppedImage=null;
    private Bounds selectionBounds;

    //Images Size Property
    private ObjectProperty<Point> imageSize = new SimpleObjectProperty<Point>(new Point(120,120));        
        
    public ObjectProperty<Point> imageSizeProperty() {
        return imageSize;
    }    
    public ImageCrop(Image image,ImageView imageView, Group imageLayer,ScrollPane scrollPane) 
    {
        this.image = image;
        // the container for the image as a javafx node
        this.imageView = imageView;
        // image layer: a group of images
        this.imageLayer = imageLayer;  
        // rubberband selection
        this.scrollPane=scrollPane;
        
        rubberBandSelection = new RubberBandSelection(imageLayer);  
        
        // create context menu and menu items
        ContextMenu contextMenu = new ContextMenu();
        MenuItem cropMenuItem = new MenuItem("Image Crop", new ImageView(new Image("images/scissors-icon-4.jpg")));
        cropMenuItem.setOnAction(new EventHandler<ActionEvent>() 
        {
            @Override
            public void handle(ActionEvent e) {
                // get bounds for image crop
                selectionBounds = rubberBandSelection.getBounds();
                // show bounds info
                // System.out.println("Selected area: " + selectionBounds);
                
                // crop the image
                int width =  (int) selectionBounds.getWidth();
                int height = (int) selectionBounds.getHeight();
                
                int width1 =  (int) imageSize.get().x;
                int height1 = (int) imageSize.get().y;
                System.out.println("image x cropping size is: "+imageSize.get().x);
                System.out.println("image y cropping size is: "+imageSize.get().y);
                
                System.out.println("cropp image is "+width1);
                System.out.println("cropp image is "+height1);
                SnapshotParameters parameters = new SnapshotParameters();
                parameters.setFill(Color.TRANSPARENT);
                if( !(  (Math.abs(rubberBandSelection.getRect().getX())     <= 0.01) &&
                        (Math.abs(rubberBandSelection.getRect().getY())     <= 0.01) &&
                        (Math.abs(rubberBandSelection.getRect().getWidth()) <= 0.01) &&
                        (Math.abs(rubberBandSelection.getRect().getHeight()) <= 0.01) )
                        && width >= 50 &&  height >= 50)
                {
                    parameters.setViewport(new Rectangle2D(selectionBounds.getMinX(), selectionBounds.getMinY(), width, height));
                    //parameters.setTransform(Transform.scale(0.5* imageView.getFitWidth(), 0.5* imageView.getFitHeight()));
                    WritableImage wi = new WritableImage(width, height);
                    imageView.snapshot(parameters, wi);
                    imageView.imageProperty().set(wi);  
                    //imageSavewithSlectedSize(wi, parameters); 
                }
                
                                
                /*
                System.out.println("orig image is "+image.getWidth());
                System.out.println("orig image is "+image.getHeight());
                System.out.println("cropped image is "+wi.getWidth());
                System.out.println("cropped image is "+wi.getHeight());
                */
                // Remove the rectangle from selection after the crop
                rubberBandSelection.getRect().setX(0);
                rubberBandSelection.getRect().setY(0);
                rubberBandSelection.getRect().setWidth(0);
                rubberBandSelection.getRect().setHeight(0);
                imageLayer.getChildren().remove(rubberBandSelection.getRect());
            }
        });
        
        contextMenu.getItems().add(cropMenuItem);
        // set context menu on image layer
        imageLayer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(imageLayer, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }  
}
