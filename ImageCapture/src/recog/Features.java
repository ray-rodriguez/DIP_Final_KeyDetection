/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recog;

/**
 *
 * @author xwc981
 */
public class Features {
    private double[] feat;
    
    public double[] getFeatures(){
        return feat;
    }
    public void setFeatures(double[] features){
        feat = features;
    }
    
    //Constructor
    public Features(double[] features){
        feat = features;
    }
    
    //returns true if find a match
    public boolean compareFeatures(double[] features, int noToCompare, ArrayList<double[]> myFeatureList){
        boolean match = false;
        
        for (double[] temp : myFeatureList) {
            double difference = compare2Features(features, temp, noToCompare);
            if(difference <= 20){
                match = true;
                break;
            }
        }
        return match;
    }
    
    //gets difference between two feature arrays
    public double compare2Features(double[] features1, double[] features2, int noToCompare)
    {
        //size will come from the slider
        double sum=0;
        for (int i = 0; i < noToCompare; i++)
            sum += ((features1[i] - features2[i]) * (features1[i] - features2[i]));
        
        return (double) Math.sqrt(sum);
    }
}
