package com.example.jScanner.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

public class ImageProcessing {

    public static final int COLORFILTER_ORIGINAL    = 1;
    public static final int COLORFILTER_GRAYSCALE   = 1 << 1;
    public static final int COLORFILTER_COLOR       = 1 << 2;
    public static final HashMap<Integer, String> FILTER_TYPE = new HashMap<Integer, String>(){{
        put(COLORFILTER_ORIGINAL, "Original Color");
        put(COLORFILTER_GRAYSCALE, "Grayscale");
        put(COLORFILTER_COLOR, "Whiteboard");
    }};

    static{
        OpenCVLoader.initDebug();
    }

    public static void rotateImage(Mat src, Context context)
    {
        int offset = 90, rotation = 0;
        final int currScreenRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (currScreenRotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
        }

        int angleToRotate = -rotation + offset;
        switch (angleToRotate)
        {
            case -180:
                Core.rotate(src, src, Core.ROTATE_180);
                break;
            case 90:
                Core.rotate(src,src,Core.ROTATE_90_CLOCKWISE);
                break;
            case -90:
                Core.rotate(src,src,Core.ROTATE_90_COUNTERCLOCKWISE);
                break;
        }
    }

public static List<MatOfPoint> findContour(Mat src)
{
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);

    Imgproc.GaussianBlur(src
            ,src
            ,new Size(5,5)
            ,0);

    Mat temp = new Mat();
    src.convertTo(temp, CvType.CV_64FC3);
    double[] arr = new double[(int) (src.total() * src.channels())];

    temp.get(0,0,arr);
    Arrays.sort(arr);
    double median;
    if (arr.length % 2 == 0)
        median = (arr[arr.length/2] + arr[arr.length/2 - 1])/2;
    else
        median =  arr[arr.length/2];

    double sigma = 0.33;
    int lower = (int)(Math.max(0, (1.0 - sigma) * median));
    int upper = (int)(Math.min(255, (1.0 + sigma) * median));

    Imgproc.Canny(src
            ,src
            ,lower
            ,upper
    );

    Imgproc.dilate(src
            , src
            ,Imgproc.getStructuringElement(Imgproc.MORPH_RECT
            , new Size(5,5))
            ,new Point(-1,-1)
            ,2
    );

    Imgproc.findContours(src
            ,contours
            ,new Mat()
            ,Imgproc.RETR_EXTERNAL
            ,Imgproc.CHAIN_APPROX_SIMPLE);

