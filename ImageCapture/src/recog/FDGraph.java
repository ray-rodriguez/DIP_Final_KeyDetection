/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recog;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

/**
 *
 * @author xwc981
 */
public class FDGraph extends VBox{
    
    double[] features;
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    final LineChart<Number,Number> lineChart =  new LineChart<>(xAxis,yAxis);
    XYChart.Series series = new XYChart.Series();

    public FDGraph() 
    {
        lineChart.setTitle("Fourier Descriptors");
        xAxis.setLabel("Number");       
        yAxis.setLabel("Value"); 
    } 
    
    public FDGraph graphFD(double[] features, int noToGraph) 
    {
        this.features = features;
        this.getChildren().clear();
        lineChart.getData().clear();
        series.getData().clear();
        noToGraph = Math.min(features.length, noToGraph);
        for (int i = 0; i < noToGraph; i++) 
        {
            series.getData().add(new XYChart.Data(i,features[i]));
            //System.out.println(" FD is: " + features[i]);
        } 
        lineChart.getData().add(series);   
        this.getChildren().add(lineChart);
        return this;
    }   
}
