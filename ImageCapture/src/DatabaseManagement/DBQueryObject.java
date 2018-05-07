/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseManagement;

/**
 *
 * @author Ray-GRod
 */
public class DBQueryObject {
    private int id;
    private String name;
    private String building;
    private String office;
    private double[] features;
    private byte[] image;
    
    public DBQueryObject(int m_id, String m_name, String m_building, String m_office, double[] m_features, byte[] m_image)
    {
        id = m_id;
        name = m_name;
        building = m_building;
        office = m_office;
        features = m_features;
        image = m_image;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the building
     */
    public String getBuilding() {
        return building;
    }

    /**
     * @return the office
     */
    public String getOffice() {
        return office;
    }

    /**
     * @return the features
     */
    public double[] getFeatures() {
        return features;
    }

    /**
     * @return the image
     */
    public byte[] getImage() {
        return image;
    }
}
