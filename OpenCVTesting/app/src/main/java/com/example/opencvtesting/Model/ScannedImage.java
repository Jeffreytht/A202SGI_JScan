package com.example.opencvtesting.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScannedImage implements  Parcelable{

    private static int idCounter = 1;

    private int mId;
    private Bitmap mOriImage;
    private Point[] mContour;
    private int mFilter;

    ScannedImage()
    {
        mId = idCounter ++;
    }

    ScannedImage(Bitmap oriImage, Point[] contour)
    {
        mId         = idCounter++;
        mOriImage   = oriImage;
        mContour    = contour;
        mFilter     = ImageProcessing.COLORFILTER_COLOR;
    }

    public int getId(){return mId;}

    public int getFilter() {
        return mFilter;
    }

    public void setFilter(int mFilter) {
        this.mFilter = mFilter;
    }

    protected ScannedImage(Parcel in) {
        mOriImage = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<ScannedImage> CREATOR = new Creator<ScannedImage>() {
        @Override
        public ScannedImage createFromParcel(Parcel in) {
            return new ScannedImage(in);
        }

        @Override
        public ScannedImage[] newArray(int size) {
            return new ScannedImage[size];
        }
    };

    public Bitmap getOriImage() {
        return mOriImage;
    }

    public void setOriImage(Bitmap oriImage) {
        this.mOriImage = oriImage;
    }

    public Bitmap getFinalImage() {
        return ImageProcessing.colorFiltering(ImageProcessing.warpPerspective(mOriImage,new MatOfPoint2f(mContour)), mFilter);
    }

    public Point[] getContour() {
        return mContour;
    }

    public void setContour(Point[] mContour) {
        this.mContour = mContour;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mOriImage, flags);
    }

    public Bitmap[] getAllFilterBitmap(){
        int totalFilterType = ImageProcessing.FILTER_TYPE.size();
        Bitmap []bmp = new Bitmap[totalFilterType];

        Iterator<Map.Entry<Integer, String>> it = ImageProcessing.FILTER_TYPE.entrySet().iterator();

        for(int i = 0; it.hasNext(); i++){
            Map.Entry<Integer, String> element = (Map.Entry<Integer, String>)it.next();
            bmp[i] = ImageProcessing.colorFiltering(ImageProcessing.warpPerspective(mOriImage, new MatOfPoint2f(mContour)), (int)element.getKey());
        }

        return bmp;
    }
}
