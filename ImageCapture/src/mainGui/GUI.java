/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainGui;

import camera.FrameGrabber;
import javafx.scene.Parent;

/**
 *
 * @author MQ0162246
 */
public class GUI {
    
    // Gui elements...
    DipTabs   dipTabs;

    public GUI(FrameGrabber frameGrabber) 
    {
        dipTabs   = new DipTabs(frameGrabber);
    }
    

    public Parent getrootNode()
    { 
       return dipTabs.getTabsPane();
    }
}
