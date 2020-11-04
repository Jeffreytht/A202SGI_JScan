package com.example.opencvtesting.ui.documentScanner.document_reader;

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

import com.example.opencvtesting.Model.ScannedDocument;
import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.ui.documentScanner.image_contour_selector.ImageContourSelectorViewModel;
import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.core.Point;

import java.util.HashMap;
import java.util.List;

// Callback from async to insert data
interface ScannedImagePreComputation {
    void updateImageBuffer(int scannedImageId, Bitmap[] bitmaps);
}

// Asynchronous Task for image processing. To improve the performance of app and prevent main Ui thread freeze.
class ImagePreComputation extends AsyncTask<ScannedDocument, Integer, Void> {

    private final ScannedImagePreComputation mCallback;
    private final List<ScannedImage> mScannedImageList;

    public ImagePreComputation(List<ScannedImage> scannedImageList, ScannedImagePreComputation callback) {
        this.mCallback = callback;
        this.mScannedImageList = scannedImageList;
    }

    @Override
    protected Void doInBackground(ScannedDocument... scannedDocuments) {
        int totalImage = mScannedImageList.size();

        for (int imageIdx = 0; imageIdx < totalImage; imageIdx++) {
            ScannedImage si = mScannedImageList.get(imageIdx);
            mCallback.updateImageBuffer(si.getId(), si.getAllFilterBitmap());
        }
        return null;
    }
}

// Callback to update ui
interface ScannedImageFinishPreComputeCallback {
    void refreshUi();
}


public class DocumentReaderViewModel extends ViewModel implements ScannedImagePreComputation {

    private final MutableLiveData<ScannedDocument> mScannedDocument;
    private final HashMap<Integer, Bitmap[]> mScannedImageBuffer = new HashMap<>();
    private ImagePreComputation mImagePreComputationAsync;
    private ScannedImageFinishPreComputeCallback mScannedImageFinishPreComputeCallback;

    //Buffer
    private int mViewPagerCurrentIndex;

    public DocumentReaderViewModel() {
        this.mScannedDocument = new MutableLiveData<>();
        mViewPagerCurrentIndex = 0;
    }

    public void initViewModel(Bundle bundle, ScannedImageFinishPreComputeCallback callback) {
        DocumentReaderFragmentArgs args = DocumentReaderFragmentArgs.fromBundle(bundle);
        mScannedDocument.setValue(args.getScannedDocument());
        mScannedImageFinishPreComputeCallback = callback;
        mImagePreComputationAsync = new ImagePreComputation(args.getScannedDocument().getScannedImageList(), this);
        mImagePreComputationAsync.execute();
    }

    public int getViewPagerCurrentIndex() {
        return mViewPagerCurrentIndex;
    }

    LiveData<ScannedDocument> getScannedDocument() {
        return mScannedDocument;
    }

    public void rotateBitmapNContour(int index, int degree) {
        if (mScannedDocument.getValue() == null)
            return;

        ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(index);

        Bitmap[] bmpArr = {scannedImage.getOriImage()};
        ImageProcessing.rotateBitmapNContour(bmpArr, scannedImage.getContour(), degree);
        scannedImage.setOriImage(bmpArr[0]);
    }

    public void stopAsync() {
        if (mImagePreComputationAsync.getStatus() == AsyncTask.Status.RUNNING)
            mImagePreComputationAsync.cancel(true);
    }

    void setViewPagerCurrentIndex(int viewPagerCurrentIndex) {
        mViewPagerCurrentIndex = viewPagerCurrentIndex;
    }

    ScannedImage getCurrentSelectedImage() {
        if (mScannedDocument.getValue() != null)
            return mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
        return null;
    }

    Bitmap[] getCurrentSelectedFilteredImage() {
        if (mScannedDocument.getValue() == null) return null;
        int scannedDocumentId = mScannedDocument.getValue().getScannedImageList().get(getViewPagerCurrentIndex()).getId();
        if (mScannedImageBuffer.containsKey(scannedDocumentId))
            return mScannedImageBuffer.get(scannedDocumentId);
        return null;
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

    @Override
    public void updateImageBuffer(int scannedImageId, Bitmap[] bitmaps) {
        this.mScannedImageBuffer.put(scannedImageId, bitmaps);

        if (mScannedDocument.getValue() != null &&  mScannedDocument.getValue().getScannedImageList().get(getViewPagerCurrentIndex()).getId() == scannedImageId)
            mScannedImageFinishPreComputeCallback.refreshUi();
    }
}