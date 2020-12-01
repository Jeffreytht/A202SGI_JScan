package com.example.jScanner.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.jScanner.utility.ImageProcessing;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ScannedImage implements Parcelable {

    private Bitmap mOriImage;
    private Point[] mContour;
    private int mFilter;


    public ScannedImage(@Nonnull Bitmap oriImage, @Nonnull Point[] contour) {
        mOriImage = oriImage;
        mContour = contour;
        mFilter = ImageProcessing.COLORFILTER_COLOR;
    }

    protected ScannedImage(Parcel in) {
        mOriImage = in.readParcelable(Bitmap.class.getClassLoader());
        mContour = in.createTypedArray(Point.CREATOR);
        mFilter = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mOriImage, flags);
        dest.writeTypedArray(mContour, flags);
        dest.writeInt(mFilter);
    }

    @Nonnegative
    public int getFilter() {
        return mFilter;
    }

    public void setFilter(@Nonnegative int mFilter) {
        this.mFilter = mFilter;
    }

    @NonNull
    public Bitmap getOriImage() {
        return mOriImage;
    }

    public void setOriImage(@Nonnull Bitmap oriImage) {
        this.mOriImage = oriImage;
    }

    @NonNull
    public Bitmap getFinalImage() {
        return ImageProcessing.colorFiltering(ImageProcessing.warpPerspective(mOriImage, new MatOfPoint2f(mContour)), mFilter);
    }

    @NonNull
    public Point[] getContour() {
        return mContour;
    }

    public void setContour(@Nonnull Point[] mContour) {
        this.mContour = mContour;
    }

    @NonNull
    public Bitmap[] getFilteredBitmaps() {
        int totalFilterType = ImageProcessing.FILTER_TYPE.size();
        Bitmap[] bmp = new Bitmap[totalFilterType];

        Iterator<Map.Entry<Integer, String>> it = ImageProcessing.FILTER_TYPE.entrySet().iterator();

        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<Integer, String> element = it.next();
            bmp[i] = ImageProcessing.colorFiltering(ImageProcessing.warpPerspective(mOriImage, new MatOfPoint2f(mContour)), element.getKey());
        }
        return bmp;
    }
}
