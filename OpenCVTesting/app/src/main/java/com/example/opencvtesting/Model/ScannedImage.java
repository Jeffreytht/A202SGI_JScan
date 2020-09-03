package com.example.opencvtesting.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.core.Point;

public class ScannedImage implements  Parcelable{
    private Bitmap mOriImage;
    private Bitmap mFinalImage;
    private Point[] mContour;
    private int mFilter;

    ScannedImage()
    {

    }

    ScannedImage(Bitmap oriImage, Bitmap finalImage, Point[] contour)
    {
        mOriImage   = oriImage;
        mFinalImage = finalImage;
        mContour    = contour;
        mFilter     = ImageProcessing.COLORFILTER_COLOR;
    }

    public int getFilter() {
        return mFilter;
    }

    public void setFilter(int mFilter) {
        this.mFilter = mFilter;
    }

    protected ScannedImage(Parcel in) {
        mOriImage = in.readParcelable(Bitmap.class.getClassLoader());
        mFinalImage = in.readParcelable(Bitmap.class.getClassLoader());
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
        return mFinalImage;
    }

    public void setFinalImage(Bitmap finalImage) {
        this.mFinalImage = finalImage;
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
        dest.writeParcelable(mFinalImage, flags);
    }
}
