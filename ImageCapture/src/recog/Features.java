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
    
    public double compareFeatures(double[] features1, double[] features2, int noToCompare)
    {
        //size will come from the slider
        double sum=0;
        for (int i = 0; i < noToCompare; i++)
            sum += ((features1[i] - features2[i]) * (features1[i] - features2[i]));
        
        return (double) Math.sqrt(sum);
    }
}
