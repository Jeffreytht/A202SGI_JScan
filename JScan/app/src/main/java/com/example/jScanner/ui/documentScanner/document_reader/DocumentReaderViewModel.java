package com.example.jScanner.ui.documentScanner.document_reader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.ui.documentScanner.image_contour_selector.ImageContourSelectorViewModel;
import com.example.jScanner.utility.ImageProcessing;

import org.opencv.core.Point;

import java.util.HashMap;
import java.util.Objects;

public class DocumentReaderViewModel extends ViewModel {

    private final MutableLiveData<ScannedDocument> mScannedDocument;
    private final HashMap<ScannedImage, Bitmap[]> mScannedImageBuffer = new HashMap<>();
    private ImagePreComputation mImagePreComputationAsync;

    //Buffer
    private int mViewPagerCurrentIndex;

    public DocumentReaderViewModel() {
        this.mScannedDocument = new MutableLiveData<>(new ScannedDocument());
        mViewPagerCurrentIndex = 0;
    }

    public void initViewModel(Bundle bundle, ProgressDialogListener callback) {
        DocumentReaderFragmentArgs args = DocumentReaderFragmentArgs.fromBundle(bundle);
        mScannedDocument.setValue(args.getScannedDocument());
        mImagePreComputationAsync = new ImagePreComputation(args.getScannedDocument().getScannedImageList(), callback, mScannedImageBuffer);
        mImagePreComputationAsync.execute();
    }

    public int getViewPagerCurrentIndex() {
        return mViewPagerCurrentIndex;
    }

    LiveData<ScannedDocument> getScannedDocument() {
        return mScannedDocument;
    }

    public void setDocumentName(String documentName) {
        Objects.requireNonNull(mScannedDocument.getValue()).setName(documentName);
    }

    void setViewPagerCurrentIndex(int viewPagerCurrentIndex) {
        mViewPagerCurrentIndex = viewPagerCurrentIndex;
    }

    public ScannedImage getCurrentSelectedImage() {
        return Objects.requireNonNull(mScannedDocument.getValue()).getScannedImageList().get(mViewPagerCurrentIndex);
    }

    public int getNumOfScannedImage() {
        return (mScannedDocument.getValue() != null) ? mScannedDocument.getValue().getTotalPages() : 0;
    }

    public String getDocumentName() {
        return Objects.requireNonNull(mScannedDocument.getValue()).getName();
    }

    public boolean isDocumentNameSet() {
        String name = Objects.requireNonNull(mScannedDocument.getValue()).getName();
        return name != null && !name.isEmpty();
    }

    public boolean isNewDocument(){
        return mScannedDocument.getValue().getId() == null || mScannedDocument.getValue().getId().isEmpty();
    }

    public void rotateBitmapNContour(int index, int degree) {
        if (mScannedDocument.getValue() == null)
            return;

        ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(index);
        Bitmap[] bufferImage = Objects.requireNonNull(mScannedImageBuffer.get(scannedImage));
        Bitmap[] bmpArr = {scannedImage.getOriImage(), bufferImage[0], bufferImage[1], bufferImage[2]};
        ImageProcessing.rotateBitmapNContour(bmpArr, scannedImage.getContour(), degree);
        mScannedImageBuffer.put(scannedImage, new Bitmap[]{bmpArr[1], bmpArr[2], bmpArr[3]});
        scannedImage.setOriImage(bmpArr[0]);
    }

    public void stopAsync() {
        if (mImagePreComputationAsync.getStatus() == AsyncTask.Status.RUNNING)
            mImagePreComputationAsync.cancel(true);
    }

    Bitmap[] getCurrentSelectedFilteredImage() {
        if (mScannedDocument.getValue() == null) return null;
        ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(getViewPagerCurrentIndex());
        if (mScannedImageBuffer.containsKey(scannedImage))
            return mScannedImageBuffer.get(scannedImage);
        return null;
    }

    void removeScannedImage(ScannedImage image) {
        if (mScannedDocument.getValue() != null) {
            mScannedDocument.getValue().getScannedImageList().remove(image);
            mScannedImageBuffer.remove(image);
        }
    }

    void setBackStackEntry(final NavController navController, LifecycleOwner owner) {
        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if (backStack != null) {

            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            final Bitmap oriBitmapLiveData = savedStateHandle.get(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            final Point[] contourLiveData = savedStateHandle.get(ImageContourSelectorViewModel.TAG_CONTOUR);
            final MutableLiveData<Integer> flagLiveData = savedStateHandle.getLiveData(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);

            flagLiveData.observe(owner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    if (mScannedDocument.getValue() != null) {
                        if (integer == ImageContourSelectorViewModel.ADD_IMAGE) {
                            ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
                            scannedImage.setOriImage(oriBitmapLiveData);
                            scannedImage.setContour(contourLiveData);
                            mScannedImageBuffer.put(scannedImage, scannedImage.getFilteredBitmaps());
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
        if (backStack != null) {
            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_EDITED_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_CONTOUR);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);
        }
    }
}