    return contours;
}

    public static Bitmap colorFiltering(@NonNull Bitmap bmp, int type)
    {
        Mat src = bitmapToMat(bmp);
        colorFiltering(src,type);
        return matToBitmap(src);
    }

    public static void colorFiltering(@NonNull Mat src, int type)
    {
        if(type == COLORFILTER_COLOR)
        {
            List<Mat>listOfMat = new ArrayList<>();
            List<Mat>finalListOfMat = new ArrayList<>();
            Core.split(src,listOfMat);

            final int channel = listOfMat.size() - 1;
            for(int i = 0 ; i < channel; i++)
            {
                filterNoise(listOfMat.get(i));
                finalListOfMat.add(listOfMat.get(i));
            }

            Core.merge(finalListOfMat, src);

        }
        else if (type == COLORFILTER_GRAYSCALE)
        {
            Imgproc.cvtColor(src
                    , src
                    , Imgproc.COLOR_BGR2GRAY);
            filterNoise(src);
        }
    }

    private static void filterNoise(@NonNull Mat src)
    {
        Mat tempImage = new Mat();
        Imgproc.dilate(src
                ,tempImage
                ,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(7,7)));

        Imgproc.medianBlur(tempImage
                ,tempImage
                ,21);

        Core.absdiff(src
                ,tempImage
                ,src);

        Core.bitwise_not(src
                ,src);

        src.copyTo(tempImage);

        Core.normalize(tempImage
                , src
                ,0
                ,255
                ,Core.NORM_MINMAX, CV_8UC1);

        Imgproc.threshold(src
                ,src
                ,230
                ,0
                , Imgproc.THRESH_TRUNC);

        Core.normalize(src
                ,src
                ,0
                ,255
                ,Core.NORM_MINMAX,CV_8UC1);
    }


    public static MatOfPoint2f maxContour(List<MatOfPoint>contours, int minimumArea)
    {
        double maxArea = 0.0;
        MatOfPoint2f biggestApprox = null;

        final int CONTOUR_SIZE = contours.size();
        for (int i = 0 ; i < CONTOUR_SIZE; i++) {
            MatOfPoint c     = contours.get(i);
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());

            double area = Imgproc.contourArea(c);
            double peri = Imgproc.arcLength(c2f,true);

            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx,0.02 * peri,true);

            if(area > minimumArea &&  area > maxArea && approx.toArray().length == 4)
            {
                maxArea = area;
                biggestApprox = approx;
            }
        }

        return biggestApprox;
    }

    public static MatOfPoint2f sortContour(MatOfPoint2f contour)
    {
        Point []points = contour.toArray();
        sortPoint(points);
        return new MatOfPoint2f(points);
    }

    public static void sortPoint(Point[] points)
    {
        for(int i = 1; i < points.length; i++)
        {
            Point temp = points[i];
            int index = i - 1;

            while(index >= 0 &&  points[index].y > temp.y)
            {
                points[index + 1] = points[index];
                index = index - 1;
            }
            points[index + 1] = temp;
        }

        if(points[0].x > points[1].x)
        {
            Point temp = points[0];
            points[0] = points[1];
            points[1] = temp;
        }

        if(points[2].x > points[3].x)
        {
            Point temp = points[2];
            points[2] = points[3];
            points[3] = temp;
        }
    }


    public static Bitmap warpPerspective(Bitmap src, MatOfPoint2f contour)
    {
        Mat mat = bitmapToMat(src);
        warpPerspective(mat,contour);
        return matToBitmap(mat);
    }

    public static void warpPerspective(Mat src, MatOfPoint2f contour)
    {
        contour = sortContour(contour);
        Point[] points = contour.toArray();

        // Find average width and height
        int targetWidth = (int)(Math.sqrt(Math.pow(points[0].x - points[1].x, 2) + Math.pow(points[0].y - points[1].y, 2))
                + Math.sqrt(Math.pow(points[2].x - points[3].x, 2) + Math.pow(points[2].y - points[3].y, 2)))/2;

        int targetHeight = (int)(Math.sqrt(Math.pow(points[0].x - points[2].x, 2) + Math.pow(points[0].y - points[2].y, 2))
                + Math.sqrt(Math.pow(points[1].x - points[3].x, 2) + Math.pow(points[1].y - points[3].y, 2)))/2;

        MatOfPoint2f finalContour = new MatOfPoint2f(
                new Point(0,0)
                ,new Point(targetWidth,0)
                ,new Point(0,targetHeight)
                ,new Point(targetWidth,targetHeight));

        Mat perspectiveTransform =  Imgproc.getPerspectiveTransform(contour,finalContour);
        Imgproc.warpPerspective(src,src,perspectiveTransform,new Size(targetWidth,targetHeight),Imgproc.INTER_CUBIC);
    }

    public static Bitmap matToBitmap(Mat src)
    {
        Bitmap bp = Bitmap.createBitmap(src.cols(),src.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,bp);
        return bp;
    }

    public static Mat bitmapToMat(Bitmap bitmap)
    {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap,mat);
        return mat;
    }


    /*
                                             Y
                                             ^
                             Bitmap          |
                              (0,0)          |
                                  X----------|-----------+
                                  |          |           |
                                  |          | Matrix    |
                                  |          | (0, 0)    |
              --------------------|----------X-----------|-------------------------> X
                                  |          |           |
                                  |          |           |
                                  |          |           |
                                  +----------|-----------|
                                             |
                                             |

          Rotation using matrix will get negative value which make the contour offset.
          To find the offset
          1. Use matrix to rotate temporary point (0,0) in Bitmap
          2. New point - temporary point = offset
          3. As after rotation, the (0,0) point will differ, hence rotation offset need to be added.

             Example:
             A        B                                           D        A
             +--------+                                           +--------+
             |        |          After clockwise rotate 90        |        |
             |        |          --------------------------->     |        |
             +--------+                                           +--------+
             D        C                                           C        B

             Before rotation, A is (0,0). After rotation, D is (0,0). Hence, we need to
             plus rotation offsetY A->D
     */

    public static void rotateBitmapNContour(Bitmap[] bmp, @Nullable Point[] points, int degree)
    {
        // Please note that first bitmap must be original bitmap in order to rotate points
        final int BMP_LENGTH = bmp.length;
        if(degree == 0 || BMP_LENGTH <= 0)
            return;

        // Set up matrix to center of bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(degree,bmp[0].getWidth() / 2.0f,  bmp[0].getHeight() / 2.0f);

        if(points != null) {
            int rotationOffsetX = 0;
            int rotationOffsetY = 0;

            // Calculate rotation offset
            if (degree > 0)
                rotationOffsetX = bmp[0].getHeight();
            else
                rotationOffsetY = bmp[0].getWidth();

            //Calculate (0,0) offset
            float[] offset = {0.0f, 0.0f};
            matrix.mapPoints(offset);

            for (Point p : points) {
                float[] pts = {
                        (float) p.x
                        , (float) p.y
                };

                matrix.mapPoints(pts);

                // Apply offset
                p.set(new double[]{
                        pts[0] - offset[0] + rotationOffsetX
                        , pts[1] - offset[1] + rotationOffsetY
                });
            }
        }

        for(int i = 0 ; i < BMP_LENGTH; i++)
            bmp[i] =  Bitmap.createBitmap(bmp[i]
                    ,0
                    ,0
                    , bmp[i].getWidth()
                    , bmp[i].getHeight()
                    , matrix
                    ,true);
    }
}
