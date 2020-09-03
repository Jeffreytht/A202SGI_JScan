package com.example.opencvtesting.ui.documentScanner.document_reader;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import com.example.opencvtesting.Model.ScannedDocument;
import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.ui.documentScanner.image_contour_selector.ImageContourSelectorViewModel;
import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.core.Point;

public class DocumentReaderViewModel extends ViewModel {

    private MutableLiveData<ScannedDocument> mScannedDocument;

    //Buffer
    private int mViewPagerCurrentIndex;

    public DocumentReaderViewModel() {
        this.mScannedDocument = new MutableLiveData<>();
        mViewPagerCurrentIndex = 0;
    }

    public void initViewModel(Bundle bundle)
    {
        DocumentReaderFragmentArgs args = DocumentReaderFragmentArgs.fromBundle(bundle);
        mScannedDocument.setValue(args.getScannedDocument());
    }

    public int getViewPagerCurrentIndex(){
        return  mViewPagerCurrentIndex;
    }

    LiveData<ScannedDocument> getScannedDocument()
    {
        return mScannedDocument;
    }

    public void rotateBitmapNContour(int index, int degree)
    {
        if(mScannedDocument.getValue() == null)
            return;

        ScannedImage scannedImage =  mScannedDocument.getValue().getScannedImageList().get(index);

        Bitmap []bmpArr = {scannedImage.getOriImage(), scannedImage.getFinalImage()};
        ImageProcessing.rotateBitmapNContour(bmpArr,scannedImage.getContour(),degree);
        scannedImage.setOriImage(bmpArr[0]);
        scannedImage.setFinalImage(bmpArr[1]);
    }

    void setViewPagerCurrentIndex(int viewPagerCurrentIndex)
    {
        mViewPagerCurrentIndex = viewPagerCurrentIndex;
    }

    ScannedImage getCurrentSelectedImage()
    {
        if( mScannedDocument.getValue() != null)
            return mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
        return null;
    }

    void setBackStackEntry(final NavController navController, LifecycleOwner owner) {
        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if (backStack != null) {
            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();

            final Bitmap oriBitmapLiveData              = savedStateHandle.get(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            final Bitmap editedBitmapLiveData           = savedStateHandle.get(ImageContourSelectorViewModel.TAG_EDITED_BITMAP);
            final Point[] contourLiveData               = savedStateHandle.get(ImageContourSelectorViewModel.TAG_CONTOUR);
            final MutableLiveData<Integer> flagLiveData = savedStateHandle.getLiveData(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);

            flagLiveData.observe(owner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    if (mScannedDocument.getValue() != null) {
                        if (integer == ImageContourSelectorViewModel.ADD_IMAGE) {
                            ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
                            scannedImage.setOriImage(oriBitmapLiveData);
                            scannedImage.setFinalImage(editedBitmapLiveData);
                            scannedImage.setContour(contourLiveData);
                            clearBuffer(navController);
                        } else if (integer == ImageContourSelectorViewModel.DISCARD_IMAGE) {
                            clearBuffer(navController);
                        }
                    }
                }
            });
        }
    }

    void clearBuffer(NavController navController) {
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