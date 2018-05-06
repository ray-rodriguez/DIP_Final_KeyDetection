/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recog;

import recog.KeyInformation;
import javafx.scene.image.Image;

/**
 *
 * @author xwc981
 */
public class KeyProperties {
    //Add Key properties for display, modification recognition
    KeyInformation keyInformation; // separate the owner frm the key;
    Image  keyImage; // Blob in the database;
    String keyImageName; // Image Name
    Features keyFeatures; //Features for the key; Texfile/Binary File in the DB?  
}
