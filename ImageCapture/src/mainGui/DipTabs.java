/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainGui;

import camera.FrameGrabber;
import camera.CameraPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import recog.KeyOperations;

/**
 *
 * @author MQ0162246
 */
public class DipTabs {

    //Tab PAne
    private TabPane tabsPane = new TabPane();
    
    private Image img01 = new Image("images/camera.gif");
    private Image img02 = new Image("images/camera2.png");

    public DipTabs(FrameGrabber frameGrabber)
    {
        // Color
        tabsPane.setStyle("-fx-background-color: cornsilk;");

        // Tab1: Camera Capture
        Tab tab1 = new Tab("Camera Capture");
        tab1.idProperty().set("Camera Capture");        
        tab1.setClosable(false);
        tab1.setGraphic(new ImageView(img01));
        tab1.setStyle("-fx-border-color:navy; -fx-background-color: bisque;");
        CameraPane cameraCapture = new CameraPane(frameGrabber,tabsPane);
        tab1.setContent(cameraCapture);
        
        // Tab2: Key Operations for example
        Tab tab2 = new Tab("DIP Ops");
        tab2.idProperty().set("DIP Ops");        
        tab2.setClosable(false);
        tab2.setGraphic(new ImageView(img02));
        tab2.setStyle("-fx-border-color:navy; -fx-background-color: bisque;");
        KeyOperations keyOperations = new KeyOperations(tabsPane);
        tab2.setContent(keyOperations);
        
        

        tabsPane.getTabs().addAll(tab1,tab2);
    }

    /**
     * @return the tabsPane
     */
    public TabPane getTabsPane() {
        return tabsPane;
    }

    /**
     * @param tabsPane the tabsPane to set
     */
    public void setTabsPane(TabPane tabsPane) {
        this.tabsPane = tabsPane;
    }

}
