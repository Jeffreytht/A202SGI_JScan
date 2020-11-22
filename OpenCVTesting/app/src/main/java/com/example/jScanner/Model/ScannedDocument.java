package com.example.jScanner.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.opencv.core.Point;

import java.util.LinkedList;

public class ScannedDocument implements Parcelable {
    private LinkedList<ScannedImage> mScannedImageList = new LinkedList<>();
    private String mName;
    private String mId;

    protected ScannedDocument(Parcel in) {
    }

    public ScannedDocument(LinkedList<ScannedImage> scannedImages, String name){
        mScannedImageList = scannedImages;
        mName = name;
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

    public ScannedDocument(String id, String name) {
        this.mId = id;
        this.mName = name;
    }

    public LinkedList<ScannedImage> getScannedImageList() {
        return mScannedImageList;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void addScannedImage(Bitmap oriImage, Point[] contour)
    {
        mScannedImageList.add(new ScannedImage(oriImage,contour));
    }

    public int size(){
        return mScannedImageList.size();
    }

    public boolean isEmpty(){return mScannedImageList.size() == 0;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
