package com.example.opencvtesting.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.opencv.core.Point;

import java.util.LinkedList;

public class ScannedDocument implements Parcelable {
    private LinkedList<ScannedImage> mScannedImageList = new LinkedList<>();

    protected ScannedDocument(Parcel in) {
    }

    public static final Creator<ScannedDocument> CREATOR = new Creator<ScannedDocument>() {
        @Override
        public ScannedDocument createFromParcel(Parcel in) {
            return new ScannedDocument(in);
        }

        @Override
        public ScannedDocument[] newArray(int size) {
            return new ScannedDocument[size];
        }
    };

    public ScannedDocument() {
        this.mScannedImageList = new LinkedList<>();
    }

    public LinkedList<ScannedImage> getScannedImageList() {
        return mScannedImageList;
    }

    public void setScannedImageList(LinkedList<ScannedImage> mScannedImageList) {
        this.mScannedImageList = mScannedImageList;
    }

    public void addScannedImage(Bitmap oriImage, Bitmap finalImage, Point[] contour)
    {
        mScannedImageList.add(new ScannedImage(oriImage,finalImage,contour));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
