/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencv;

import fourierdescriptors.Complex;
import fourierdescriptors.FFT;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 *
 * @author xwc981
 */
public class OpenCVProcessor {

    public static final int MORPH_GRAD_OUTER = 1;
    public static final int MORPH_GRAD_INNER = 2;
    public static final int MORPH_GRAD_DOUBLE = 3;

    public static Mat doCanny(Mat grayImage) {
        // init
        Mat detectedEdges = new Mat();

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 100, 100 * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        grayImage.copyTo(dest, detectedEdges);

        return dest;
    }

    public static Mat doPyramidDown(Mat source) {
        Mat destination = new Mat(source.rows() / 2, source.cols() / 2, source.type());
        try {
            destination = source;
            Imgproc.pyrDown(source, destination, new Size(source.cols() / 2, source.rows() / 2));
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
        return destination;
    }

    public static Mat doPyramidUp(Mat source) {
        Mat destination = new Mat(source.rows() * 2, source.cols() * 2, source.type());
        try {
            destination = source;
            Imgproc.pyrUp(source, destination, new Size(source.cols() * 2, source.rows() * 2));
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
        return destination;
    }

    public static Mat doMoments(Mat source) {
        Moments mm = Imgproc.moments(source, true);
        mm.get_m00();
        mm.get_m00();
        mm.get_m00();
        mm.get_m00();
        return source;
    }

    public static MatOfPoint doContour(Mat source) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat contourImg = new Mat(source.rows(), source.cols(), CvType.CV_8UC1, new Scalar(0));

        /// New 
        MatOfPoint2f currentContour2f;

        Imgproc.findContours(source, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        System.out.println("Found contours...." + contours.size());

        //From the maximum contour, extract the following:
        double maxArea = 0;
        double maxPeri = 0;
        //
        MatOfPoint maxContour = contours.get(0);
        int maxContourIndex = 0;
        MatOfPoint currentContour;
        RotatedRect rect = new RotatedRect();
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            // System.out.println("current contour size is:" + currentContour.size());

            // Extract features:
            double area = Imgproc.contourArea(currentContour);
            currentContour2f = new MatOfPoint2f(currentContour.toArray());
            double perimeter = Imgproc.arcLength(currentContour2f, true);
            rect = Imgproc.minAreaRect(currentContour2f);

            if (area > maxArea) {
                maxArea = area;
                maxPeri = perimeter;
                maxContour = currentContour;
                maxContourIndex = i;
            }
        }

        System.out.println("Max Area index: " + maxContourIndex);
        System.out.println("Max Area is: " + maxArea);
        System.out.println("Max Perimeter is: " + maxPeri);
        System.out.println("Max contour size is: " + maxContour.size());

        Point[] points = maxContour.toArray();
//            System.out.println("X Values:");
//            for (Point point : points) {
//                // System.out.println("X:  "+ point.x +" Y: "+point.y);  
//                System.out.println(point.x);  
//            }
//            System.out.println("Y Values:");
//            for (Point point : points) {
//                // System.out.println("X:  "+ point.x +" Y: "+point.y);  
//                System.out.println( point.y);  
//            }

        // Draw only the max contour
        return maxContour;
    }

    public static Mat doFDDescriptorsComplexCoord(Mat originalImage) {
        
        // get max contour:
        MatOfPoint matOfPoint = doContour(originalImage);

        //convert to array of Points
        Point[] points = matOfPoint.toArray();

        // start FFT
        Complex[] complextData2 = convertPointArray2ComplexArray(points);

        //Normalized length
        Complex[] complextData = normalizedSizeToBackward(complextData2, 512);
        for (int i = 0; i < 10; i++) {
            System.out.println("original data: " + complextData[i].toString());
        }

        //Extract Average
        float XCentroid = 0;
        float YCentroid = 0;
        for (Complex complextData1 : complextData) {
            XCentroid += complextData1.re();
            YCentroid += complextData1.im();
        }
        XCentroid /= complextData.length;
        YCentroid /= complextData.length;
        for (int i = 0; i < complextData.length; i++) {
            complextData[i] = new Complex(complextData[i].re() - XCentroid, complextData[i].im() - YCentroid);
        }

        Complex[] fftData = doFFT(complextData);

        // Inverse 
        Complex[] complextDataRecovered = doIFFT(fftData);

        //Restore Means
        for (int i = 0; i < complextData.length; i++) {
            complextDataRecovered[i] = new Complex(complextDataRecovered[i].re() + XCentroid, complextDataRecovered[i].im() + YCentroid);
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("original data recovered: " + complextDataRecovered[i].toString());
        }

        Point[] points2 = convertComplexArray2PointArray(complextDataRecovered);
        for (int i = 0; i < 10; i++) {
            System.out.println("original data recovered points: " + points2[i].toString());
        }

        MatOfPoint recoveredContour = convertPointArray2MatOfPoint(points2);

        Mat FDReconstructedImg = drawContourMat(recoveredContour, originalImage);

        return FDReconstructedImg;
    }

    public static Mat doFDDescriptorsComplexDistance(Mat originalImage) {

        // get max contour:
        MatOfPoint matOfPoint = doContour(originalImage);

        //convert to array of Points
        Point[] points = matOfPoint.toArray();

        // start FFT
        Complex[] complextData2 = convertPointArray2ComplexArray(points);

        //Normalized length
        Complex[] complextData = normalizedSizeToBackward(complextData2, 512);
        
        //For radial distance
        Complex[] distanceData = new Complex[complextData.length];
        
        // For angles
        double[] distanceAngle1 = new double[complextData.length];
        double[] distanceAngle2 = new double[complextData.length];
        for (int i = 0; i < 10; i++) {
            System.out.println("Original data: " + complextData[i].toString());
        }

        //Extract Average
        float XCentroid = 0;
        float YCentroid = 0;
        for (Complex complextData1 : complextData) {
            XCentroid += complextData1.re();
            YCentroid += complextData1.im();
        }
        XCentroid /= complextData.length;
        YCentroid /= complextData.length;
       System.out.println("Original data Centroid " + XCentroid + "  "+ YCentroid);

        // find index of max distance
        int maxIndex =0;
        double maxDistance=0;
        for (int i = 0; i < complextData.length; i++) 
        {
            double distance= Math.sqrt( Math.pow((complextData[i].re() - XCentroid),2.00)+
                       Math.pow((complextData[i].im() - YCentroid),2.00));
            distanceData[i] = new Complex(distance,0);
            if(distance > maxDistance)
            {
                 maxIndex =i;
                 maxDistance=distance;
            } 
        }
        
        //Rotate to the max radius
        Complex[] distanceRotationNormalized = new Complex[distanceData.length];
        for (int i = maxIndex; i < maxIndex+ distanceData.length; i++) 
        {
            double distance= Math.sqrt( Math.pow((complextData[i-maxIndex].re() - XCentroid),2.00)+
                       Math.pow((complextData[i-maxIndex].im() - YCentroid),2.00));
            
            distanceRotationNormalized[i- maxIndex] = distanceData[i% distanceData.length];
            distanceAngle1[i- maxIndex] = Math.acos((complextData[i-maxIndex].re() - XCentroid)/distance);
            distanceAngle2[i- maxIndex] = Math.asin((complextData[i-maxIndex].im() - YCentroid)/distance);
        }
        
        //fft  
        Complex[] fftData = doFFT(distanceRotationNormalized);

        // Inverse FFT
        Complex[] complextDataRecovered = doIFFT(fftData);

        //Restore Means & rotation
        for (int i = 0; i < complextDataRecovered.length; i++) 
        {
            double complextDataRecovered_x = Math.abs(complextDataRecovered[i].re())* Math.cos(distanceAngle1[i])+XCentroid;
            double complextDataRecovered_y = Math.abs(complextDataRecovered[i].re())* Math.sin(distanceAngle2[i])+YCentroid;
            complextDataRecovered[i] = new Complex(complextDataRecovered_x,Math.abs(complextDataRecovered_y) );
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("original data recovered: " + complextDataRecovered[i].toString());
        }

        Point[] points2 = convertComplexArray2PointArray(complextDataRecovered);
        for (int i = 0; i < 10; i++) {
            System.out.println("original data recovered points: " + points2[i].toString());
        }

        MatOfPoint recoveredContour = convertPointArray2MatOfPoint(points2);

        Mat FDReconstructedImg = drawContourMat(recoveredContour, originalImage);

        return FDReconstructedImg;
    }

    /*
This method accepts the following parameters −
src − An object of the class Mat representing the source (input) image.
dst − An object of the class Mat representing the destination (output) image.
maxValue − A variable of double type representing the value that is to be given if pixel value is more than the threshold value.
adaptiveMethod − A variable of integer the type representing the adaptive method to be used. This will be either of the following two values
ADAPTIVE_THRESH_MEAN_C − threshold value is the mean of neighborhood area.
ADAPTIVE_THRESH_GAUSSIAN_C − threshold value is the weighted sum of neighborhood values where weights are a Gaussian window.
thresholdType − A variable of integer type representing the type of threshold to be used.
blockSize − A variable of the integer type representing size of the pixelneighborhood used to calculate the threshold value.
C − A variable of double type representing the constant used in the both methods (subtracted from the mean or weighted mean).
     */
    public static Mat doAdaptiveThreshold(Mat src, int blockSize, double C) {
        // Creating an empty matrix to store the result
        Mat dest = new Mat(src.rows(), src.cols(), src.type());

        // ADAPTIVE_THRESH_GAUSSIAN_C
        Imgproc.adaptiveThreshold(src, dest, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, C);
        return dest;
    }

    // basic thresholding
    public static Mat doThreshold(Mat src, double th) {
        // Creating an empty matrix to store the result
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.threshold(src, dest, th, 255, Imgproc.THRESH_BINARY);
        return dest;
    }

    public static Mat doNegative(Mat src) {
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                byte[] data = new byte[1];
                src.get(i, j, data);
                data[0] = (byte) (255 - (data[0] & 0xff));
                dest.put(i, j, data);
            }
        }
        return dest;
    }

    // 
    public static Mat doDilate(Mat src, int dilation_size) {

        Mat dest = src;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
        Imgproc.dilate(src, dest, element);
        return dest;
    }

    public static Mat doErode(Mat src, int erosion_size) {

        Mat dest = src;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
        Imgproc.erode(src, dest, element);
        return dest;
    }

    public static Mat doMorphGrad(Mat src, int size, int type) {
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        Mat d = src.clone();
        Mat e = src.clone();
        Mat dd = doDilate(d, size);
        Mat ee = doErode(e, size);

        switch (type) {
            case 1:
                Core.subtract(dd, src, dest);
                break;
            case 2:
                Core.subtract(src, ee, dest);
                break;
            case 3:
                Core.subtract(dd, ee, dest);
                break;
            default:
                Core.subtract(dd, src, dest);
        }
        return dest;
    }

    public static Complex[] doFFT(Complex[] data) {
        Complex[] fft = FFT.fft(data);
        return fft;
    }

    public static Complex[] doIFFT(Complex[] fftData) {
        Complex[] data = FFT.ifft(fftData);
        return data;
    }

    //
    public static Complex[] convertPointArray2ComplexArray(Point[] points) {
        Complex[] data = new Complex[points.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Complex(points[i].x, points[i].y);
        }
        return data;
    }

    //
    public static Point[] convertComplexArray2PointArray(Complex[] complex) {
        Point[] points = new Point[complex.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point((int) (complex[i].re()+0.5), (int) (complex[i].im()+0.50));
        }
        return points;
    }

    //
    public static MatOfPoint convertPointArray2MatOfPoint(Point[] points) {
        MatOfPoint contour = new MatOfPoint(points);
        return contour;
    }

    public static Mat drawContourMat(MatOfPoint contour, Mat source) {
        Mat contourImg = new Mat(source.rows(), source.cols(), CvType.CV_8UC1, new Scalar(0));

        List<MatOfPoint> maxContourList = new ArrayList<>();
        maxContourList.add(contour);
        Imgproc.drawContours(contourImg, maxContourList, 0, new Scalar(255), 1);  // -1 for all indexes
        return contourImg;
    }

    public static Mat drawContour(MatOfPoint source, boolean drawBoundingRect) {
        Mat contourImg = new Mat(source.rows(), source.cols(), CvType.CV_8UC1, new Scalar(0));
        // Extract features:
        MatOfPoint2f currentContour2f = new MatOfPoint2f(source.toArray());

        RotatedRect rect = new RotatedRect();
        rect = Imgproc.minAreaRect(currentContour2f);

        // Draw only the  contour
        List<MatOfPoint> contourList = new ArrayList<>();
        contourList.add(source);
        Imgproc.drawContours(contourImg, contourList, 0, new Scalar(255), 1);  // -1 for all indexes

        //
        if (drawBoundingRect == true) {
            Point[] vertices = new Point[4];
            rect.points(vertices);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(contourImg, vertices[j], vertices[(j + 1) % 4], new Scalar(128));
            }
        }
        return contourImg;
    }

    public static Complex[] normalizedSizeToForward(Complex[] complextData, int newSize) {
        Complex[] normalizedData = new Complex[newSize];
        float scaleFactor = (float) (newSize - 1) / complextData.length;
        for (int i = 0; i < complextData.length; i++) {
            int j = (int) (i * scaleFactor + 0.50);
            if (j < 0) {
                j = 0;
            }
            if (j > (newSize - 1)) {
                j = newSize - 1;
            }
            normalizedData[j] = complextData[i];
        }

        normalizedData[0] = complextData[0];
        normalizedData[newSize - 1] = complextData[complextData.length - 1];
        for (int i = 1; i < newSize - 1; i++) {
            if (normalizedData[i] == null) {
                normalizedData[i] = normalizedData[i - 1];
            }
        }
        return normalizedData;
    }
    public static Complex[] normalizedSizeToBackward(Complex[] complextData, int newSize) {
        Complex[] normalizedData = new Complex[newSize];
        float scaleFactor = (float) complextData.length /(newSize - 1);  
        for (int i = 0; i <  newSize; i++) 
        {
            int old_i = (int) (i * scaleFactor + 0.50);
            if (old_i < 0) {
                old_i = 0;
            }
            if (old_i > (complextData.length - 1)) {
                old_i = complextData.length - 1;
            }
            normalizedData[i] = complextData[old_i];
        }

        normalizedData[0] = complextData[0];
        normalizedData[newSize - 1] = complextData[complextData.length - 1];
        for (int i = 1; i < newSize - 1; i++) {
            if (normalizedData[i] == null) {
                normalizedData[i] = normalizedData[i - 1];
            }
        }
        return normalizedData;
    }

}
