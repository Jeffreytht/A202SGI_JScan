package com.example.jScanner.Model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.core.Point;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class ScannedDocument implements Parcelable {

    private LinkedList<ScannedImage> mScannedImageList = new LinkedList<>();
    private String mName;
    private String mId;
    private Uri mCoverUri;
    private long mDate;

    protected ScannedDocument(Parcel in) {
        mName = in.readString();
        mId = in.readString();
        mCoverUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public ScannedDocument() {
        this("", "");
    }

    public ScannedDocument(String id, String name) {
        this(id, name, new LinkedList<>());
    }

    public ScannedDocument(String id, String name, LinkedList<ScannedImage> scannedImages) {
        this(id, name, scannedImages, null);
    }

    public ScannedDocument(String id, String name, LinkedList<ScannedImage> scannedImages, Uri uri) {
        this(id, name, scannedImages, uri, 0);
    }

    public ScannedDocument(@NonNull String id, @NonNull String name, @NonNull LinkedList<ScannedImage> scannedImages, @Nullable Uri uri, long date){
        this.mId = id;
        this.mName = name;
        this.mScannedImageList = scannedImages;
        this.mCoverUri = uri;
        this.mDate = date;
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

    public List<ScannedImage> getScannedImageList() {
        return mScannedImageList;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return (mName == null || mName.isEmpty()) ?  DateFormat.getDateInstance().format(Calendar.getInstance().getTime()) : mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Uri getCoverUri() {
        return this.mCoverUri;
    }

    public void setCoverUri(@Nonnull Uri uri){
        this.mCoverUri = uri;
    }

    public long getDate(){
        return mDate;
    }

    public void setDate(long date){
        mDate = date;
    }

    public void addScannedImage(Bitmap oriImage, Point[] contour) {
        mScannedImageList.add(new ScannedImage(oriImage, contour));
    }

    public int getTotalPages() {
        return mScannedImageList.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mId);
        dest.writeParcelable(mCoverUri, flags);
    }
}
