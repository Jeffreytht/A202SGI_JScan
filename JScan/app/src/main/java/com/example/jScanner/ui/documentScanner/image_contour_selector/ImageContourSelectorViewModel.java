package com.example.jScanner.ui.documentScanner.image_contour_selector;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import com.example.jScanner.utility.ImageProcessing;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.List;

public class ImageContourSelectorViewModel extends ViewModel {

    private final MutableLiveData<Bitmap> mBitmap;
    private final MutableLiveData<Point[]> mContour;

    // Tag
    public static final String TAG_ORIGINAL_BITMAP = "#ImageContourSelector.OriBitmap";
    public static final String TAG_EDITED_BITMAP = "#ImageContourSelector.EditedBitmap";
    public static final String TAG_CONTOUR = "#ImageContourSelector.Contour";
    public static final String TAG_FLAG_ADD_TO_DOCUMENT = "#ImageContourSelector.FlagAddToDocument";

    // Flag
    public static final int ADD_IMAGE = 1;
    public static final int DISCARD_IMAGE = 2;

    public ImageContourSelectorViewModel() {
        mBitmap = new MutableLiveData<>();
        mContour = new MutableLiveData<>();
    }

    LiveData<Bitmap> getBitmap() {
        return mBitmap;
    }

    LiveData<Point[]> getContour() {
        return mContour;
    }

    void initViewModel(Bundle bundle)
    {
        ImageContourSelectorFragmentArgs args = ImageContourSelectorFragmentArgs.fromBundle(bundle);
        mBitmap.setValue(args.getOriImage());

        if(args.getContour() == null)
            findContour();
        else
            mContour.setValue(args.getContour());
    }

    private void findContour()
    {
        if(mBitmap.getValue() == null)
            return;

        Mat bitmapMat = ImageProcessing.bitmapToMat(mBitmap.getValue());
        List<MatOfPoint> contours = ImageProcessing.findContour(bitmapMat);

        // Find the biggest contour
        MatOfPoint2f approx2f = ImageProcessing.maxContour(contours, 10000);
        if(approx2f != null) {
            mContour.setValue(approx2f.toArray());
        }
        else
        {
            Point center = new Point(bitmapMat.cols() / 2.0,bitmapMat.rows() / 2.0);
            mContour.setValue(new Point[]{
                    new Point(center.x - center.x / 2, center.y - center.y / 2) // top left
                    ,new Point(center.x + center.x / 2, center.y - center.y / 2) // top right
                    ,new Point(center.x - center.x / 2, center.y + center.y / 2) // bottom left
                    ,new Point(center.x + center.x/2 , center.y + center.y / 2) // bottom right
            });
        }
    }

    public void notifyChangesToParentFragment(NavController navController, int flag)
    {
        NavBackStackEntry backStackEntry = navController.getPreviousBackStackEntry();
        if(backStackEntry != null) {
            SavedStateHandle savedStateHandle = backStackEntry.getSavedStateHandle();
            savedStateHandle.set(TAG_ORIGINAL_BITMAP, mBitmap.getValue());
            savedStateHandle.set(TAG_CONTOUR, mContour.getValue());
            savedStateHandle.set(TAG_FLAG_ADD_TO_DOCUMENT,  flag);
        }
    }

    public void rotateBitmapNContour(int degree)
    {
        Bitmap[] wrapper = {mBitmap.getValue()};
        ImageProcessing.rotateBitmapNContour(wrapper, mContour.getValue(),degree);
        mBitmap.setValue(wrapper[0]);
    }
}