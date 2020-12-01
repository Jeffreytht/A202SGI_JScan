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

public class ScannedDocument implements Parcelable, Comparable<ScannedDocument> {

    private LinkedList<ScannedImage> mScannedImageList = new LinkedList<>();
    private String mName;
    private String mId;
    private final Uri mCoverUri;
    private long mDate;

    protected ScannedDocument(Parcel in) {
        mName = in.readString();
        mId = in.readString();
        mCoverUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public ScannedDocument() {
        this("", "");
    }

    public ScannedDocument(@NonNull String id, @NonNull String name) {
        this(id, name, new LinkedList<>());
    }

    public ScannedDocument(@NonNull String id, @NonNull String name, @NonNull LinkedList<ScannedImage> scannedImages) {
        this(id, name, scannedImages, null);
    }

    public ScannedDocument(@NonNull String id, @NonNull String name, @NonNull LinkedList<ScannedImage> scannedImages, @Nullable Uri uri) {
        this(id, name, scannedImages, uri, 0);
    }

    public ScannedDocument(@NonNull String id, @NonNull String name, @NonNull LinkedList<ScannedImage> scannedImages, @Nullable Uri uri, long date) {
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

    @NonNull
    public List<ScannedImage> getScannedImageList() {
        return mScannedImageList;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    public void setId(@NonNull String mId) {
        this.mId = mId;
    }

    @NonNull
    public String getName() {
        return (mName.isEmpty()) ? DateFormat.getDateInstance().format(Calendar.getInstance().getTime()) : mName;
    }

    public void setName(@NonNull String name) {
        this.mName = name;
    }

    @Nullable
    public Uri getCoverUri() {
        return this.mCoverUri;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public void addScannedImage(@NonNull Bitmap oriImage, @NonNull Point[] contour) {
        mScannedImageList.add(new ScannedImage(oriImage, contour));
    }

    public void clearScannedImage() {
        mScannedImageList.clear();
    }

    public void addScannedImage(@NonNull ScannedImage scannedImage) {
        mScannedImageList.add(scannedImage);
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

    @Override
    public int compareTo(ScannedDocument o) {
        return (int) (this.mDate - o.mDate);
    }
}
