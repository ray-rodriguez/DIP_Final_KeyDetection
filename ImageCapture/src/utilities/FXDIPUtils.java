/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author MQ0162246
 */
package utilities;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import org.opencv.core.Mat;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public final class FXDIPUtils {

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    
    public static Mat byteToGrayMat(byte[][] byteData2D, int type)
    {
        byte[] byteData1D= new byte[byteData2D.length* byteData2D[0].length];
        
        Mat mat = new Mat(byteData2D.length, byteData2D[0].length, CV_8UC1);
        // 
        for (int i = 0; i < byteData2D.length * byteData2D[0].length; i++) {
            byteData1D[i] = byteData2D[i/byteData2D[0].length][i%byteData2D[0].length];    
        }
        mat.put(0, 0, byteData1D);
        return mat;
    }
    public static byte[] matToByte(Mat mat)
    {
        byte[] byteData = new byte[(int) (mat.total() * mat.channels())];
        mat.get(0, 0, byteData);
        return byteData;
    }
        
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        } catch (Exception e) {
            System.err.println("Cannot convert the Mat obejct: " + e);
            return null;
        }
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property a {@link ObjectProperty}
     * @param value the value to set for the given {@link ObjectProperty}
     * @param <T> This describes my type parameter
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    public static void onFXThread2(BooleanProperty property, boolean value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    /**
     * Support for the {@link mat2image()} method
     *
     * @param original the {@link Mat} object in BGR or grayscale
     * @return the corresponding {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    private static Image mat2Image2(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static void getBufferedImageType(BufferedImage bImage, String from) {

//        TYPE_INT_RGB, TYPE_INT_ARGB, TYPE_INT_ARGB_PRE, TYPE_INT_BGR, 
//        TYPE_3BYTE_BGR, TYPE_4BYTE_ABGR, TYPE_4BYTE_ABGR_PRE, 
//        TYPE_BYTE_GRAY, TYPE_BYTE_BINARY, 
//        TYPE_BYTE_INDEXED, TYPE_USHORT_GRAY, 
//        TYPE_USHORT_565_RGB, 
//        TYPE_USHORT_555_RGB, 
//        TYPE_CUSTOM
        switch (bImage.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                System.out.println(from + " " + "Color TYPE_INT_RGB");
                break;
            case BufferedImage.TYPE_INT_ARGB:
                System.out.println(from + " " + "Color TYPE_INT_ARGB");
                break;
            case BufferedImage.TYPE_INT_ARGB_PRE:
                System.out.println(from + " " + "Color TYPE_INT_ARGB_PRE");
                break;
            case BufferedImage.TYPE_INT_BGR:
                System.out.println(from + " " + "Color TYPE_INT_BGR");
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                System.out.println(from + " " + "Color TYPE_3BYTE_BGR");
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                System.out.println(from + " " + "Color TYPE_4BYTE_ABGR");
                break;
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                System.out.println(from + " " + "Color TYPE_4BYTE_ABGR_PRE");
                break;
            case BufferedImage.TYPE_BYTE_GRAY:
                System.out.println(from + " " + "Gray-Byte TYPE_BYTE_GRAY");
                break;
            case BufferedImage.TYPE_BYTE_BINARY:
                System.out.println(from + " " + "Binary-Byte TYPE_BYTE_BINARY");
                break;
            case BufferedImage.TYPE_USHORT_GRAY:
                System.out.println(from + " " + "Gray-UShort TYPE_USHORT_GRAY");
                break;
            default:
                System.out.println(from + " " + "Other/None!");
                break;
        }
    }
    public static BufferedImage toGray(BufferedImage original) {
        // Second way of converting to Gray
        BufferedImage grayKeyImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayKeyImage.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        return grayKeyImage;
    }    

    public static byte[][] getGrayByteImageArray2DFromBufferedImage(BufferedImage image) {
        byte[] byteData_1d;
        int Rows = image.getHeight();
        int Cols = image.getWidth();
        //getBufferedImageType(image, "getByteImageArray1DFromBufferedImage");
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Type is other than gray 1-byte");
            return null;
        }

        //System.out.println("Row_Siz= " + Rows + " Cols_Size = " + Cols);
        byteData_1d = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        byte[][] byteData_2d = new byte[Rows][Cols];
        for (int i = 0; i < Rows; i++) {
            for (int j = 0; j < Cols; j++) {
                byteData_2d[i][j] = byteData_1d[i * Cols + j];
            }
        }
        //System.out.println("Image Length (1-D Array) = " + byteData_1d.length);
        //System.out.println("Image Row Length (2-D Array) = " + byteData_2d.length);
        //System.out.println("Image Column Length (2-D Array) = " + byteData_2d[0].length);
        return byteData_2d;
    }

    public static BufferedImage setGrayByteImageArray2DToBufferedImage(byte[][] byteData_2d) {
        int width = byteData_2d[0].length;
        int height = byteData_2d.length;
        byte[] byteData_1d = new byte[width * height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byteData_1d[i * width + j] = byteData_2d[i][j];
            }
        }
        int dataType = DataBuffer.TYPE_BYTE;

        DataBufferByte buffer = new DataBufferByte(byteData_1d, byteData_1d.length);

        int cs = ColorSpace.CS_GRAY;
        ColorSpace cSpace = ColorSpace.getInstance(cs);
        ComponentColorModel ccm;
        if (dataType == DataBuffer.TYPE_INT || dataType == DataBuffer.TYPE_BYTE) {
            ccm = new ComponentColorModel(cSpace,
                    ((cs == ColorSpace.CS_GRAY)
                            ? new int[]{8} : new int[]{8, 8, 8}),
                    false, false, Transparency.OPAQUE, dataType);
        } else {
            ccm = new ComponentColorModel(
                    cSpace, false, false, Transparency.OPAQUE, dataType);
        }

        SampleModel sm = ccm.createCompatibleSampleModel(width, height);
        //WritableRaster raster = ccm.createCompatibleWritableRaster(width,height);
        WritableRaster raster = Raster.createWritableRaster(sm, buffer, new Point(0, 0));
        return new BufferedImage(ccm, raster, false, null);
    }

    public static byte[] getGrayByteImageArray1DFromBufferedImage(BufferedImage image) {
        byte[] byteData_1d;
        int Rows = image.getHeight();
        int Cols = image.getWidth();
        //getBufferedImageType(image, "getByteImageArray1DFromBufferedImage");
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Type is other than gray 1-byte");
            return null;
        }
        //System.out.println("Row_Siz= " + Rows + " Cols_Size = " + Cols);
        byteData_1d = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        //System.out.println("Image Length (1-D Array) = " + byteData_1d.length);
        return byteData_1d;
    }
    

    public static BufferedImage setGrayByteImageArray1DToBufferedImage(byte[] byteData_1d, int width, int height) {
        int dataType = DataBuffer.TYPE_BYTE;
        DataBufferByte buffer = new DataBufferByte(byteData_1d, byteData_1d.length);

        int cs = ColorSpace.CS_GRAY;
        ColorSpace cSpace = ColorSpace.getInstance(cs);
        ComponentColorModel ccm = null;
        if (dataType == DataBuffer.TYPE_INT || dataType == DataBuffer.TYPE_BYTE) {
            ccm = new ComponentColorModel(cSpace,
                    ((cs == ColorSpace.CS_GRAY)
                            ? new int[]{8} : new int[]{8, 8, 8}),
                    false, false, Transparency.OPAQUE, dataType);
        } else {
            ccm = new ComponentColorModel(
                    cSpace, false, false, Transparency.OPAQUE, dataType);
        }

        SampleModel sm = ccm.createCompatibleSampleModel(width, height);
        //WritableRaster raster = ccm.createCompatibleWritableRaster(width,height);
        WritableRaster raster = Raster.createWritableRaster(sm, buffer, new Point(0, 0));
        return new BufferedImage(ccm, raster, false, null);
    }
    
    public static double[] getGrayDoubleImageArray1DFromBufferedImage(BufferedImage image) {
        byte[] byteData_1d;
        double[] doubleData_1d;
        int Rows = image.getHeight();
        int Cols = image.getWidth();
        //getBufferedImageType(image, "getByteImageArray1DFromBufferedImage");
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Type is other than gray 1-byte");
            return null;
        }
        //System.out.println("Row_Siz= " + Rows + " Cols_Size = " + Cols);
        byteData_1d = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); 
        doubleData_1d = new double[Rows*Cols];
        for (int i = 0; i < doubleData_1d.length; i++) {
                doubleData_1d[i] = (double) (byteData_1d[i] & 0xff);
            }       
        //System.out.println("Image Length (1-D Array) = " + doubleData_1d.length);
        return doubleData_1d;
    }
    
    public static BufferedImage setGrayDoubleImageArray1DToBufferedImage(double[] doubleData_1d, int width, int height) {
        int dataType = DataBuffer.TYPE_BYTE;
        byte[] byteData_1d;
        
        // Convert the double to byte gray by scaling and clipping
       byteData_1d = scale(doubleData_1d,0,255);
       DataBufferByte buffer = new DataBufferByte(byteData_1d, byteData_1d.length);

        int cs = ColorSpace.CS_GRAY;
        ColorSpace cSpace = ColorSpace.getInstance(cs);
        ComponentColorModel ccm = null;
        if (dataType == DataBuffer.TYPE_INT || dataType == DataBuffer.TYPE_BYTE) {
            ccm = new ComponentColorModel(cSpace,
                    ((cs == ColorSpace.CS_GRAY)
                            ? new int[]{8} : new int[]{8, 8, 8}),
                    false, false, Transparency.OPAQUE, dataType);
        } else {
            ccm = new ComponentColorModel(
                    cSpace, false, false, Transparency.OPAQUE, dataType);
        }

        SampleModel sm = ccm.createCompatibleSampleModel(width, height);
        //WritableRaster raster = ccm.createCompatibleWritableRaster(width,height);
        WritableRaster raster = Raster.createWritableRaster(sm, buffer, new Point(0, 0));
        return new BufferedImage(ccm, raster, false, null);
    }    
    
    // Simple clipping of the a pixel value to the range 0-255
    public static int clip(float xx) {
        int x = Math.round(xx);
        if (x > 255) 
            x = 255;
        if (x < 0)
            x = 0;
        return x;
    }
    
    public static byte[] scale(double[] inputArray,int newMin, int newMax)
    {
        byte[] outputArray = new byte [inputArray.length];
        double oldMin =min(inputArray);
        double oldMax =max(inputArray);
        for (int i=0; i < inputArray.length; i++){
            double newValue = ((inputArray[i]-oldMin)*(newMax-newMin)) / (oldMax-oldMin) + newMin;
            outputArray[i] = (byte) ((int)(newValue + 0.5));
         }
        return outputArray;
    }
    /**
     * 
     * @param array an array of double
     * @return the minimum value of the array
     */
    public  static double min(double[] array){
        double currentMin = array[0];
        for (int i=1; i<array.length; i++){
            if (array[i] < currentMin)
                currentMin = array[i];
        }
        return currentMin;
    }
    /**
     * 
     * @param array an array of double
     * @return the maximum value of the array
     */
    public static double max(double[] array){
        double currentMax = array[0];
        for (int i=1; i<array.length; i++){
            if (array[i] > currentMax)
                currentMax = array[i];
        }
        return currentMax;
    }    
}
