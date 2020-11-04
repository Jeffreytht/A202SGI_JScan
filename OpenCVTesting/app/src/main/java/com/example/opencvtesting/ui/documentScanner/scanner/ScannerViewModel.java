package com.example.opencvtesting.ui.documentScanner.scanner;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import com.example.opencvtesting.Model.ScannedDocument;
import com.example.opencvtesting.ui.documentScanner.image_contour_selector.ImageContourSelectorViewModel;
import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ScannerViewModel extends ViewModel {

    private ScannedDocument mScannedDocument;
    private Mat mRGBA;

    // Buffer
    private Bitmap mReceivedBitmap;

    public ScannerViewModel() {
        mScannedDocument = new ScannedDocument();
        mReceivedBitmap = null;
    }

    ScannedDocument getScannedDocument(){return mScannedDocument;}

    Mat getRGBA(){return mRGBA;}

    void setRGBA(int width, int height)
    {
        this.mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    void setRGBA(Mat rgba, Context context){
        ImageProcessing.rotateImage(rgba, context);
        this.mRGBA = rgba;
    }

    void releaseRGBA(){
        if(mRGBA != null)
            mRGBA.release();
    }

    Bitmap getReceivedBitmap(){return mReceivedBitmap;}

    void setReceivedBitmap(Mat mat, Context context)
    {
        mReceivedBitmap = ImageProcessing.matToBitmap(mat);
    }

    void setBackStackEntry(final NavController navController, LifecycleOwner owner)
    {
        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if(backStack != null)
        {
            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            final Bitmap oriBitmapLiveData     = savedStateHandle.get(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            final Point[] contourLiveData      = savedStateHandle.get(ImageContourSelectorViewModel.TAG_CONTOUR);
            final MutableLiveData<Integer> flagLiveData  = savedStateHandle.getLiveData(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);

            flagLiveData.observe(owner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    if(integer== ImageContourSelectorViewModel.ADD_IMAGE) {
                        mScannedDocument.addScannedImage(oriBitmapLiveData, contourLiveData);
                        clearBuffer(navController);
                    }
                    else if(integer == ImageContourSelectorViewModel.DISCARD_IMAGE)
                    {
                        clearBuffer(navController);
                    }
                }
            });
        }
    }

    void clearBuffer(NavController navController)
    {
        mReceivedBitmap = null;

        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if(backStack != null)
        {
            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_EDITED_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_CONTOUR);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);
        }
    }

}